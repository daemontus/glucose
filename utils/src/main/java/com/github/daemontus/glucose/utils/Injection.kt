package com.github.daemontus.glucose.utils

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.View
import java.util.*
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

@Suppress("UNCHECKED_CAST")
class InjectibleFragment() : Fragment() {

    private val propertyMap = HashMap<Int, ViewProperty<*>>()

    protected fun <T: View> bindView(id: Int): ReadOnlyProperty<InjectibleFragment, T> = ViewProperty(id)

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        for ((id, prop) in propertyMap) {
            (prop as ViewProperty<View>).view = view?.findViewById(id)
        }
    }

    @Suppress("UNCHECKED_CAST")
    private inner class ViewProperty<V: View>(
            private val id: Int
    ) : ReadOnlyProperty<InjectibleFragment, V> {

        var view: V? = null

        init {
            propertyMap[id] = this
            this.view = this@InjectibleFragment.view?.findViewById(id) as V?
        }

        override fun getValue(thisRef: InjectibleFragment, property: KProperty<*>): V = view!!

    }
}