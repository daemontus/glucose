package com.glucose2.app

import com.glucose.app.presenter.LifecycleException
import rx.Subscription

/**
 *
 */
interface Bindable<out T : Any> {

    /**
     * @throws [LifecycleException] if this instance is not bound.
     */
    @get:Throws(LifecycleException::class)
    val state: T

    val isBound: Boolean

    /**
     * Modify the subscription so that it is unsubscribed on next reset.
     */
    fun Subscription.whileBound(): Subscription

}

/**
 * See [Bindable.whileBound].
 */
fun Subscription.whileBound(target: Bindable<*>): Subscription = target.run {
    this@whileBound.whileBound()
}