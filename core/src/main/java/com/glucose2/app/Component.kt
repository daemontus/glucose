package com.glucose2.app

import android.content.Context
import android.content.res.Configuration
import android.os.Bundle
import android.support.annotation.CallSuper
import android.util.SparseArray
import android.view.View
import com.glucose2.ContextHost
import com.glucose2.ResettableLazyDelegate
import com.glucose2.ResourceGetter
import com.glucose2.app.event.EventHost
import com.glucose2.app.event.EventHostDelegate
import com.glucose2.app.transaction.TransactionHost
import com.glucose2.rx.ObservableBinder
import com.glucose2.state.StateHost
import com.glucose2.view.ViewHost
import kotlin.properties.ReadOnlyProperty

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
 */
open class Component private constructor(
        override val view: View,
        val host: ComponentHost,
        private val _eventHost: EventHostDelegate
) : StateHost, EventHost by _eventHost, ViewHost, TransactionHost by host, ContextHost {

    constructor(view: View, host: ComponentHost): this(view, host, EventHostDelegate())

    /* ========== Visible state ========== */

    /** While this binder is active, the component has not been destroyed yet. **/
    val alive: ObservableBinder

    /** While this binder is active, the component has an attached state.
        WARNING: The binder is NOT reset during [reattach] **/
    val attached: ObservableBinder

    /** This binder is active while a single configuration is associated with this component.
        Once the configuration changes, this binder will reset. **/
    val hasConfiguration: ObservableBinder

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
    var canChangeConfiguration = true

    /** Default: true
     *  If false, the component is never reused (it should still be recycled though).
     *
     *  Set to false before recycling a component which had [canChangeConfiguration] set to false.
     */
    var canReuse = true

    override val context: Context = host.activity

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
        hasConfiguration = ObservableBinder().apply { this.start() }
        attached = ObservableBinder()
    }

    /* ========== Driving the lifecycle ========== */

    // used by ComponentGroups to connect the EventHosts.
    internal fun registerChild(child: Component) {
        child._eventHost.attach(this._eventHost)
    }

    // used by ComponentGroups to disconnect the EventHosts.
    internal fun unregisterChild(child: Component) {
        child._eventHost.detach()
    }

    internal fun getEventHostDelegate(): EventHostDelegate = _eventHost

    /**
     * Attach this component to a [parent] group at the given [location] with the given [state].
     */
    open fun <IP> attach(parent: ComponentGroup<IP>, location: IP, state: Bundle) {
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
    open fun <IP> reattach(parent: ComponentGroup<IP>, location: IP) {
        if (!isAttached) lifecycleError("Reattaching a component $this which is not attached.")
        _parent!!.let { oldParent ->
            oldParent.detachChild(this)
            this.beforeReattach()
            _parent = null
            oldParent.removeChild(this)
            parent.addChild(this, location)
            _parent = parent
            this.afterReattach()
            parent.attachChild(this)
        }
    }

    /**
     * Detach this component from its current [ComponentGroup] parent, returning the latest [state].
     */
    open fun detach(): Bundle {
        if (!isAttached) lifecycleError("Detaching a component $this which is not attached.")
        _parent!!.let { oldParent ->
            oldParent.detachChild(this)
            val stateCopy = this.state
            this.onDetach()
            _eventHost.detach()
            _parent = null
            oldParent.removeChild(this)
            return stateCopy
        }
    }

    /**
     * Destroy this component (component must be detached first).
     */
    open fun destroy() {
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
        val stateCopy = saveInstanceState()
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
    protected open fun onConfigurationChange(newConfig: Configuration) {
        hasConfiguration.stop()
        hasConfiguration.start()
    }

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
        hasConfiguration.stop()
        this.alive.stop()
        attached.destroy()
        hasConfiguration.destroy()
        alive.destroy()
        _eventHost.destroy()
    }

    /* ========== Resource delegates ========== */

    private class ResourceDelegate<out T : Any>(
            private val id: Int,
            private val getter: ResourceGetter<T>,
            resetTrigger: ObservableBinder
    ) : ResettableLazyDelegate<ContextHost, T>(resetTrigger) {

        override fun initializer(thisRef: ContextHost): T {
            return getter.run { thisRef.context.get(id) }
        }

    }

    fun <T : Any> resourceDelegate(id: Int, getter: ResourceGetter<T>): ReadOnlyProperty<ContextHost, T>
            = ResourceDelegate(id, getter, hasConfiguration)

}