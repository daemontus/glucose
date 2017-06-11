package com.glucose2.state


class StateOptional<T : Any?>(
        private val bundler: com.glucose2.bundle.BundlerObject<T?>
) : kotlin.properties.ReadWriteProperty<StateHost, T?>, kotlin.properties.ReadOnlyProperty<StateHost, T?> {

    override fun setValue(thisRef: StateHost, property: kotlin.reflect.KProperty<*>, value: T?)
            = bundler.setter(thisRef.state, property.name, value)

    override fun getValue(thisRef: StateHost, property: kotlin.reflect.KProperty<*>): T?
            = bundler.getter(thisRef.state, property.name)

}
