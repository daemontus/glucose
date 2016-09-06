package com.glucose.app.presenter

import android.os.Bundle
import android.os.Parcelable
import android.view.View
import com.glucose.app.Presenter
import kotlin.properties.ReadOnlyProperty
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

fun Bundle.getId() = this.getInt(Presenter::id.name, View.NO_ID)
fun Bundle.isRestored() = this.getBoolean(Presenter.IS_RESTORED_KEY, false)
fun Bundle.setId(id: Int) = this.putInt(Presenter::id.name, id)
fun Bundle.setRestored(restored: Boolean) = this.putBoolean(Presenter.IS_RESTORED_KEY, restored)

class InstanceArgument<out T>(
        private val bundler: Bundler<T>
) : ReadOnlyProperty<Presenter, T> {

    override fun getValue(thisRef: Presenter, property: KProperty<*>): T
            = bundler.getter.invoke(thisRef.arguments, property.name)

}

class InstanceState<T>(
        private val bundler: Bundler<T>
) : ReadWriteProperty<Presenter, T> {

    override fun setValue(thisRef: Presenter, property: KProperty<*>, value: T)
            = bundler.setter.invoke(thisRef.arguments, property.name, value)

    override fun getValue(thisRef: Presenter, property: KProperty<*>): T
            = bundler.getter.invoke(thisRef.arguments, property.name)

}

inline private fun <T> getWithError(crossinline getter: Bundle.(String) -> T?): (Bundle.(String) -> T) {
    return {
       this.getter(it) ?: throw NullPointerException("Missing argument $it")
    }
}

//TODO: Add more argument and state delegates (optional, default) - ideally auto-generated
//TODO: Refactor delegates into multiple modules to reduce method count (array, list, primitive...)

/* ====== Argument (Read only) delegates ====== */

//Parcelable
fun <P: Parcelable> parcelableArgument() = InstanceArgument<P>(getWithError {
    this.getParcelable<P>(it)
})

//String
fun stringArgument() = InstanceArgument<String>(getWithError(Bundle::getString))

//Primitive
fun longArgument() = InstanceArgument(longBundler)
fun intArgument() = InstanceArgument(intBundler)
fun floatArgument() = InstanceArgument(floatBundler)
fun doubleArgument() = InstanceArgument(doubleBundler)

/* ====== State (Read write) delegates ====== */

//Parcelable
fun <P: Parcelable> parcelableState() = InstanceState<P>(getWithError {
    this.getParcelable<P>(it)
}, Bundle::putParcelable)

//String
fun stringState()
        = InstanceState<String>(getWithError(Bundle::getString), Bundle::putString)

//Primitive
fun longState() = InstanceState<Long>(Bundle::getLong, Bundle::putLong)
fun intState() = InstanceState<Int>(Bundle::getInt, Bundle::putInt)
fun floatState() = InstanceState<Float>(Bundle::getFloat, Bundle::putFloat)
fun doubleState() = InstanceState<Double>(Bundle::getDouble, Bundle::putDouble)