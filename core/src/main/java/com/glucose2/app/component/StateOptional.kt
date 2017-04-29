package com.glucose2.app.component

import com.glucose2.bundle.BundlerObject
import kotlin.properties.ReadOnlyProperty
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty


class StateOptional<T : Any?>(
        private val bundler: BundlerObject<T?>
) : ReadWriteProperty<DataHost, T?>, ReadOnlyProperty<DataHost, T?> {

    override fun setValue(thisRef: DataHost, property: KProperty<*>, value: T?)
            = bundler.setter(thisRef.data, property.name, value)

    override fun getValue(thisRef: DataHost, property: KProperty<*>): T?
            = bundler.getter(thisRef.data, property.name)

}
