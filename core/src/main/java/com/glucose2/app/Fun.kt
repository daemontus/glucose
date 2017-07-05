package com.glucose2.app

import android.content.res.Configuration
import android.os.Bundle
import android.support.annotation.CallSuper
import com.glucose2.rx.ObservableBinder
import com.glucose2.state.StateHost

val Component.isAlive
    get() = this.alive.isActive

val Component.isAttached
    get() = this.attached.isActive

val Presenter.isStarted
    get() = this.started.isActive

val Presenter.isResumed
    get() = this.resumed.isActive

open class Presenter : Component() {

    /* ========== Visible state ========== */

    /** Active while the presenter is part of a started hierarchy **/
    val started: ObservableBinder = ObservableBinder()

    /** Active while the presenter is part of a resume hierarchy **/
    val resumed: ObservableBinder = ObservableBinder()

    /* ========== Internal state ========== */

    private val _groupMap: Map<String, ComponentGroup<*>> = registerComponentGroups()

    private val _groups
        get() = _groupMap.values

    /* ========== Driving the lifecycle ========== */

    /** Called exactly once during construction to collect all component groups. */
    protected fun registerComponentGroups(): Map<String, ComponentGroup<*>> = emptyMap()

    fun start() {
        if (!isAttached) lifecycleError("Starting a detach presenter $this.")
        if (isStarted) lifecycleError("Starting an already started presenter $this.")
        this.onStart()
        if (!isStarted) lifecycleError("super.onStart not called properly in $this.")
    }

    fun resume() {
        if (!isStarted) lifecycleError("Resuming a stopped presenter $this.")
        if (isResumed) lifecycleError("Resuming an already resumed presenter $this.")
        this.onResume()
        if (!isResumed) lifecycleError("super.onResume not called properly in $this.")
    }

    fun pause() {
        if (!isResumed) lifecycleError("Pausing a presenter $this which is not resumed.")
        this.onPause()
        if (isResumed) lifecycleError("super.onPause not called properly in $this.")
    }

    fun stop() {
        if (!isStarted) lifecycleError("Stopping a presenter $this which is not started.")
        this.onStop()
        if (isStarted) lifecycleError("super.onStop not called properly in $this.")
    }

    override fun saveInstanceState(): Bundle {
        val state = super.saveInstanceState()
        for ((key, group) in _groupMap) {
            state.putBundle(key, group.saveInstanceState())
        }
        return state
    }

    override fun onConfigurationChange(newConfig: Configuration) {
        _groups.forEach { it.configurationChange(newConfig) }
        super.onConfigurationChange(newConfig)
    }

    /* ========== Observing lifecycle ========== */

    override fun onAttach(state: Bundle) {
        super.onAttach(state)
        for ((key, group) in _groupMap) {
            val groupState = state.getBundle(key) ?: Bundle()
            group.attach(groupState)
        }
    }

    @CallSuper
    protected open fun onStart() {
        started.start()
        _groups.forEach { it.start() }
    }

    @CallSuper
    protected open fun onResume() {
        resumed.start()
        _groups.forEach { it.resume() }
    }

    @CallSuper
    protected open fun onPause() {
        _groups.forEach { it.pause() }
        resumed.stop()
    }

    @CallSuper
    protected open fun onStop() {
        _groups.forEach { it.stop() }
        started.stop()
    }

    override fun onDetach() {
        if (isResumed) pause()
        if (isStarted) stop()
        _groups.forEach { it.detach() }
        super.onDetach()
    }
}

abstract class ComponentGroup<in IP> internal constructor(
        private val host: Parent
) : StateHost {

    constructor(host: Presenter) : this(object : Parent {
        override fun registerChild(child: Component) {
            host.registerChild(child)
        }
        override fun unregisterChild(child: Component) {
            host.unregisterChild(child)
        }
    })

    internal interface Parent {
        fun registerChild(child: Component)
        fun unregisterChild(child: Component)
    }

    private var _state: Bundle? = null
    override val state: Bundle
        get() = _state ?: lifecycleError("ComponentGroup $this currently has no state.")

    @CallSuper
    open fun attach(state: Bundle) {
        _state = state
    }

    @CallSuper
    open fun detach(): Bundle {
        val state = saveInstanceState()
        _state = null
        return state
    }

    open fun saveInstanceState(): Bundle {
        return Bundle().apply { putAll(state) }
    }

    @CallSuper
    open fun addChild(child: Component, location: IP) {
        host.registerChild(child)
    }

    @CallSuper
    open fun attachChild(child: Component) = Unit

    @CallSuper
    open fun detachChild(child: Component) = Unit

    @CallSuper
    open fun removeChild(child: Component) {
        host.unregisterChild(child)
    }

    @CallSuper
    abstract fun configurationChange(newConfig: Configuration)

    @CallSuper
    abstract fun start()

    @CallSuper
    abstract fun resume()

    @CallSuper
    abstract fun pause()

    @CallSuper
    abstract fun stop()

}