package com.glucose2.app

import android.view.View
import com.glucose2.app.transaction.TransactionHost
import com.glucose2.rx.ObservableBinder
import com.glucose2.state.StateHost
import com.glucose2.view.ViewHost

/**
 * Component defines common functionality provided
 * by both [Holder] and [Presenter].
 */
interface Component : TransactionHost, StateHost, ViewHost {

    /**
     * Unique integer identifier of this object. Default: [View.NO_ID].
     *
     * Used when searching/saving/restoring the component tree.
     */
    val id: Int

    /**
     * Save the tree state of this component if possible. Default: true.
     *
     * Note that you don't always need a valid [id], assuming the position
     * of the object in the component tree is uniquely identifiable.
     */
    val preserveState: Boolean

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
    val preservePosition: Boolean

    /**
     * Don't recreate this component when configuration changes. Default: true.
     *
     * If false, this component together with all its children will be recreated on each
     * configuration change.
     */
    val surviveConfigChange: Boolean

    /**
     * Monitors the main lifecycle of the component - that is, observables associated with
     * this binder will be unsubscribed once the component is destroyed.
     */
    val alive: ObservableBinder

    val isAlive: Boolean

    val host: ComponentHost

    val isAttached: Boolean
    val attached: ObservableBinder

}