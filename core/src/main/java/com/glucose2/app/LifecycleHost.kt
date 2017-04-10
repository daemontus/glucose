package com.glucose2.app

import rx.Subscription

/**
 * Abstraction over components that are able to follow activity's started/resumed state.
 *
 * Note that it extends the [Bindable] interface. Semantically, a LifecycleHost
 * can only enter started or resumed state when it is also bound. Similarly, when
 * resetting the binding, any started / resumed state should also be cleared.
 *
 */
interface LifecycleHost<out T : Any> : Bindable<T> {

    val isStarted: Boolean

    val isResumed: Boolean

    /**
     * Modify the subscription so that it is unsubscribed on next stop.
     */
    fun Subscription.whileStarted(): Subscription

    /**
     * Modify the subscription so that it is unsubscribed on next pause.
     */
    fun Subscription.whileResumed(): Subscription

}

/**
 * See [LifecycleHost.whileStarted].
 */
fun Subscription.whileStarted(target: LifecycleHost<*>): Subscription = target.run {
    this@whileStarted.whileStarted()
}

/**
 * See [LifecycleHost.whileResumed].
 */
fun Subscription.whileResumed(target: LifecycleHost<*>): Subscription = target.run {
    this@whileResumed.whileResumed()
}