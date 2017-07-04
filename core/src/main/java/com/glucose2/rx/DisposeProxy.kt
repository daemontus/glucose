package com.glucose2.rx

import io.reactivex.Observable
import io.reactivex.disposables.Disposable


/**
 * DisposeProxy is a class which provides safe subscriptions
 * and as such can be only used as the last element in the observable
 * chain.
 *
 * It mimics the subscribe part of the [Observable] API but nothing more.
 *
 * By design, the class is part of the public API, but cannot be instantiated
 * outside of Glucose. This is mainly to avoid problems with unexpected usage
 * outside of usual scenarios.
 *
 * @see [subscribeWhile]
 * @see [observeWhile]
 */
class DisposeProxy<out T> internal constructor(
        private val observable: Observable<T>,
        private val binders: Array<out ObservableBinder>
) {

    fun subscribe(): Disposable?
            = checkState { observable.subscribe() }
    fun subscribe(onNext: (T) -> Unit): Disposable?
            = checkState { observable.subscribe(onNext) }
    fun subscribe(onNext: (T) -> Unit, onError: (Throwable) -> Unit): Disposable?
            = checkState { observable.subscribe(onNext, onError) }
    fun subscribe(onNext: (T) -> Unit, onError: (Throwable) -> Unit, onCompleted: () -> Unit)
            = checkState { observable.subscribe(onNext, onError, onCompleted) }
    // TODO: Deal with this shit...
    //fun subscribe(observer: Observer<in T>): Disposable?
    //        = checkState { observable.subscribe(observer) }
    //fun subscribe(subscriber: Disposable<in T>): Disposable?
    //        = checkState { observable.subscribe(subscriber) }

    private inline fun checkState(subscribe: () -> Disposable): Disposable? {
        return if (binders.any { !it.isActive }) {
            null
        } else {
            subscribe().also { subscription ->
                binders.forEach { it.bindDisposable(subscription) }
            }
        }
    }

}