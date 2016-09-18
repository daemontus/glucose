package com.glucose.app.presenter

import android.app.Activity
import android.app.Application
import com.glucose.app.Presenter
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

/**
 * A shortcut to access the [Presenter] activity assuming it has a specific type.
 *
 * @see [ParentActivity]
 */
inline fun <reified A: Activity> Presenter.getActivity(): A {
    return this.host.activity as A
}

/**
 * A simple wrapper property that casts activity from context to a given type.
 * Use this if you need to access the parent activity often, so that [Presenter.getActivity]
 * becomes too verbose.
 */
class ParentActivity<out A: Activity>(
        private val clazz: Class<A>
) : ReadOnlyProperty<Presenter, A> {
    override fun getValue(thisRef: Presenter, property: KProperty<*>): A {
        return clazz.cast(thisRef.host.activity)
    }
}

/**
 * A shortcut to access the [Presenter] application assuming it has a specific type.
 *
 * @see [ParentApp]
 */
inline fun <reified A: Application> Presenter.getApp(): A {
    return this.host.activity.application as A
}

/**
 * A simple wrapper property that casts application from context to a given type.
 * Use this if you need to access the parent application often, so that [Presenter.getApp]
 * becomes too verbose.
 */
class ParentApp<out A: Application>(
        private val clazz: Class<A>
) : ReadOnlyProperty<Presenter, A> {
    override fun getValue(thisRef: Presenter, property: KProperty<*>): A {
        return clazz.cast(thisRef.host.activity.application)
    }
}