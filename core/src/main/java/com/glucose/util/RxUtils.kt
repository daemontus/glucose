package com.glucose.util

import com.github.daemontus.Result
import com.github.daemontus.asError
import com.github.daemontus.asOk
import rx.Observable


/**
 * Create an observable that will not throw an error, instead it will propagate it in form of
 * result object.
 */
fun <R> Observable<R>.asResult(): Observable<Result<R, Throwable>>
        = this.map { it.asOk<R, Throwable>() }.onErrorReturn { it.asError() }