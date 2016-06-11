package com.github.daemontus.glucose.blueprints.component

import android.app.Activity
import android.app.Application
import android.os.Bundle
import java.util.*

interface ComponentRoot {
    val activity: Activity
    val app: Application
}

open class Component<Root: ComponentRoot>(
        root: Root
) {

    val root = root
        get() {
            if (destroyed) {
                throw IllegalStateException("Running context based operation on a destroyed component!")
            }
            return field
        }

    var destroyed = false
        private set

    var attached = false
        private set

    private val childComponents = HashMap<String, Component<out Root>>()

    private var lastState = Bundle()

    open fun onAttach(state: Bundle) {
        lastState = state
        attached = true
        childComponents.forEach {
            it.value.onAttach(state.getBundle(it.key))
        }
    }

    open fun onDetach() {
        childComponents.forEach {
            it.value.onDetach()
        }
        attached = false
    }

    open fun onDestroy() {
        childComponents.forEach {
            it.value.onDestroy()
        }
        destroyed = true
    }

    open fun saveState(): Bundle {
        return Bundle()
    }

    open fun addChild(key: String, component: Component<out Root>) {
        if (key in childComponents) {
            throw IllegalStateException("Component $this already has a child with key $key")
        }
        childComponents[key] = component
        if (attached) {
            component.onAttach(lastState)
        }
    }

    //it's a nested class so that we can safely access internal structures
    /*abstract class State<T: Any>(
            protected val component: Component<*,*>,
            protected  val key: String,
            protected val defaultValue: T
    ) : ReadWriteProperty<Component<*,*>, T> {

        internal var value: T = component.latestState.run { bundleGet(this) } ?: defaultValue

        init {
            component.stateProperties.add(this)
        }

        override fun getValue(thisRef: Component<*,*>, property: KProperty<*>): T = value

        override fun setValue(thisRef: Component<*,*>, property: KProperty<*>, value: T) {
            this.value = value
        }

        abstract fun bundleGet(bundle: Bundle): T?
        abstract fun bundleSet(bundle: Bundle): Unit

    }*/

}
