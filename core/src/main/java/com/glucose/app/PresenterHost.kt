package com.glucose.app

import android.app.Activity
import android.os.Bundle
import android.util.SparseArray
import android.view.ViewGroup

/**
 * Implementation of [PresenterHost] represents a context in which presenters can be created
 * using a [PresenterFactory] and attached to the presenter hierarchy.
 *
 * The responsibility of [PresenterHost] is to notify the [PresenterFactory] and the
 * presenters in the hierarchy about lifecycle of [Activity] and preserving
 * the state information when activity is destroyed and during config changes.
 *
 * Note: For most applications, all you need is a [PresenterDelegate] or a [RootActivity].
 *
 * @see PresenterDelegate
 * @see PresenterFactory
 */
interface PresenterHost {

    /**
     * Access to the activity that governs the lifecycle of this PresenterHost.
     */
    val activity: Activity

    /**
     * Provides a way to create and cache presenters.
     */
    val factory: PresenterFactory

    /**
     * The root of the presenter hierarchy managed by this host.
     *
     * Note: It is left to the host to decide how to initialize and update the root
     * during config changes, therefore no specific interface for this is provided now.
     *
     * General rule is that the root shouldn't be null as long as the state is fully restored and
     * the overall context isn't destroyed.
     */
    val root: Presenter?

    /**
     * Obtain a new presenter from the factory and attach it to this host with given arguments.
     *
     * The method only attaches the arguments to the presenter, but does not insert it into the
     * view hierarchy. (parent parameter is for inflation purposes only) See [PresenterGroup]
     * if you want to also insert the presenter into the view hierarchy.
     */
    fun <P: Presenter> attach(clazz: Class<P>, arguments: Bundle = Bundle(), parent: ViewGroup? = null): P

    /**
     * Obtain a new presenter from the factory and attach it to this host with given arguments
     * while maintaining the given state information about the hierarchy.
     *
     * This method is mainly used to restore the presenter hierarchy after config change or
     * activity destroy. Here, the savedState parameter represents an id<->state data
     * for each presenter that was present in the hierarchy before (this is required
     * to preserve the state of presenters that changed position in the hierarchy
     * during the operation).
     */
    fun <P: Presenter> attachWithState(
            clazz: Class<P>, savedState: SparseArray<Bundle>,
            arguments: Bundle = Bundle(), parent: ViewGroup? = null
    ) : P

    /**
     * Detach given presenter and recycle it back in the factory.
     */
    fun detach(presenter: Presenter)
}
