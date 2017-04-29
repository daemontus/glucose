package com.glucose2.app.component

import android.os.Bundle
import com.glucose2.rx.ObservableBinder

/**
 * Abstraction of types that can be bound with data bundles.
 *
 * The data bundle holds the state of the object. To keep the bundle
 * up-to-date, use [State], [StateOptional] or [StateNative] delegates
 * to access the data inside the bundle.
 */
interface DataHost {

    val data: Bundle
    val isBound: Boolean
    val dataBound: ObservableBinder

    fun bindData(data: Bundle): Bundle?
    fun unbindData(): Bundle

}