package com.glucose2.rx

import com.glucose2.app.LifecycleException
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.subjects.BehaviorSubject

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

    private val subscription: CompositeDisposable = CompositeDisposable()
    private val isActiveSubject: BehaviorSubject<Boolean> = BehaviorSubject.createDefault(false)

    /**
     * Bind a subscription to this binder.
     *
     * If the binder is not active, the subscription is unsubscribed immediately.
     * Otherwise, the subscription will be unsubscribed once it becomes inactive.
     *
     */
    fun bindDisposable(subscription: Disposable) {
        if (!isActive) {
            subscription.dispose()
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
            = observable.takeUntil(isActiveSubject.filter { !it })

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

    // TODO: Look around and ensure this is called
    internal fun destroy() {
        subscription.dispose()
    }

}