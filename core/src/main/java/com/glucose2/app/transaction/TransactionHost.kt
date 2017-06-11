package com.glucose2.app.transaction

import android.support.annotation.AnyThread
import rx.Observable
import rx.android.schedulers.AndroidSchedulers

/**
 * TransactionHost is implemented by Glucose components which allow execution of
 * transactions.
 *
 * It is responsible for executing transactions and ensuring their mutual exclusivity.
 * For more information about the transaction mechanism, see the package documentation.
 *
 * One can submit a new transaction either using the [submit] method or one of the
 * [asTransaction] extension functions.
 *
 * After submitting, one has to subscribe to the returned proxy observable in order
 * to enqueue the transaction. Once the transaction is being executed, the proxy observable
 * will mirror the behaviour of the transaction observable.
 *
 * To cancel the transaction (either pending or running), simply unsubscribe from the
 * proxy observable.
 *
 * Finally, if the transaction cannot be executed or has to be terminated prematurely,
 * [CannotExecuteException] or [PrematureTerminationException] will be observed by
 * the proxy observable (not by the transaction itself).
 *
 * All transactions are by default executed on the [AndroidSchedulers.mainThread].
 *
 * One should also keep in mind that transactions are a form of critical section, so
 * the usual rules for deadlocks apply (avoid cyclic dependencies, etc.). Especially,
 * avoid using proxy observables as transactions (transaction.submit().submit()). This
 * will cause an immediate deadlock, since the second transaction will wait for the
 * first one to finish, but will be queued before it, thus, since transactions are exclusive,
 * creating a never ending transaction.
 */
@AnyThread
interface TransactionHost {

    /**
     * Create a proxy observable which, once subscribed, enqueues given [transaction]
     * for execution and mirrors its behaviour.
     */
    fun <T> submit(transaction: Observable<T>): Observable<T>

    /**
     * Extension alternative to [TransactionHost.submit]
     */
    fun <T> Observable<T>.asTransaction(): Observable<T> = submit(this)

}
