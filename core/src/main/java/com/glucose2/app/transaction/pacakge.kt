package com.glucose2.app.transaction

import io.reactivex.Observable


/**
 * Observed by the proxy observable when the [TransactionHost] cannot execute
 * a transaction. Either due to congestion or due to shutdown.
 */
class CannotExecuteException(message: String) : RuntimeException(message)

/**
 * Observed by the proxy observable when the [TransactionHost] has to
 * interrupt a transaction because of an ongoing shutdown.
 */
class PrematureTerminationException(message: String) : RuntimeException(message)

/**
 * Reversed alternative to [TransactionHost.submit].
 */
fun <T> Observable<T>.asTransaction(host: TransactionHost): Observable<T>
    = host.submit(this)