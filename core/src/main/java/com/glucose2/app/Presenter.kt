package com.glucose2.app

import android.view.View
import android.view.ViewGroup
import com.glucose2.app.component.LifecycleHost
import com.glucose2.app.event.EventHost
import com.glucose2.app.event.EventHostDelegate
import com.glucose2.app.transaction.TransactionHost
import com.glucose2.rx.ObservableBinder
import rx.Subscription
import rx.subscriptions.CompositeSubscription
import java.util.*

open class Presenter(
        view: View,
        host: ComponentHost
) : AbstractComponent(view, host), TransactionHost by host {

    override fun onDestroy() {
        super.onDestroy()
        started.performDestroy()
        resumed.performDestroy()
    }

/* ========== Holder Group ============ */

    private val _children = ArrayList<Component>()

    val children: Sequence<Component>
            get() = _children.asSequence()

    val childrenRecursive: Sequence<Component>
            get() = _children.asSequence().flatMap {
                sequenceOf(it) + if (it is ComponentGroup) it.childrenRecursive else emptySequence()
            }

    override fun <T : Component> attach(component: T, location: InsertionPoint): T {
        // decrease lifecycle if necessary
        if (component is Presenter) {
            if (component.isResumed && !this.isResumed) component.performPause()
            if (component.isStarted && !this.isStarted) component.performStop()
        }
        // remove from old position if necessary
        if (component.view.parent != null) {
            (component.view.parent as ViewGroup).removeView(component.view)
        }
        // insert into view hierarchy
        val parent = location.invoke(view as ViewGroup, component)
        if (!view.hasTransitiveChild(parent) || !parent.hasTransitiveChild(component.view)) {
            lifecycleError("$component view has not been inserted properly into $this")
        }
        // move holder into attached state
        _children.add(component)

        holder.performAttach(this)
        return holder
    }

    fun <T : AbstractComponent> detach(component: T): T {
        if (component !in _children) {
            lifecycleError("$component not attached to $this (children: $attached)")
        }
        if (!view.hasTransitiveChild(component.view)) {
            lifecycleError("$component is not in the view tree of $this. Has it been moved?")
        }
        if (component is Presenter) {
            if (component.isResumed) component.performPause()
            if (component.isStarted) component.performStop()
        }
        // move holder into detached state
        component.performDetach()
        _children.remove(component)
        // remove component from view hierarchy
        (component.view.parent as ViewGroup).removeView(component.view)
        return component
    }

    /* ========== Lifecycle Host ============ */

    val started: ObservableBinder = ObservableBinder()
    val resumed: ObservableBinder = ObservableBinder()

    val isStarted: Boolean = started.isActive
    val isResumed: Boolean = resumed.isActive

    internal fun performStart() {
        if (!isAttached) lifecycleError("Starting presenter [$this] which is not attached.")
        if (isStarted) lifecycleError("Presenter [$this] is already started.")
        onStart()
        if (!isStarted) lifecycleError("Super.onStart not called properly in [$this].")
    }

    internal fun performResume() {
        if (!isStarted) lifecycleError("Resuming presenter [$this] which is not started.")
        if (isResumed) lifecycleError("Presenter [$this] is already resumed.")
        onResume()
        if (!isResumed) lifecycleError("Super.onResume not called properly in [$this].")
    }

    internal fun performPause() {
        if (!isResumed) lifecycleError("Pausing presenter [$this] which is not resumed.")
        onPause()
        if (isResumed) lifecycleError("Super.onPause not called properly in [$this].")
    }

    internal fun performStop() {
        if (!isStarted) lifecycleError("Stopping presenter [$this] which is not started.")
        onStop()
        if (isStarted) lifecycleError("Super.onStop not called properly in [$this].")
    }

    protected open fun onStart() {
        started.performStart()
        _children.forEach { if (it is Presenter) it.performStart() }
    }

    protected open fun onResume() {
        resumed.performStart()
        _children.forEach { if (it is Presenter) it.performResume() }
    }

    protected open fun onPause() {
        _children.forEach { if (it is Presenter) it.performPause() }
        resumed.performStop()
    }

    protected open fun onStop() {
        _children.forEach { if (it is Presenter) it.performStop() }
        started.performStop()
    }

}