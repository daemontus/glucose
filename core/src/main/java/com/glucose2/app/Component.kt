package com.glucose2.app

import android.os.Bundle
import android.support.annotation.CallSuper
import android.util.SparseArray
import android.view.View
import android.view.ViewGroup
import com.glucose2.app.event.EventHostDelegate
import com.glucose2.app.transaction.TransactionHost
import com.glucose2.rx.ObservableBinder
import com.glucose2.state.StateHost
import com.glucose2.view.ViewHost

open class Component private constructor(
    override final val view: View,
    host: ComponentHost,
    private val eventHostDelegate: EventHostDelegate
) : TransactionHost by host, StateHost, ViewHost {

    constructor(view: View, host: ComponentHost) : this(view, host, EventHostDelegate())

    /* ========== Basic parameters ========== */

    /**
     * Unique integer identifier of this object. Default: [View.NO_ID].
     *
     * Used when searching/saving/restoring the component tree.
     */
    val id: Int by ID_DELEGATE

    /**
     * Save the tree state of this component if possible. Default: true.
     *
     * Note that you don't always need a valid [id], assuming the position
     * of the object in the component tree is uniquely identifiable.
     */
    val preserveState: Boolean by TRUE_DELEGATE

    /**
     * Save the tree location of this component if possible. Default: true.
     *
     * If [preserveState] is false, this setting has no effect, since no state is saved.
     *
     * Otherwise, the state is preserved only by its [id], not by its position. Mainly,
     * the component won't be recreated automatically, since there is no way to tell
     * where it should be attached. On the other hand, assuming it is recreated by some
     * other means, the state will be restored.
     *
     * This is useful when implementing adapters or other structures that manage children
     * separately, but still require some kind of state preservation.
     */
    val preservePosition: Boolean by TRUE_DELEGATE

    /**
     * Don't recreate this component when configuration changes. Default: true.
     *
     * If false, this component together with all its children will be recreated on each
     * configuration change.
     */
    val surviveConfigChange: Boolean by TRUE_DELEGATE

    /* ========== Elementary lifecycle ========== */

    private var _host: ComponentHost? = host

    val host: ComponentHost
        get() = _host ?: throw LifecycleException("The component has been destoryed.")

    /**
     * Monitors the main lifecycle of the component - that is, observables associated with
     * this binder will be unsubscribed once the component is destroyed.
     */
    val alive: ObservableBinder = ObservableBinder().apply { performStart() }

    /**
     * Check whether this component hasn't been destroyed yet.
     */
    val isAlive: Boolean
        get() = alive.isActive

    internal fun performDestroy() {
        if (!isAlive) lifecycleError("Destroying presenter [$this] which is not alive.")
        if (isAttached) lifecycleError("Presenter [$this] is still attached.")
        onDestroy()
        if (isAlive) lifecycleError("super.onDestroy not called properly on [$this]")
    }

    /**
     * Called when the component is being destroyed.
     *
     * Once this method is called, any access to the [host] property will result in a
     * [LifecycleException].
     */
    @CallSuper
    protected open fun onDestroy() {
        eventHostDelegate.performDestroy()
        attached.performDestroy()
        alive.performStop()
        alive.performDestroy()
        _host = null
    }

    /* ========== StateHost implementation ========== */

    private var _data: Bundle? = null

    override final val state: Bundle
        get() = _data ?: lifecycleError("Accessing state on a component which does not have any.")

    override final val isBound get() = _data != null

    override final val stateBound: ObservableBinder = ObservableBinder()

    override final fun bindState(state: Bundle): Bundle? {
        val old = _data
        if (old != null) beforeDataRebind()
        onDataBind(state)
        if (_data !== state) lifecycleError("super.onDataBind not called properly in [$this].")
        return old
    }

    override final fun resetState(): Bundle? {
        val old = _data
        onDataReset()
        if (isBound) lifecycleError("super.onDataReset not called properly in [$this].")
        return old
    }

    /**
     * Use this method to react to data bundle changes.
     *
     * Before you call super.onDataBind, the old data bundle is still available,
     * so that you can distinguish fresh binds and rebinds.
     */
    @CallSuper
    protected open fun onDataBind(data: Bundle) {
        _data = data
        if (!stateBound.isActive) stateBound.performStart()
    }

    /**
     * Use this method to react to data bundle resets.
     */
    @CallSuper
    protected open fun onDataReset() {
        stateBound.performStop()
        _data = null
    }

    /**
     * Use this method to prepare this component for [state] swap.
     */
    @CallSuper
    protected open fun beforeDataRebind() { }

    /* ========== Attach/Detach mechanism ========== */

    /**
     * Monitors whether the component is attached to the main hierarchy.
     *
     * Note that this binder does not monitor what the parent of this component is.
     * Meaning that the component can change location in the tree, assuming it is done
     * correctly.
     */
    val attached: ObservableBinder = ObservableBinder()

    /**
     * Check whether this component is attached to the main hierarchy.
     */
    val isAttached: Boolean
        get() = attached.isActive


    private var parent: Presenter? = null

    /**
     * Attach this component to a given [Presenter] at the specified location.
     *
     * If this component is already attached to some other parent, it will be moved to the new
     * location WITHOUT detaching.
     *
     * Note that it is the responsibility of the parent to update the [Lifecycle] of this
     * component, if it has any.
     */
    open fun attachTo(parent: Presenter, location: InsertionPoint) {
        this.parent?.let { oldParent ->
            // swap parents and move the view
            (view.parent as ViewGroup).removeView(view)
            location.invoke(parent.view as ViewGroup, this)
            //TODO Check if the previous code succeeded
            if (oldParent != parent) {
                this.parent = parent
                oldParent.beforeChildDetach(this)
                oldParent.removeChild(this)
                parent.addChild(this)
                parent.afterChildAttach(this)
            }
        } ?: run {
            // add parent and call onAttach
            location.invoke(parent.view as ViewGroup, this)
            this.parent = parent
            onAttach()
            parent.addChild(this)
            parent.afterChildAttach(this)
        }
    }

    /**
     * Detach this component from the current parent.
     *
     * If component is not attached, [LifecycleException] is thrown.
     */
    open fun detach() {
        this.parent?.let { oldParent ->
            (view.parent as ViewGroup).removeView(view)
            oldParent.beforeChildDetach(this)
            oldParent.removeChild(this)
            onDetach()
            this.parent = null
        } ?: lifecycleError("Cannot detach. The component has no parent.")
    }

    /**
     * The holder is connected to the view hierarchy before onAttach is called.
     * After onAttach returns, the component is attached.
     */
    protected open fun onAttach() {
        if (!isAttached) attached.performStart()
    }

    /**
     * After onDetach returns, this component is detached and can be safely removed
     * from the view hierarchy.
     */
    protected open fun onDetach() {
        attached.performStop()
    }

    /* ========== State preservation ========== */

    open fun saveInstanceState(): Bundle? {
        return _data?.let { data ->
            Bundle().apply { putAll(data) }
        }
    }

    open fun saveHierarchyState(container: SparseArray<Bundle>): Bundle? {
        val state = saveInstanceState()
        if (state != null && id != View.NO_ID) {
            container.append(id, state)
        }
        return state
    }

    /* ========== Configuration changes ========== */

    open fun performConfigurationChange() {

    }

}