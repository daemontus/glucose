package com.glucose.app.presenter

import android.os.Bundle
import android.os.IBinder
import android.os.Parcelable
import android.util.Size
import android.util.SizeF
import android.view.View
import com.glucose.app.Presenter
import java.io.Serializable
import java.util.*
import kotlin.properties.ReadOnlyProperty
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

internal fun Bundle.getId() = this.getInt(Presenter::id.name, View.NO_ID)
fun Bundle.isRestored() = this.getBoolean(Presenter.IS_RESTORED_KEY, false)
fun Bundle.setRestored(restored: Boolean) = this.putBoolean(Presenter.IS_RESTORED_KEY, restored)
fun Bundle.isFresh() = !this.isRestored()

//Note: Presenter state is checked when accessing arguments, so whatever we do with them here is safe.

class NativeArgument<out T>(
        private val default: T,
        private val bundler: NativeBundler<T>
) : ReadOnlyProperty<Presenter, T> {
    override fun getValue(thisRef: Presenter, property: KProperty<*>): T
            = bundler.getter(thisRef.arguments, property.name, default)
}

class Argument<out T>(
        private val bundler: ObjectBundler<T>
) : ReadOnlyProperty<Presenter, T> {

    override fun getValue(thisRef: Presenter, property: KProperty<*>): T
            = bundler.getter(thisRef.arguments, property.name)

}

class RequiredArgument<out T>(
        private val bundler: ObjectBundler<T?>
) : ReadOnlyProperty<Presenter, T> {
    override fun getValue(thisRef: Presenter, property: KProperty<*>): T
            = bundler.getter(thisRef.arguments, property.name) ?:
            throw KotlinNullPointerException("Missing required argument for ${property.name}")
}

class NativeState<T>(
        private val default: T,
        private val bundler: NativeBundler<T>
) : ReadWriteProperty<Presenter, T> {
    override fun getValue(thisRef: Presenter, property: KProperty<*>): T
            = bundler.getter(thisRef.arguments, property.name, default)

    override fun setValue(thisRef: Presenter, property: KProperty<*>, value: T)
            = bundler.setter(thisRef.arguments, property.name, value)

}

class State<T>(
        private val bundler: ObjectBundler<T>
) : ReadWriteProperty<Presenter, T> {

    override fun setValue(thisRef: Presenter, property: KProperty<*>, value: T)
            = bundler.setter(thisRef.arguments, property.name, value)

    override fun getValue(thisRef: Presenter, property: KProperty<*>): T
            = bundler.getter(thisRef.arguments, property.name)

}

class RequiredState<T>(
        private val bundler: ObjectBundler<T?>
) : ReadWriteProperty<Presenter, T> {
    override fun setValue(thisRef: Presenter, property: KProperty<*>, value: T)
            = bundler.setter(thisRef.arguments, property.name, value)

    override fun getValue(thisRef: Presenter, property: KProperty<*>): T
            = bundler.getter(thisRef.arguments, property.name) ?:
            throw KotlinNullPointerException("Missing required state for ${property.name}")

}

private inline fun <R> checkAttached(presenter: Presenter, name: String, action: () -> R): R
        = if (presenter.isAttached) action() else {
            throw LifecycleException("Accessing argument $name on a detached Presenter.")
        }