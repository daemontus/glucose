package com.glucose2.app

import com.glucose.app.presenter.LifecycleException
import rx.Subscription

/**
 * Abstraction of all types that can be attached to some kind of parent.
 */
interface Attachable<out T> {

    /**
     * @throws [LifecycleException] if this instance is not attached.
     */
    @get:Throws(LifecycleException::class)
    val parent: T

    val isAttached: Boolean

    /**
     * Modify the subscription so that it is unsubscribed before next detach.
     */
    fun Subscription.whileAttached(): Subscription

}

/**
 * See [Attachable.whileAttached].
 */
fun Subscription.whileAttached(target: Attachable<*>): Subscription = target.run {
    this@whileAttached.whileAttached()
}

class AttachDelegate<out T : Any> : Attachable<T> {

    private val _parent: T? = null

    override val parent: T
        get() = _parent ?: throw LifecycleException("Object is not attached to its parent.")

    override val isAttached: Boolean
        get() = _parent != null

    override fun Subscription.whileAttached(): Subscription {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }


}