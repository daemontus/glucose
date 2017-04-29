package com.glucose2.app.transaction

import com.glucose.util.asResult
import rx.Observable
import rx.Subscription
import rx.android.schedulers.AndroidSchedulers
import rx.subjects.ReplaySubject
import java.util.*

/**
 * Reasoning about correctness:
 *  - All private variables and functions (even async callbacks) are synchronized with respect
 *  to the state, so no races should occur.
 *  - Upon start, there can't be any waiting actions, so no one needs to start them.
 *  - Upon action enqueue, an action is started if nothing is running.
 *  - Upon action termination, it is cleaned up and next action is started.
 *  - Upon unsubscribe from active action, it is cleaned up and next action is started.
 *
 *  Future work: Can we implement them without that nasty double replay? (Unicast seems nice,
 *  but does not provide all the API of ReplaySubject)
 */
internal class TransactionHostDelegate : TransactionHost {

    private val scheduler = AndroidSchedulers.mainThread()
    private val maxCapacity = 1000

    private var destroyed = false
    // first observable is the original transaction, second is the proxy subject
    private var pendingTransactions = LinkedList<Pair<Observable<*>, ReplaySubject<*>>>()
    private var activeTransaction: Pair<Observable<*>, ReplaySubject<*>>? = null
    private var activeSubscription: Subscription? = null

    override fun <T> submit(transaction: Observable<T>): Observable<T> {
        // Concat is just to flatten the observable.
        // Since it emits only one item, there is nothing to concat.
        // Note: we can't just use defer because that would mean the cache will be caching
        // the transaction results, not the proxy subjects and hence the transaction
        // couldn't be unsubscribed.
        return Observable.concat(Observable.fromCallable { enqueueAction(transaction) }
                .cacheWithInitialCapacity(1))    //make sure only one proxy is created)
    }

    internal fun onDestroy() {
        synchronized(this) {
            if (destroyed) throw IllegalStateException("This host is already destroyed.")
            destroyed = true
            //first clear pending to make sure unsubscribe won't start executing them
            pendingTransactions.forEach {
                it.second.onError(CannotExecuteException("ActionHost is shutting down."))
            }
            pendingTransactions.clear()
            activeSubscription?.unsubscribe()
            activeTransaction?.second?.let { proxy ->
                if (!proxy.hasCompleted() && !proxy.hasThrowable()) {
                    proxy.onError(PrematureTerminationException("ActionHost is shutting down."))
                }
            }
            activeSubscription = null
            activeTransaction = null
        }
    }

    // Create a valid mirror observable and enqueue it.
    // Note that this is called only after the the proxy is subscribed, because
    // we use the deferred fromCallable.
    private fun <R> enqueueAction(transaction: Observable<R>): Observable<R> {
        synchronized(this) {
            return if (pendingTransactions.size >= maxCapacity) {
                Observable.error<R>(CannotExecuteException("Host capacity reached."))
            } else if (destroyed) {
                Observable.error<R>(CannotExecuteException("Transaction host is destroyed."))
            } else {
                val proxy = ReplaySubject.create<R>().run { transaction.doOnEach(this) to this }
                pendingTransactions.add(proxy)
                takeNextAction()
                proxy.second
                        //On unsubscribe, remove this transaction from queue
                        //or unsubscribe it if it is active
                        .doOnUnsubscribe {
                            synchronized(this) {
                                pendingTransactions.removeAll {
                                    it == proxy
                                }
                                if (activeTransaction == proxy) {
                                    activeSubscription?.unsubscribe()
                                    activeTransaction = null
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
                        //should really cancel the transaction
                        //We can't use publish because that will negate the effects of the replay
                        //subject.
                        .replay().refCount()
            }
        }
    }

    // Try to execute next action in queue if possible.
    private fun takeNextAction() {
        synchronized(this) {
            if (!destroyed && activeSubscription == null && pendingTransactions.isNotEmpty()) {
                val nextAction = pendingTransactions.remove()
                activeTransaction = nextAction
                //Warning: Calling subscribe here can initiate a chain of calls
                //that will execute terminate/unsubscribe !before! the subscribe
                //call returns, meaning that activeSubscription will be assigned
                //something that is already terminated.
                activeSubscription = nextAction.first
                        //make sure each action runs by default on the same scheduler
                        .subscribeOn(scheduler)
                        .doOnTerminate {
                            // so this can actually happen before active subscription is assigned
                            synchronized(this) {
                                if (activeTransaction == nextAction) {
                                    activeTransaction = null
                                    activeSubscription = null
                                }
                                takeNextAction()
                            }
                        }.asResult().subscribe()
                //therefore we have to check that the action is still active!
                //We don't have to update the activeTransaction, because that is already
                //assigned and hence will be cleared correctly.
                if (activeSubscription?.isUnsubscribed ?: false) {
                    activeSubscription = null
                }
            }
        }
    }

}