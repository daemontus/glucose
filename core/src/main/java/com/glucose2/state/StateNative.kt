package com.glucose2.state

class StateNative<T>(
        private val default: T,
        private val bundler: com.glucose2.bundle.BundlerNative<T>
) : kotlin.properties.ReadWriteProperty<StateHost, T>, kotlin.properties.ReadOnlyProperty<StateHost, T> {
    override fun getValue(thisRef: StateHost, property: kotlin.reflect.KProperty<*>): T
            = bundler.getter(thisRef.state, property.name, default)

    override fun setValue(thisRef: StateHost, property: kotlin.reflect.KProperty<*>, value: T)
            = bundler.setter(thisRef.state, property.name, value)

}
