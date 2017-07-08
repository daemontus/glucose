package com.glucose2

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.drawable.Drawable
import android.os.Build
import android.support.annotation.*
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

//TODO: Caching? What about config changes then...

//Note: Resources object is synchronized, so it is safe to use from different threads!

interface ContextHost {
    val context: Context
}

interface ResourceGetter<out T : Any> {
    fun Context.get(id: Int): T
}

private object ColorGetter : ResourceGetter<Int> {
    override fun Context.get(id: Int): Int {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            this.resources.getColor(id, this.theme)
        } else {
            @Suppress("DEPRECATION")    // only on SDK 22 or older
            this.resources.getColor(id)
        }
    }
}

private object ColorStateListGetter : ResourceGetter<ColorStateList> {
    override fun Context.get(id: Int): ColorStateList {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            this.resources.getColorStateList(id, this.theme)
        } else {
            @Suppress("DEPRECATION")    // only on SDK 22 or older
            this.resources.getColorStateList(id)
        }
    }
}

private object DimensionPixelGetter : ResourceGetter<Int> {
    override fun Context.get(id: Int): Int = this.resources.getDimensionPixelSize(id)
}

private object DimensionGetter : ResourceGetter<Float> {
    override fun Context.get(id: Int): Float = this.resources.getDimension(id)
}

private object BooleanGetter : ResourceGetter<Boolean> {
    override fun Context.get(id: Int): Boolean = this.resources.getBoolean(id)
}

private object DrawableGetter : ResourceGetter<Drawable> {
    override fun Context.get(id: Int): Drawable {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
            this.resources.getDrawable(id, this.theme)
        } else {
            @Suppress("DEPRECATION")    // only on SDK 21 or older
            this.resources.getDrawable(id)
        }
    }
}

private object IntArrayGetter : ResourceGetter<IntArray> {
    override fun Context.get(id: Int): IntArray = this.resources.getIntArray(id)
}

private object IntGetter : ResourceGetter<Int> {
    override fun Context.get(id: Int): Int = this.resources.getInteger(id)
}

private object StringGetter : ResourceGetter<String> {
    override fun Context.get(id: Int): String = this.resources.getString(id)
}

private object StringArrayGetter : ResourceGetter<Array<String>> {
    override fun Context.get(id: Int): Array<String> = this.resources.getStringArray(id)
}


private class ResourceDelegate<out T : Any>(
        private val id: Int,
        private val getter: ResourceGetter<T>
) : ReadOnlyProperty<ContextHost, T> {

    override fun getValue(thisRef: ContextHost, property: KProperty<*>): T {
        return getter.run { thisRef.context.get(id) }
    }

}

fun ContextHost.colorResource(@ColorRes id: Int): ReadOnlyProperty<ContextHost, Int>
        = ResourceDelegate(id, ColorGetter)

fun ContextHost.dimensionPixelResource(@DimenRes id: Int): ReadOnlyProperty<ContextHost, Int>
        = ResourceDelegate(id, DimensionPixelGetter)

fun ContextHost.dimensionResource(@DimenRes id: Int): ReadOnlyProperty<ContextHost, Float>
        = ResourceDelegate(id, DimensionGetter)

fun ContextHost.colorStateListResource(id: Int): ReadOnlyProperty<ContextHost, ColorStateList>
        = ResourceDelegate(id, ColorStateListGetter)

fun ContextHost.booleanResource(@BoolRes id: Int): ReadOnlyProperty<ContextHost, Boolean>
        = ResourceDelegate(id, BooleanGetter)

fun ContextHost.drawableResource(@DrawableRes id: Int): ReadOnlyProperty<ContextHost, Drawable>
        = ResourceDelegate(id, DrawableGetter)

fun ContextHost.intArrayResource(id: Int): ReadOnlyProperty<ContextHost, IntArray>
        = ResourceDelegate(id, IntArrayGetter)

fun ContextHost.integerResource(@IntegerRes id: Int): ReadOnlyProperty<ContextHost, Int>
        = ResourceDelegate(id, IntGetter)

fun ContextHost.stringResource(@StringRes id: Int): ReadOnlyProperty<ContextHost, String>
        = ResourceDelegate(id, StringGetter)

fun ContextHost.stringArrayResource(id: Int): ReadOnlyProperty<ContextHost, Array<String>>
        = ResourceDelegate(id, StringArrayGetter)