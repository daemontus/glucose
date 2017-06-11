package com.glucose2.app

import android.os.Bundle
import android.support.annotation.CallSuper
import android.view.View
import com.glucose2.app.event.EventHost
import com.glucose2.app.event.EventHostDelegate
import com.glucose2.rx.ObservableBinder
import rx.Subscription

abstract class AbstractComponent internal constructor(
        override final val view: View,
        override final val host: ComponentHost,
        private val eventHostDelegate: EventHostDelegate = EventHostDelegate()
) : Component, EventHost by eventHostDelegate {

    override final val id: Int by ID_DELEGATE
    override final val preserveState: Boolean by TRUE_DELEGATE
    override final val preservePosition: Boolean by TRUE_DELEGATE
    override final val surviveConfigChange: Boolean by TRUE_DELEGATE

    override final val alive: ObservableBinder = ObservableBinder().apply { this.performStart() }
    override final val isAlive: Boolean = alive.isActive

    @CallSuper
    protected open fun onDestroy() {
        eventHostDelegate.performDestroy()
        alive.performStop()
        alive.performDestroy()
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

    /* ========== Attach mechanism ========== */

    private var eventSubscription: Subscription? = null

    /**
     * [ObservableBinder] monitoring the changes of the [isAttached] state.
     *
     * Note that this does not include reattaches! So the observables associated with
     * [isAttached] will be terminated only when the is no longer part of the tree,
     * not every time it moves.
     */
    override final val attached: ObservableBinder = ObservableBinder()

    /**
     * Indicates this component is connected to the component tree (the [EventHost] is fully
     * operational, the view is part of the main hierarchy, etc.)
     */
    override final val isAttached: Boolean = attached.isActive

    /**
     * If you are reattaching, make sure to decrease lifecycle before and increase after
     * the operation.
     *
     * Example: If the new location is started and this component is resumed, pause it before
     * moving. If the new location is started and this component is stopped, start it after
     * moving.
     */
    internal open fun performAttach(parent: AbstractComponent) {
        if (isAttached) lifecycleError("Component [$this] is already attached.")
        eventSubscription?.unsubscribe()
        eventSubscription = eventHostDelegate.onAttach(parent.eventHostDelegate)
        onAttach()
        if (!isAttached) lifecycleError("Super.onAttach hasn't been called properly in [$this].")
    }

    /**
     * Make sure to decrease lifecycle before detaching.
     */
    internal open fun performDetach() {
        if (!isAttached) lifecycleError("Component [$this] is not attached to anything.")
        onDetach()
        eventSubscription?.unsubscribe()
        if (isAttached) lifecycleError("Super.onDetach hasn't been called properly in [$this].")
    }

    /**
     * The holder is connected to the view hierarchy before onAttach is called.
     * After super.onAttach returns, the component is attached.
     */
    protected open fun onAttach() {
        if (!isAttached) attached.performStart()
    }

    /**
     * After super.onDetach returns, this component is detached and can be safely removed
     * from the view hierarchy.
     */
    protected open fun onDetach() {
        attached.performStop()
    }

    /**
     * Use this method to prepare this component for a parent swap.
     */
    protected open fun beforeReattach() { }
}