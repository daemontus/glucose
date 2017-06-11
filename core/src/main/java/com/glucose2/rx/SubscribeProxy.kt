package com.glucose2.rx

import rx.Observable
import rx.Observer
import rx.Subscriber
import rx.Subscription

/**
 * SubscribeProxy is a class which provides safe subscriptions
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
class SubscribeProxy<out T> internal constructor(
        private val observable: Observable<T>,
        private val binders: Array<out ObservableBinder>
) {

    fun subscribe(): Subscription?
            = checkState { observable.subscribe() }
    fun subscribe(onNext: (T) -> Unit): Subscription?
            = checkState { observable.subscribe(onNext) }
    fun subscribe(onNext: (T) -> Unit, onError: (Throwable) -> Unit): Subscription?
            = checkState { observable.subscribe(onNext, onError) }
    fun subscribe(onNext: (T) -> Unit, onError: (Throwable) -> Unit, onCompleted: () -> Unit)
            = checkState { observable.subscribe(onNext, onError, onCompleted) }
    fun subscribe(observer: Observer<in T>): Subscription?
            = checkState { observable.subscribe(observer) }
    fun subscribe(subscriber: Subscriber<in T>): Subscription?
            = checkState { observable.subscribe(subscriber) }

    private inline fun checkState(subscribe: () -> Subscription): Subscription? {
        return if (binders.any { !it.isActive }) {
            null
        } else {
            subscribe().also { subscription ->
                binders.forEach { it.bindSubscription(subscription) }
            }
        }
    }

}