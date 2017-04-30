package com.glucose2.app.component

import com.glucose2.rx.ObservableBinder

/**
 * Abstraction of types which provide access to the lifecycle similar to the one of an Activity.
 * By default, the LifecycleHost should mirror the underlying Activity. However,
 * not all components have to strictly follow this. For example, you can have an adapter
 * that will stop items that aren't currently visible but not detach them.
 *
 * However, under no circumstances should the state of the Activity be "smaller"
 * than that of the LifecycleHost (can't have a resumed LifecycleHost in a
 * stopped Activity for example). This implies that all lifecycle changes
 * should be synchronous and performed on main thread. So when the onPause
 * of the underlying activity shouldn't return unless all associated lifecycle hosts
 * are paused.
 *
 * Also note that the state indicators work as a hierarchy. That is, whenever [isStarted] is true,
 * [isAttached] must be also true, since you can't start a component which isn't attached and so on.
 *
 * The implementations of this interface will usually provide methods such as
 * onAttach, onStart or onResume so that you can react to the lifecycle changes.
 */
interface LifecycleHost {

    /**
     * Indicates that this host is destroyed and can't be used again.
     */
    val isDestroyed: Boolean

    /**
     * Indicates that this host hasn't been destroyed yet.
     */
    val isAlive: Boolean

    /**
     * [ObservableBinder] monitoring the changes of the [isAlive] state.
     */
    val alive: ObservableBinder

    /**
     * Indicates that this host has been attached to the main hierarchy.
     */
    val isAttached: Boolean

    /**
     * [ObservableBinder] monitoring the changes of the [isAttached] state.
     */
    val attached: ObservableBinder

    /**
     * Indicates that the host is part of a started Activity and should be started too.
     */
    val isStarted: Boolean

    /**
     * [ObservableBinder] monitoring the changes of the [isStarted] state.
     */
    val started: ObservableBinder

    /**
     * Indicates that the host is part of a resumed Activity and should be resumed too.
     */
    val isResumed: Boolean

    /**
     * [ObservableBinder] monitoring the changes of the [isResumed] state.
     */
    val resumed: ObservableBinder

}