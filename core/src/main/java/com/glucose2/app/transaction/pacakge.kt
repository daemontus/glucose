package com.glucose2.app.transaction

import rx.Observable

/**
 * Observed by the proxy observable when the [TransactionHost] cannot execute
 * a transaction. Either due to congestion or due to shutdown.
 */
class CannotExecuteException(message: String) : Exception(message)

/**
 * Observed by the proxy observable when the [TransactionHost] has to
 * interrupt a transaction because of an ongoing shutdown.
 */
class PrematureTerminationException(message: String) : Exception(message)

/**
 * Reversed alternative to [TransactionHost.submit].
 */
fun <T> Observable<T>.asTransaction(host: TransactionHost): Observable<T>
    = host.submit(this)