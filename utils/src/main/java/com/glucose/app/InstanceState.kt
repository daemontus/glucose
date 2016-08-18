package com.glucose.app

import android.os.Bundle
import kotlin.properties.ReadOnlyProperty
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

class InstanceArgument<out T>(
        private val getter: Bundle.(String) -> T
) : ReadOnlyProperty<Presenter, T> {

    override fun getValue(thisRef: Presenter, property: KProperty<*>): T
            = getter.invoke(thisRef.arguments, property.name)

}

class InstanceState<T>(
        private val getter: Bundle.(String) -> T,
        private val setter: Bundle.(String, T) -> Unit
) : ReadWriteProperty<Presenter, T> {

    override fun setValue(thisRef: Presenter, property: KProperty<*>, value: T)
            = setter.invoke(thisRef.arguments, property.name, value)

    override fun getValue(thisRef: Presenter, property: KProperty<*>): T
            = getter.invoke(thisRef.arguments, property.name)

}