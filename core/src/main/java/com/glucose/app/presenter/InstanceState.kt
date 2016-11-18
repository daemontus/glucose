package com.glucose.app.presenter

import android.os.Bundle
import android.view.View
import com.glucose.app.Presenter
import kotlin.properties.ReadOnlyProperty
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

internal fun Bundle.getId() = this.getInt(Presenter::id.name, View.NO_ID)
fun Bundle.isRestored() = this.getBoolean(Presenter.IS_RESTORED_KEY, false)
fun Bundle.setRestored(restored: Boolean) = this.putBoolean(Presenter.IS_RESTORED_KEY, restored)
fun Bundle.isFresh() = !this.isRestored()

//Note: Presenter state is checked when accessing instanceState, so whatever we do with them here is safe.
//Note: State and Argument don't check for value presence on attach, but on first access.
//This is mainly because we don't have the property name until it is first accessed.

class NativeState<T>(
        private val default: T,
        private val bundler: NativeBundler<T>
) : ReadWriteProperty<Presenter, T>, ReadOnlyProperty<Presenter, T> {
    override fun getValue(thisRef: Presenter, property: KProperty<*>): T
            = bundler.getter(thisRef.instanceState, property.name, default)

    override fun setValue(thisRef: Presenter, property: KProperty<*>, value: T)
            = bundler.setter(thisRef.instanceState, property.name, value)

}

class OptionalState<T>(
        private val bundler: ObjectBundler<T?>
) : ReadWriteProperty<Presenter, T?>, ReadOnlyProperty<Presenter, T?> {

    override fun setValue(thisRef: Presenter, property: KProperty<*>, value: T?)
            = bundler.setter(thisRef.instanceState, property.name, value)

    override fun getValue(thisRef: Presenter, property: KProperty<*>): T?
            = bundler.getter(thisRef.instanceState, property.name)

}

class State<T>(
        private val bundler: ObjectBundler<T?>
) : ReadWriteProperty<Presenter, T>, ReadOnlyProperty<Presenter, T> {

    override fun setValue(thisRef: Presenter, property: KProperty<*>, value: T)
            = bundler.setter(thisRef.instanceState, property.name, value)

    override fun getValue(thisRef: Presenter, property: KProperty<*>): T
            = bundler.getter(thisRef.instanceState, property.name) ?:
            throw NullPointerException("Missing required state for ${property.name}")

}