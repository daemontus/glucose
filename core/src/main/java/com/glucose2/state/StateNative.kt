package com.glucose2.state

import com.glucose2.bundle.BundlerNative
import kotlin.properties.ReadOnlyProperty
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

class StateNative<T>(
        private val default: T,
        private val bundler: BundlerNative<T>
) : ReadWriteProperty<StateHost, T>, ReadOnlyProperty<StateHost, T> {

    override fun getValue(thisRef: StateHost, property: KProperty<*>): T
            = bundler.getter(thisRef.state, property.name, default)

    override fun setValue(thisRef: StateHost, property: KProperty<*>, value: T)
            = bundler.setter(thisRef.state, property.name, value)

}
