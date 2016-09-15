package com.glucose.app.presenter

import com.glucose.util.asResult
import rx.Observable
import rx.Scheduler
import rx.Subscription
import rx.subjects.ReplaySubject
import java.util.*

/**
 * Implementation of an [ActionHost] that executes all actions using a provided scheduler.
 *
 * [ActionDelegate] uses [ReplaySubject] as the proxy observable. Hence multiple
 * subscriptions to proxy observables will not execute actions multiple times.
 * However, results of each action are effectively cached, so try to avoid actions emitting
 * a high amount of items.
 *
 * Reasoning about correctness:
 *  - All private variables and functions (even async callbacks) are synchronized with respect
 *  to the state, so no races should occur.
 *  - Upon start, there can't be any waiting actions, so no need to start them.
 *  - Upon action enqueue, an action is started if nothing is running.
 *  - Upon action termination, it is cleaned up and next action is started.
 *  - Upon unsubscribe from active action, it is cleaned up and next action is started.
 *
 *  Future work: Can we implement them without that nasty double replay? (Unicast seems nice,
 *  but does not provide all the API of ReplaySubject)
 */
internal class ActionDelegate(
        private val scheduler: Scheduler,
        private val maxCapacity: Int = Int.MAX_VALUE
) : ActionHost {

    override fun <R> post(action: Observable<R>): Observable<R> {
        //Concat is just to flatten the observable.
        //Since it emits only one item, there is nothing to concat.
        return Observable.concat(Observable.fromCallable { enqueueAction(action) }
                .cacheWithInitialCapacity(1))    //make sure only one proxy is created)
    }

    //Note: We can't just recreate the host on every attach, because
    //we don't know when the proxy will be subscribed.
    private var active = false
    private var pendingActions = LinkedList<Pair<Observable<*>, ReplaySubject<*>>>()
    private var activeAction: Pair<Observable<*>, ReplaySubject<*>>? = null
    private var activeSubscription: Subscription? = null

    /**
     * Create appropriate proxy observable and enqueue it.
     */
    private fun <R> enqueueAction(action: Observable<R>): Observable<R> {
        synchronized(this) {
            return if (pendingActions.size >= maxCapacity) {
                Observable.error<R>(CannotExecuteException("Host capacity reached"))
            } else if (!active) {
                Observable.error<R>(CannotExecuteException("Action host is inactive"))
            } else {
                val proxy = ReplaySubject.create<R>().run { action.doOnEach(this) to this }
                pendingActions.add(proxy)
                takeNextAction()
                proxy.second
                        //On unsubscribe, remove this action from queue
                        //or unsubscribe it if it is active
                        .doOnUnsubscribe {
                            synchronized(this) {
                                pendingActions.removeAll {
                                    it.second == proxy
                                }
                                if (activeAction == proxy) {
                                    activeSubscription?.unsubscribe()
                                    activeAction = null
                                    activeSubscription = null
                                    takeNextAction()
                                }
                                if (!proxy.second.hasCompleted() && !proxy.second.hasThrowable()) {
                                    //Proxy is a replay subject, so we need to make sure future
                                    //subscribers will get some closure.
                                    proxy.second.onCompleted()
                                }
                            }
                        }
                        //This will filter unsubscribe requests to only those that
                        //should really cancel the action
                        //We can't use publish because that will negate the effects of the replay
                        //subject.
                        .replay().refCount()
            }
        }
    }

    /**
     * Try to execute next action if possible.
     */
    private fun takeNextAction() {
        synchronized(this) {
            if (active && activeSubscription == null && pendingActions.isNotEmpty()) {
                val nextAction = pendingActions.remove()
                activeAction = nextAction
                activeSubscription = nextAction.first
                        //make sure each action runs by default on the same scheduler
                        .subscribeOn(scheduler)
                        .doOnTerminate {
                            synchronized(this) {
                                if (activeAction == nextAction) {
                                    activeAction = null
                                    activeSubscription = null
                                }
                                takeNextAction()
                            }
                        }.asResult().subscribe()
            }
        }
    }

    /**
     * Mark this [ActionHost] as ready to process actions.
     */
    internal fun startProcessingActions() {
        synchronized(this) {
            if (active) throw IllegalStateException("This host is already started.")
            active = true
        }
    }

    /**
     * Stop processing actions and notify unfinished actions with errors.
     *
     * (When this method returns, no action is being executed)
     */
    internal fun stopProcessingActions() {
        synchronized(this) {
            if (!active) throw IllegalStateException("This host isn't started.")
            active = false
            activeSubscription?.unsubscribe()
            activeAction?.second?.let { proxy ->
                if (!proxy.hasCompleted() && !proxy.hasThrowable()) {
                    proxy.onError(PrematureTerminationException("ActionHost is shutting down."))
                }
            }
            pendingActions.forEach {
                it.second.onError(CannotExecuteException("ActionHost is shutting down."))
            }
            pendingActions.clear()
        }
    }

}