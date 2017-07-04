package com.glucose2.state

import android.os.Bundle

/**
 * Abstraction of types that can be bound with data bundles.
 *
 * The data bundle holds the state of the object. To keep the bundle
 * up-to-date, use [State], [StateOptional] or [StateNative] delegates
 * to access the data inside the bundle.
 *
 * Typically, an implementation of data host will provide some attach/detach
 * methods that you can use to react to new data.
 */
interface StateHost {

    /**
     * Tha data bundle of this object. Only accessible when the data bundle is bound.
     *
     * Use [State], [StateOptional] and [StateNative] to access data in the bundle.
     */
    val state: Bundle

}