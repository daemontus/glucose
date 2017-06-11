package com.glucose2.state

import android.os.Bundle
import com.glucose2.rx.ObservableBinder

/**
 * Abstraction of types that can be bound with data bundles.
 *
 * The data bundle holds the state of the object. To keep the bundle
 * up-to-date, use [State], [StateOptional] or [StateNative] delegates
 * to access the data inside the bundle.
 *
 * Typically, an implementation of data host will provide a onBind and onReset
 * methods that you can use to react to new data.
 */
interface StateHost {

    /**
     * Tha data bundle of this object. Only accessible when [isBound] is true.
     *
     * Use [State], [StateOptional] and [StateNative] to access data in the bundle.
     */
    val state: Bundle

    /**
     * Indicates whether this [StateHost] is bound to a data bundle.
     */
    val isBound: Boolean

    /**
     * [ObservableBinder] monitoring the changes of the [isBound] state.
     *
     * Note that this does not include rebounds! So the observables associated with
     * [stateBound] will be terminated only when the data object is no longer available,
     * not every time it changes.
     */
    val stateBound: ObservableBinder

    /**
     * Bind new [state] [Bundle] to this object. If some data Bundle is already
     * bound to it, it will be unbound and returned. Otherwise, this method returns null.
     */
    fun bindState(state: Bundle): Bundle?

    /**
     * Remove the [state] [Bundle] from this object and return it.
     *
     * If no data is bound, this method will return null.
     */
    fun resetState(): Bundle?

}