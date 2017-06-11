package com.glucose2.state

import com.glucose2.bundle.BundlerObject
import kotlin.properties.ReadOnlyProperty
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

class State<T : Any>(
        private val bundler: BundlerObject<T?>
) : ReadWriteProperty<StateHost, T>, ReadOnlyProperty<StateHost, T> {

    override fun setValue(thisRef: StateHost, property: KProperty<*>, value: T)
            = bundler.setter(thisRef.state, property.name, value)

    override fun getValue(thisRef: StateHost, property: KProperty<*>): T
            = bundler.getter(thisRef.state, property.name) ?:
            throw NullPointerException("Missing required state for ${property.name}")

}