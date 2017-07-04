package com.glucose2.app

import android.content.res.Configuration
import android.os.Bundle
import android.support.annotation.CallSuper
import android.util.SparseArray
import android.view.View
import com.glucose2.app.event.EventHost
import com.glucose2.app.event.EventHostDelegate
import com.glucose2.rx.ObservableBinder
import com.glucose2.state.StateHost

/**
 * Component
 *  - has a fixed reference to its view
 *  - has a fixed reference to its host
 *  - has a dynamic reference to a state
 *  - serves as a node in the EventBus hierarchy
 *  - can be attached to a parent Presenter
 *  - can be moved between Presenter without changing state
 *  - can save/restore its state
 *  - can survive configuration change
 *
 *  Lifecycle:
 *  constructor
 *      -> this.init - prepare instance specific state
 *
 *  attach(parent, location, state)
 *      -> parent.addChild - decrease lifecycle if necessary (pause, stop), add to location
 *      -> this.attach - adjust to new state (only at this point does the component know it is attached)
 *      -> parent.attachChild - increase lifecycle if necessary (start, resume), update other kids (stop invisible children, etc.)
 *
 *  reattach(parent, location)
 *      -> oldParent.detachChild - prepare other children in the hierarchy (which will be brought to the front for example)
 *      -> this.beforeReattach - (the component forgets its old parent and for the next two !atomic! calls is in a limbo)
 *      -> oldParent.removeChild - remove the child from its current location
 *      -> parent.beforeAttach - see attach
 *      -> this.afterReattach - (the component has a parent again!)
 *      -> parent.afterAttach - see attach
 *
 *  detach(): state
 *      -> oldParent.detachChild - see reattach
 *      -> this.onDetach - last chance to commit something to state
 *      -> oldParent.removeChild - see reattach
 *
 *  Only for Presenter:
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
open class Component private constructor(
        private val _eventHost: EventHostDelegate
) : StateHost, EventHost by _eventHost {

    constructor(): this(EventHostDelegate())

    /* ========== Visible state ========== */

    /** While this binder is active, the component has not been destroyed yet. **/
    val alive: ObservableBinder

    /** While this binder is active, the component has an attached state.
        WARNING: The binder is NOT reset during [reattach] **/
    val attached: ObservableBinder

    /** The state data bundle of this component. When possible, use [State] delegates to
        access this data. **/
    override val state: Bundle
        get() = _state ?: lifecycleError("Component $this has no state.")

    /** Default: [View.NO_ID]
     *  Unique identifier of each component. Used to identify the component if its location
     *  in the hierarchy changes during activity restart (if the location remains the same,
     *  the component state should be saved within its parent group).
     */
    val id by ID_DELEGATE

    /** Default: true
     *  If true, this component is not recreated on configuration change.
     *  Instead, [onConfigurationChange] is called and user should handle the change manually.
     *
     *  Use this property to forcibly recreate parts of your component tree during config changes.
     **/
    val canChangeConfiguration by TRUE_DELEGATE

    /* ========== Internal state ========== */

    // Holds the actual state bundle of this component (never null if attached is active).
    private var _state: Bundle? = null

    // Holds a reference to current parent of this component.
    // Assuming attached is active, can be null only very briefly during reattach.
    // User is not able to access this value, it is for internal purposes only.
    private var _parent: ComponentGroup<*>? = null

    // initialize this whole mess in one place, so that it is easier to reason about correctness.
    init {
        alive = ObservableBinder().apply { this.start() }
        attached = ObservableBinder()
    }

    /* ========== Driving the lifecycle ========== */

    internal fun registerChild(child: Component) {
        child._eventHost.attach(this._eventHost)
    }

    internal fun unregisterChild(child: Component) {
        child._eventHost.detach()
    }

    /**
     * Attach this component to a [parent] group at the given [location] with the given [state].
     */
    fun <IP> attach(parent: ComponentGroup<IP>, location: IP, state: Bundle) {
        if (!isAlive) lifecycleError("Attaching a dead component: $this.")
        if (isAttached) lifecycleError("Component $this is already attached. Use reattach instead.")
        parent.addChild(this, location)
        _parent = parent
        this.onAttach(state)
        if (!isAttached) lifecycleError("super.attach not called properly in component $this")
        parent.attachChild(this)
    }

    /**
     * Move this component from current to new [location] under the same or a different [parent].
     * Component [state] remains the same.
     */
    fun <IP> reattach(parent: ComponentGroup<IP>, location: IP) {
        if (!isAttached) lifecycleError("Reattaching a component $this which is not attached.")
        _parent?.let { oldParent ->
            oldParent.detachChild(this)
            beforeReattach()
            _eventHost.detach()
            _parent = null
            oldParent.removeChild(this)
            parent.addChild(this, location)
            _parent = parent
            this.afterReattach()
            parent.attachChild(this)
        } ?: throw IllegalStateException("GLUCOSE INTERNAL ERROR: _parent is null during reattach.")
    }

    /**
     * Detach this component from its current [ComponentGroup] parent, returning the latest [state].
     */
    fun detach(): Bundle {
        if (!isAttached) lifecycleError("Detaching a component $this which is not attached.")
        _parent?.let { oldParent ->
            oldParent.detachChild(this)
            val stateCopy = this.state
            this.onDetach()
            _eventHost.detach()
            _parent = null
            oldParent.removeChild(this)
            return stateCopy
        } ?: throw IllegalStateException("GLUCOSE INTERNAL ERROR: _parent is null during detach.")
    }

    /**
     * Destroy this component (component must be detached first).
     */
    fun destroy() {
        if (!isAlive) lifecycleError("Component $this is already dead.")
        if (isAttached) lifecycleError("Component $this is attached. Detach it before destroying.")
        this.onDestroy()
        if (isAttached) lifecycleError("super.onDestroy not called properly in component $this")
    }

    /**
     * Notify the component that activity configuration has changed.
     * Only applicable if component is attached and [canChangeConfiguration] is true.
     */
    fun configurationChange(newConfig: Configuration) {
        if (!canChangeConfiguration) lifecycleError("ConfigurationChange called while canChangeConfiguration is false.")
        this.onConfigurationChange(newConfig)
    }

    /**
     * Save the state of this component into the given array and also return it.
     * Only applicable if component is attached and
     */
    fun saveHierarchyState(into: SparseArray<Bundle>): Bundle {
        if (!isAttached) lifecycleError("Cannot save state of a detached component $this.")
        val stateCopy = Bundle().apply { putAll(state) }
        if (id != View.NO_ID) {
            into.put(id, stateCopy)
        }
        return stateCopy
    }

    open fun saveInstanceState(): Bundle {
        if (!isAttached) lifecycleError("Cannot save state of a detached component $this.")
        return Bundle().apply { putAll(state) }
    }

    /* ========== Observing lifecycle ========== */

    @CallSuper
    protected open fun onAttach(state: Bundle) {
        this._state = state
        this.attached.start()
    }

    @CallSuper
    protected open fun onConfigurationChange(newConfig: Configuration) = Unit

    @CallSuper
    protected open fun beforeReattach() = Unit

    @CallSuper
    protected open fun afterReattach() = Unit

    @CallSuper
    protected open fun onDetach() {
        this.attached.stop()
        this._state = null
    }

    @CallSuper
    protected open fun onDestroy() {
        this.alive.stop()
        _eventHost.destroy()
    }

}

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