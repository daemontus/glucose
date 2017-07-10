package com.glucose2.app

import android.content.res.Configuration
import android.os.Bundle
import android.support.annotation.CallSuper
import android.view.View
import com.glucose2.rx.ObservableBinder
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

/**
 * Presenter is an extension of [Component] which
 *  - has full lifecycle, including start/resume/pause/stop
 *  - can have an associated [ComponentGroup]s to attach child components
 *
 *  start()
 *      -> this.onStart
 *
 *  resume()
 *      -> this.onResume
 *
 *  pause()
 *      -> this.onPause
 *
 *  stop()
 *      -> this.onStop
 */
open class Presenter(view: View, host: ComponentHost) : Component(view, host) {

    /* ========== Visible state ========== */

    /** Active while the presenter is part of a started hierarchy **/
    val started: ObservableBinder = ObservableBinder()

    /** Active while the presenter is part of a resumed hierarchy **/
    val resumed: ObservableBinder = ObservableBinder()

    /* ========== Internal state ========== */

    private val _groupMap = HashMap<String, ComponentGroup<*>>()

    private val _groups
        get() = _groupMap.values

    inner class ComponentGroupDelegate<out G : ComponentGroup<*>>(
            private val group: G
    ) : ReadOnlyProperty<Presenter, G> {

        operator fun provideDelegate(thisRef: Presenter, property: KProperty<*>): ReadOnlyProperty<Presenter, G> {
            _groupMap.put(property.name, group)
            return this
        }

        override fun getValue(thisRef: Presenter, property: KProperty<*>): G = group
    }

    /* ========== Driving the lifecycle ========== */

    /** Called exactly once during construction to collect all component groups. */
    protected open fun registerComponentGroups(): Map<String, ComponentGroup<*>> = emptyMap()

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
            state.putBundle(":group:$key", group.saveInstanceState())
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
            val groupState = state.getBundle(":group:$key") ?: Bundle()
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

    override fun detach(): Bundle {
        if (isResumed) pause()
        if (isStarted) stop()
        return super.detach()
    }

    override fun onDetach() {
        _groups.forEach { it.detach() }
        super.onDetach()
    }

}