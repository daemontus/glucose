package com.glucose2.app

import rx.Subscription

/**
 * Abstraction over components that are able to follow activity's started/resumed state.
 *
 * Note that the semantics are a little more complex when applied to object that is
 * also [Bindable] and [Attachable].
 *
 * Specifically, the lifecycle of [Bindable] is completely independent on ths LifecycleHost.
 * On the other hand, LifecycleHost is directly dependent on the [Attachable] interface.
 *
 * That is because the information about the current state are passed from parent
 * in the attach hierarchy. Therefore one has to be attached to have proper information
 * about current state of the lifecycle.
 *
 */
interface LifecycleHost<out T : Any> : Attachable<T> {

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