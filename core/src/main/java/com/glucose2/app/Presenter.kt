package com.glucose2.app

import android.view.View
import android.view.ViewGroup
import com.glucose.app.presenter.LifecycleException
import com.glucose2.app.event.EventHost
import com.glucose2.app.event.EventHostDelegate
import rx.Subscription
import rx.subscriptions.CompositeSubscription
import java.util.*

interface HolderFactory {
    fun <T: Holder> obtain(clazz: Class<T>): T
    fun recycle(holder: Holder)
}

interface PresenterHost : HolderFactory {



}

internal open class Presenter(
        view: View,
        host: PresenterHost,
        private val eventHost: EventHostDelegate = EventHostDelegate()
) : Holder(view, host), EventHost by eventHost, HolderGroup, LifecycleHost<Presenter> {



    /* ========== Holder Group ============ */

    private val attached = ArrayList<Holder>()

    override val children: Sequence<Holder>
            get() = attached.asSequence()

    override val childrenRecursive: Sequence<Holder>
            get() = attached.asSequence().flatMap {
                if (it is Presenter) {
                    sequenceOf(it) + it.childrenRecursive
                } else sequenceOf(it)
            }

    override fun <T : Holder> attach(holder: T, location: InsertionPoint): T {
        // insert into view hierarchy
        location.invoke(view as ViewGroup, holder)
        if (!view.hasTransitiveChild(holder.view)) {
            throw LifecycleException("$holder view has not been inserted properly into $this")
        }
        // move holder into attached state
        attached.add(holder)
        holder.performAttach(this)
        return holder
    }

    override fun <T : Holder> detach(holder: T): T {
        if (holder !in attached || holder.parent != this) {
            throw LifecycleException("$holder not attached to $this (attached: $attached)")
        }
        if (!view.hasTransitiveChild(holder.view)) {
            throw LifecycleException("$holder is not in the view tree of this presenter. Has it been moved?")
        }
        // move holder into detached state
        holder.performDetach()
        attached.remove(holder)
        // remove holder from view hierarchy
        (holder.view.parent as ViewGroup).removeView(holder.view)
        return holder
    }



    /* ========== Lifecycle Host ============ */

    private val whileStarted = CompositeSubscription()
    private val whileResumed = CompositeSubscription()

    private var _started = false
    private var _resumed = false

    override val isStarted: Boolean
        get() = _started

    override val isResumed: Boolean
        get() = _resumed

    internal fun performStart() {
        if (!isAttached) {
            throw LifecycleException("Starting presenter $this which is not attached.")
        }
        if (isStarted) {
            throw LifecycleException("Presenter $this is already started.")
        }
        onStart()
        if (!isStarted) {
            throw LifecycleException("Super.onStart not called properly in $this.")
        }
    }

    internal fun performResume() {
        if (!isStarted) {
            throw LifecycleException("Resuming presenter $this which is not started.")
        }
        if (isResumed) {
            throw LifecycleException("Presenter $this is already resumed.")
        }
        onResume()
        if (!isResumed) {
            throw LifecycleException("Super.onResume not called properly in $this.")
        }
    }

    internal fun performPause() {
        if (!isResumed) {
            throw LifecycleException("Pausing presenter $this which is not resumed.")
        }
        onPause()
        if (isResumed) {
            throw LifecycleException("Super.onPause not called properly in $this.")
        }
    }

    internal fun performStop() {
        if (!isStarted) {
            throw LifecycleException("Stopping presenter $this which is not started.")
        }
        onStop()
        if (isStarted) {
            throw LifecycleException("Super.onStop not called properly in $this.")
        }
    }

    protected open fun onStart() {
        _started = true
        attached.forEach { if (it is Presenter) it.performStart() }
    }

    protected open fun onResume() {
        _resumed = true
        attached.forEach { if (it is Presenter) it.performResume() }
    }

    protected open fun onPause() {
        attached.forEach { if (it is Presenter) it.performPause() }
        whileResumed.clear()
        _resumed = false
    }

    protected open fun onStop() {
        attached.forEach { if (it is Presenter) it.performStop() }
        whileStarted.clear()
        _started = false
    }

    override fun Subscription.whileResumed(): Subscription {
        if (isResumed) {
            whileResumed.add(this)
        } else {
            unsubscribe()
        }
        return this
    }

    override fun Subscription.whileStarted(): Subscription {
        if (isStarted) {
            whileStarted.add(this)
        } else {
            unsubscribe()
        }
        return this
    }

    override fun performAttach(parent: Presenter) {
        super.performAttach(parent)
        if (parent.isStarted) this.performStart()
        if (parent.isResumed) this.performResume()
    }

    override fun performDetach() {
        if (this.isResumed) this.performResume()
        if (this.isStarted) this.performStart()
        super.performDetach()
    }

    /* ========== Holder overrides ============ */

    override fun onAttach(parent: Presenter) {
        super.onAttach(parent)
        this.eventHost.onAttach(parent.eventHost).whileAttached()
    }

    override fun performReset() {
        // ensure proper lifecycle semantics
        if (isResumed) performPause()
        if (isStarted) performStop()
        super.performReset()
    }

}