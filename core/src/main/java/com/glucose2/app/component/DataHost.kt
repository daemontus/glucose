package com.glucose2.app.component

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
interface DataHost {

    /**
     * Tha data bundle of this object. Only accessible when [isBound] is true.
     *
     * Use [State], [StateOptional] and [StateNative] to access data in the bundle.
     */
    val data: Bundle

    /**
     * Indicates whether this [DataHost] is bound to a data bundle.
     */
    val isBound: Boolean

    /**
     * [ObservableBinder] monitoring the changes of the [isBound] state.
     *
     * Note that this includes rebounds. So the observables associated with
     * [dataBound] will be terminated every time the data object changes.
     */
    val dataBound: ObservableBinder

    /**
     * Bind new [data] [Bundle] to this object. If some data Bundle is already
     * bound to it, it will be unbound and returned. Otherwise, this method returns null.
     */
    fun bindData(data: Bundle): Bundle?

    /**
     * Remove the [data] [Bundle] from this object and return it.
     *
     * If no data is bound, this method will return null.
     */
    fun resetData(): Bundle?

}