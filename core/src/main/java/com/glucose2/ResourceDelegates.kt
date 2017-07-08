package com.glucose2

import android.content.res.ColorStateList
import android.content.res.Resources
import android.graphics.drawable.Drawable
import android.support.annotation.*
import com.glucose2.app.Component
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

interface ResourceGetter<out T : Any> {
    fun Resources.get(id: Int): T
}

private object ColorGetter : ResourceGetter<Int> {
    override fun Resources.get(id: Int): Int = this.getColor(id)
}

private object ColorStateListGetter : ResourceGetter<ColorStateList> {
    override fun Resources.get(id: Int): ColorStateList = this.getColorStateList(id)
}

private object DimensionPixelGetter : ResourceGetter<Int> {
    override fun Resources.get(id: Int): Int = this.getDimensionPixelSize(id)
}

private object DimensionGetter : ResourceGetter<Float> {
    override fun Resources.get(id: Int): Float = this.getDimension(id)
}

private object BooleanGetter : ResourceGetter<Boolean> {
    override fun Resources.get(id: Int): Boolean = this.getBoolean(id)
}

private object DrawableGetter : ResourceGetter<Drawable> {
    override fun Resources.get(id: Int): Drawable = this.getDrawable(id)
}

private object IntArrayGetter : ResourceGetter<IntArray> {
    override fun Resources.get(id: Int): IntArray = this.getIntArray(id)
}

private object IntGetter : ResourceGetter<Int> {
    override fun Resources.get(id: Int): Int = this.getInteger(id)
}

private object StringGetter : ResourceGetter<String> {
    override fun Resources.get(id: Int): String = this.getString(id)
}

private object StringArrayGetter : ResourceGetter<Array<String>> {
    override fun Resources.get(id: Int): Array<String> = this.getStringArray(id)
}


private class ResourceDelegate<out T : Any>(
        private val id: Int,
        private val getter: ResourceGetter<T>
) : ReadOnlyProperty<Component, T> {

    override fun getValue(thisRef: Component, property: KProperty<*>): T {
        return getter.run { thisRef.host.activity.resources.get(id) }
    }

}

fun Component.colorResource(@ColorRes id: Int): ReadOnlyProperty<Component, Int>
        = ResourceDelegate(id, ColorGetter)

fun Component.dimensionPixelResource(@DimenRes id: Int): ReadOnlyProperty<Component, Int>
        = ResourceDelegate(id, DimensionPixelGetter)

fun Component.dimensionResource(@DimenRes id: Int): ReadOnlyProperty<Component, Float>
        = ResourceDelegate(id, DimensionGetter)

fun Component.colorStateListResource(id: Int): ReadOnlyProperty<Component, ColorStateList>
        = ResourceDelegate(id, ColorStateListGetter)

fun Component.booleanResource(@BoolRes id: Int): ReadOnlyProperty<Component, Boolean>
        = ResourceDelegate(id, BooleanGetter)

fun Component.drawableResource(@DrawableRes id: Int): ReadOnlyProperty<Component, Drawable>
        = ResourceDelegate(id, DrawableGetter)

fun Component.intArrayResource(id: Int): ReadOnlyProperty<Component, IntArray>
        = ResourceDelegate(id, IntArrayGetter)

fun Component.integerResource(@IntegerRes id: Int): ReadOnlyProperty<Component, Int>
        = ResourceDelegate(id, IntGetter)

fun Component.stringResource(@StringRes id: Int): ReadOnlyProperty<Component, String>
        = ResourceDelegate(id, StringGetter)

fun Component.stringArrayResource(id: Int): ReadOnlyProperty<Component, Array<String>>
        = ResourceDelegate(id, StringArrayGetter)