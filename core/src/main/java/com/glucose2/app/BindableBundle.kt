package com.glucose2.app

import android.os.Bundle
import com.glucose.app.Presenter
import com.glucose.app.presenter.NativeBundler
import com.glucose.app.presenter.ObjectBundler
import kotlin.properties.ReadOnlyProperty
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty


class NativeState<T>(
        private val default: T,
        private val bundler: NativeBundler<T>
) : ReadWriteProperty<Bindable<Bundle>, T>, ReadOnlyProperty<Bindable<Bundle>, T> {

    override fun getValue(thisRef: Bindable<Bundle>, property: KProperty<*>): T
            = bundler.getter(thisRef.state, property.name, default)

    override fun setValue(thisRef: Bindable<Bundle>, property: KProperty<*>, value: T)
            = bundler.setter(thisRef.state, property.name, value)

}

class OptionalState<T>(
        private val bundler: ObjectBundler<T?>
) : ReadWriteProperty<Bindable<Bundle>, T?>, ReadOnlyProperty<Bindable<Bundle>, T?> {

    override fun setValue(thisRef: Bindable<Bundle>, property: KProperty<*>, value: T?)
            = bundler.setter(thisRef.state, property.name, value)

    override fun getValue(thisRef: Bindable<Bundle>, property: KProperty<*>): T?
            = bundler.getter(thisRef.state, property.name)

}

class State<T>(
        private val bundler: ObjectBundler<T?>
) : ReadWriteProperty<Bindable<Bundle>, T>, ReadOnlyProperty<Bindable<Bundle>, T> {

    override fun setValue(thisRef: Bindable<Bundle>, property: KProperty<*>, value: T)
            = bundler.setter(thisRef.state, property.name, value)

    override fun getValue(thisRef: Bindable<Bundle>, property: KProperty<*>): T
            = bundler.getter(thisRef.state, property.name) ?:
            throw NullPointerException("Missing required state for ${property.name}")

}