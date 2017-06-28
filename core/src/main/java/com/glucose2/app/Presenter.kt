package com.glucose2.app

import android.os.Bundle
import android.view.View
import com.glucose2.rx.ObservableBinder

open class Presenter(
        view: View,
        host: ComponentHost,
        group: (Presenter) -> ComponentGroup
) : Component(view, host) {

    override fun onDestroy() {
        super.onDestroy()
        started.performDestroy()
        resumed.performDestroy()
    }

    override fun detach() {
        if (isResumed) performPause()
        if (isStarted) performStop()
        super.detach()
    }

    /* ========== Component Group ============ */

    private val group = group(this)

    val children: Sequence<Component>
        get() = group.children

    val childrenRecursive: Sequence<Component>
        get() = group.childrenRecursive

    internal fun afterChildAttach(component: Component) {
        group.afterChildAttach(component)
    }

    internal fun beforeChildDetach(component: Component) {
        group.beforeChildDetach(component)
    }

    internal fun addChild(component: Component) {
        group.removeChild(component)
    }

    internal fun removeChild(component: Component) {
        group.removeChild(component)
    }

    /* ========== Advanced lifecycle ============ */

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
        group.performStart()
    }

    protected open fun onResume() {
        resumed.performStart()
        group.performResume()
    }

    protected open fun onPause() {
        group.performPause()
        resumed.performStop()
    }

    protected open fun onStop() {
        group.performStop()
        started.performStop()
    }

    /* ========== State preservation ========== */

    override fun saveInstanceState(): Bundle? {
        val state = super.saveInstanceState()
        val groupState = group.saveInstanceState()
        return when {
            state == null && groupState == null -> null
            state == null -> groupState
            groupState == null -> state
            else -> {
                state.putAll(groupState)
                state
            }
        }
    }

}