package com.glucose.app

import android.support.annotation.AnyThread
import android.support.annotation.MainThread
import com.github.daemontus.egholm.functional.Result
import rx.Observable
import rx.Subscription
import rx.android.schedulers.AndroidSchedulers
import rx.subjects.ReplaySubject
import java.util.*

/**
 * Implementation of an [ActionHost] that executes all actions using the main thread scheduler.
 *
 * [MainThreadActionHost] uses [ReplaySubject] as the proxy observable. Hence multiple
 * subscriptions to proxy observables will not execute actions multiple times.
 *
 * Reasoning about correctness:
 *  - All private variables and functions can be called only form main thread,
 * hence no races should occur.
 *  - Upon start, there can't be any waiting actions, so no need to start them.
 *  - Upon action enqueue, an action is started if nothing is running.
 *  - Upon action termination, it is cleaned up and next action is started.
 *  - Upon unsubscribe from active action, it is cleaned up and next action is started.
 */
class MainThreadActionHost : ActionHost {

    @AnyThread
    override fun <R> post(action: Observable<R>): Observable<R> {
        return Observable.defer {
            //defer ensures that the action is not queued until subscribed to
            enqueueAction(action)
        }.subscribeOn(AndroidSchedulers.mainThread())   //ensure action is started on main thread
    }

    //All private variables can only be accessed from the main thread!!
    private var active = false
    private var pendingActions = LinkedList<Pair<Observable<*>, ReplaySubject<*>>>()
    private var activeAction: Pair<Observable<*>, ReplaySubject<*>>? = null
    private var activeSubscription: Subscription? = null

    /**
     * Create appropriate proxy observable and enqueue it.
     */
    @MainThread
    private fun <R> enqueueAction(action: Observable<R>): Observable<R> {
        if (!mainThread()) throw IllegalStateException("Enqueue not on main thread. Something is wrong!")
        return if (pendingActions.size > 10) {
            Observable.error<R>(CannotExecuteException("Host capacity reached"))
        } else if (!active) {
            Observable.error<R>(CannotExecuteException("Action host is inactive"))
        } else {
            val proxy = ReplaySubject.create<R>()
            pendingActions.add(action.doOnEach(proxy) to proxy)
            takeNextAction()
            proxy
                    //On unsubscribe, remove this action from queue
                    //or unsubscribe it if it is active
                    .observeOn(AndroidSchedulers.mainThread())
                    .doOnUnsubscribe {
                        pendingActions.removeAll { it.second == proxy }
                        if (activeAction?.second == proxy) {
                            activeSubscription?.unsubscribe()
                            activeAction = null
                            activeSubscription = null
                            //No need to notify proxy - it is unsubscribed!
                            takeNextAction()
                        }
                    }
                    //share will filter unsubscribe requests to only those that
                    //should really cancel the action
                    .share()
        }
    }

    /**
     * Try to execute next action if possible.
     */
    @MainThread
    private fun takeNextAction() {
        if (!mainThread()) throw IllegalStateException("Not on main thread. Something is wrong!")
        if (active && activeSubscription == null && pendingActions.isNotEmpty()) {
            val next = pendingActions.remove()
            activeAction = next
            activeSubscription = next.first
                .observeOn(AndroidSchedulers.mainThread())
                .doOnTerminate {
                    activeAction = null
                    activeSubscription = null
                    takeNextAction()
                }.asResult().subscribe {
                    when (it) {
                        is Result.Ok<*,*> -> actionLog("Action produced an item")
                        is Result.Error<*,*> -> actionLog("Action error: ${it.error}")
                    }
            }
        }
    }

    /**
     * Mark this [ActionHost] as ready to process actions.
     */
    @MainThread
    fun startProcessingActions() {
        if (!mainThread()) throw IllegalStateException("Not on main thread. Something is wrong!")
        if (active) throw IllegalStateException("This host is already started.")
        active = true
    }

    /**
     * Stop processing actions and notify unfinished actions with errors.
     *
     * (When this method returns, no action is being executed)
     */
    @MainThread
    fun stopProcessingActions() {
        if (!mainThread()) throw IllegalStateException("Not on main thread. Something is wrong!")
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