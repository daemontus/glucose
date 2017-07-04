package com.glucose2.rx

import com.glucose2.app.LifecycleException
import rx.Observable
import rx.Subscription
import rx.subjects.BehaviorSubject
import rx.subscriptions.CompositeSubscription

/**
 * ObservableBinder is a class which allows you to hook observables
 * and subscriptions to its lifecycle. It has a binary active/inactive
 * state. While inactive, every associated observable will be terminated
 * with onCompleted and each associated subscription will be unsubscribed.
 *
 * By design, this class is part of the public API but cannot be instantiated
 * outside of Glucose. That is to ensure proper lifecycle handling.
 *
 * @see [subscribeWhile]
 * @see [observeWhile]
 */
class ObservableBinder internal constructor() {

    /**
     * Indicates whether this binder is accepting new subscriptions/observables.
     */
    var isActive: Boolean = false
        private set

    private val subscription: CompositeSubscription = CompositeSubscription()
    private val isActiveSubject: BehaviorSubject<Boolean> = BehaviorSubject.create(false)

    /**
     * Bind a subscription to this binder.
     *
     * If the binder is not active, the subscription is unsubscribed immediately.
     * Otherwise, the subscription will be unsubscribed once it becomes inactive.
     *
     */
    fun bindSubscription(subscription: Subscription) {
        if (!isActive) {
            subscription.unsubscribe()
        } else {
            this.subscription.add(subscription)
        }
    }

    /**
     * Bind an observable to this binder.
     *
     * If the binder is not active (at the time of subscription), onCompleted should be invoked
     * immediately. Otherwise, onCompleted will be invoked when it becomes inactive.
     *
     */
    fun <T> addObservable(observable: Observable<T>): Observable<T>
            = observable.takeUntil(isActiveSubject.filter { it == false })

    internal fun start() {
        if (isActive) {
            throw LifecycleException("Starting active ObservableBinder.")
        }
        isActive = true
        isActiveSubject.onNext(true)
    }

    internal fun stop() {
        if (!isActive) {
            throw LifecycleException("Stopping inactive ObservableBinder.")
        }
        isActive = false
        subscription.clear()
        isActiveSubject.onNext(false)
    }

    internal fun destroy() {
        subscription.unsubscribe()
    }

}