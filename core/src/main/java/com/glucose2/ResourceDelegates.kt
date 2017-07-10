package com.glucose2

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.drawable.Drawable
import android.os.Build
import android.support.annotation.*
import com.glucose2.app.Component
import com.glucose2.rx.ObservableBinder
import com.glucose2.rx.observeWhile
import io.reactivex.Observable
import java.util.concurrent.atomic.AtomicReference
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

//Note: Resources object is synchronized, so it is safe to use from different threads!

interface ContextHost {
    val context: Context
}

interface ResourceGetter<out T : Any> {
    fun Context.get(id: Int): T
}

/**
 * A lazy delegate which uses an AtomicReference to hold the instance, and
 * can therefore call the initializer multiple times.
 *
 * Such behaviour is desirable in case of properties which are not very costly
 * to instantiate (and don't need to be discarded), but which still need some form of
 * caching. (Another case when this is preferable is when there is little congestion)
 */
internal abstract class ResettableLazyDelegate<in R, out T>(
        private val resetTrigger: ObservableBinder
) : ReadOnlyProperty<R, T> {

    private val reference = AtomicReference<T?>()

    abstract fun initializer(thisRef: R): T

    override final fun getValue(thisRef: R, property: KProperty<*>): T {
        var value = reference.get()
        while (value == null) {
            val newValue = initializer(thisRef)
            if (reference.compareAndSet(null, newValue)) {
                Observable.just(Unit)
                        .observeWhile(resetTrigger)
                        .doOnComplete { reference.set(null) }
                        .subscribe()
            }
            value = reference.get()
        }
        return value
    }

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

fun Component.colorResource(@ColorRes id: Int): ReadOnlyProperty<ContextHost, Int>
        = resourceDelegate(id, ColorGetter)

fun Component.dimensionPixelResource(@DimenRes id: Int): ReadOnlyProperty<ContextHost, Int>
        = resourceDelegate(id, DimensionPixelGetter)

fun Component.dimensionResource(@DimenRes id: Int): ReadOnlyProperty<ContextHost, Float>
        = resourceDelegate(id, DimensionGetter)

fun Component.colorStateListResource(id: Int): ReadOnlyProperty<ContextHost, ColorStateList>
        = resourceDelegate(id, ColorStateListGetter)

fun Component.booleanResource(@BoolRes id: Int): ReadOnlyProperty<ContextHost, Boolean>
        = resourceDelegate(id, BooleanGetter)

fun Component.drawableResource(@DrawableRes id: Int): ReadOnlyProperty<ContextHost, Drawable>
        = resourceDelegate(id, DrawableGetter)

fun Component.intArrayResource(id: Int): ReadOnlyProperty<ContextHost, IntArray>
        = resourceDelegate(id, IntArrayGetter)

fun Component.integerResource(@IntegerRes id: Int): ReadOnlyProperty<ContextHost, Int>
        = resourceDelegate(id, IntGetter)

fun Component.stringResource(@StringRes id: Int): ReadOnlyProperty<ContextHost, String>
        = resourceDelegate(id, StringGetter)

fun Component.stringArrayResource(id: Int): ReadOnlyProperty<ContextHost, Array<String>>
        = resourceDelegate(id, StringArrayGetter)