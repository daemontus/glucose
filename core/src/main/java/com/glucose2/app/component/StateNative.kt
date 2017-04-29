package com.glucose2.app.component

import com.glucose2.bundle.BundlerNative
import kotlin.properties.ReadOnlyProperty
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

class StateNative<T>(
        private val default: T,
        private val bundler: BundlerNative<T>
) : ReadWriteProperty<DataHost, T>, ReadOnlyProperty<DataHost, T> {
    override fun getValue(thisRef: DataHost, property: KProperty<*>): T
            = bundler.getter(thisRef.data, property.name, default)

    override fun setValue(thisRef: DataHost, property: KProperty<*>, value: T)
            = bundler.setter(thisRef.data, property.name, value)

}
