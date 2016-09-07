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

class Argument<out T>(
        private val bundler: Bundler<T>
) : ReadOnlyProperty<Presenter, T> {

    override fun getValue(thisRef: Presenter, property: KProperty<*>): T
            = bundler.getter.invoke(thisRef.arguments, property.name)

}

class RequiredArgument<out T>(
        private val bundler: Bundler<T?>
) : ReadOnlyProperty<Presenter, T> {
    override fun getValue(thisRef: Presenter, property: KProperty<*>): T
            = bundler.getter.invoke(thisRef.arguments, property.name) ?:
            throw KotlinNullPointerException("Missing required argument for ${property.name}")
}

class State<T>(
        private val bundler: Bundler<T>
) : ReadWriteProperty<Presenter, T> {

    override fun setValue(thisRef: Presenter, property: KProperty<*>, value: T)
            = bundler.setter.invoke(thisRef.arguments, property.name, value)

    override fun getValue(thisRef: Presenter, property: KProperty<*>): T
            = bundler.getter.invoke(thisRef.arguments, property.name)

}

class RequiredState<T>(
        private val bundler: Bundler<T?>
) : ReadWriteProperty<Presenter, T> {
    override fun setValue(thisRef: Presenter, property: KProperty<*>, value: T)
            = bundler.setter.invoke(thisRef.arguments, property.name, value)

    override fun getValue(thisRef: Presenter, property: KProperty<*>): T
            = bundler.getter.invoke(thisRef.arguments, property.name) ?:
            throw KotlinNullPointerException("Missing required state for ${property.name}")

}