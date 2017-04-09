package com.glucose2.app

import com.glucose.app.presenter.LifecycleException
import rx.Subscription

/**
 * Abstraction of all types that can be attached to some kind of parent.
 *
 * Typically, such objects form a tree-like structure.
 *
 */
interface Attachable<out T : Any> {

    /**
     * @throws [LifecycleException] if this instance is not attached.
     */
    @get:Throws(LifecycleException::class)
    val parent: T

    val isAttached: Boolean

    /**
     * Modify the subscription so that it is unsubscribed on next detach.
     */
    fun Subscription.whileAttached(): Subscription

}

/**
 * See [Attachable.whileAttached].
 */
fun Subscription.whileAttached(target: Attachable<*>): Subscription = target.run {
    this@whileAttached.whileAttached()
}