package com.glucose2.app

import android.content.res.Configuration
import android.os.Bundle
import android.support.annotation.CallSuper
import com.glucose2.state.StateHost

/**
 * ComponentGroup manages a set of child components, controlling their state
 * based on the notifications from the associated [Presenter].
 *
 * It is the responsibility of the group to honor the lifecycle invariants
 * (state of the parent should be higher or equal to that of a child) and
 * the configuration change (restart children that have [Component.canChangeConfiguration]
 * set to false).
 *
 * However, aside from this, the component group is free to perform any other
 * actions with regards to the components.
 *
 * For example, the group might implement a stack, which automatically detaches
 * components which are no longer visible.
 *
 * Or, it can rely on some external data source (view pager callback f.e.), and
 * based on this pause/stop components which are not currently on screen.
 *
 * Another example would be a simple singleton component, which always holds exactly
 * one component, dropping the last component when new one comes around.
 *
 * Finally, each component group has a generic argument [IP] - an insertion point.
 *
 * The insertion point is then used by the group user to declare where the child should
 * be inserted. Often, the group has just one insertion point (back stack, singleton).
 *
 * However, one can easily imagine more complex groups, where an exact position is
 * needed.
 */
abstract class ComponentGroup<in IP> internal constructor(
        private val host: Parent
) : StateHost {

    internal interface Parent {
        val factory: ComponentFactory
        fun registerChild(child: Component)
        fun unregisterChild(child: Component)
    }

    constructor(host: Presenter) : this(wrapForGroup(host))

    private var _state: Bundle? = null
    override final val state: Bundle
        get() = _state ?: lifecycleError("ComponentGroup $this currently has no state.")

    /**
     * Called when the parent [Presenter] is attached to a state bundle.
     *
     * The [state] is not the whole presenter state, but rather a group-specific bundle.
     */
    @CallSuper
    open fun attach(state: Bundle) {
        _state = state
    }

    /**
     * Called when the parent [Presenter] is detached.
     *
     * The call should return the latest [state] of the group.
     */
    @CallSuper
    open fun detach(): Bundle {
        val state = saveInstanceState()
        _state = null
        return state
    }

    /**
     * Save the [state] and return it.
     */
    open fun saveInstanceState(): Bundle {
        return Bundle().apply { putAll(state) }
    }

    /**
     * Called to add a new child to this group.
     *
     * Note that the child does not know it is attached yet.
     *
     * If it is being reattached, you can decrease the lifecycle here.
     */
    @CallSuper
    open fun addChild(child: Component, location: IP) {
        host.registerChild(child)
    }

    /**
     * Called after child has been successfully attached.
     *
     * This is a good place to start/resume the child to sync it with the parent presenter.
     */
    @CallSuper
    open fun attachChild(child: Component) = Unit

    /**
     * Called before the child is actually detached.
     *
     * You can prepare the group for this (for example by attaching previously detach component)
     *
     * Don't decrease the lifecycle of the child here. It might be just reattaching, in which case,
     * it is the responsibility of the new parent.
     */
    @CallSuper
    open fun detachChild(child: Component) = Unit

    /**
     * Call to remove the child from this group.
     *
     * Don't decrease the lifecycle of the child here. If might be just reattaching.
     */
    @CallSuper
    open fun removeChild(child: Component) {
        host.unregisterChild(child)
    }

    /**
     * Called upon each configuration change. Make sure you restart all components with
     * [Component.canChangeConfiguration] set to false here.
     */
    @CallSuper
    open fun configurationChange(newConfig: Configuration) = Unit

    /**
     * Called when the parent presenter becomes started.
     */
    @CallSuper
    open fun start() = Unit

    /**
     * Called when the parent presenter becomes resumed.
     */
    @CallSuper
    open fun resume() = Unit

    /**
     * Called when the parent presenter becomes paused.
     */
    @CallSuper
    open fun pause() = Unit

    /**
     * Called when the parent presenter becomes stopped.
     */
    @CallSuper
    open fun stop() = Unit

}