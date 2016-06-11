package com.github.daemontus.glucose.blueprints.component

import android.os.Bundle
import android.os.Parcelable
/*
fun Component<*,*>.intState(key: String, default: Int = 0)
        = IntState(this, key, default)
fun Component<*,*>.longState(key: String, default: Long = 0L)
        = LongState(this, key, default)
fun Component<*,*>.doubleState(key: String, default: Double = 0.0)
        = DoubleState(this, key, default)
fun Component<*,*>.floatState(key: String, default: Float = 0f)
        = FloatState(this, key, default)
fun Component<*,*>.stringState(key: String, default: String = "")
        = StringState(this, key, default)
fun <T: Parcelable> Component<*,*>.parcelableState(key: String, default: T)
        = ParcelableState(this, key, default)

class IntState(
        component: Component<*,*>, key: String, defaultValue: Int
) : Component.State<Int>(component, key, defaultValue) {
    override fun bundleGet(bundle: Bundle): Int? = bundle.getInt(key, defaultValue)
    override fun bundleSet(bundle: Bundle) {
        bundle.putInt(key, value)
    }
}

class LongState(
        component: Component<*,*>, key: String, defaultValue: Long
) : Component.State<Long>(component, key, defaultValue) {
    override fun bundleGet(bundle: Bundle): Long? = bundle.getLong(key, defaultValue)
    override fun bundleSet(bundle: Bundle) {
        bundle.putLong(key, value)
    }
}

class DoubleState(
        component: Component<*,*>, key: String, defaultValue: Double
) : Component.State<Double>(component, key, defaultValue) {
    override fun bundleGet(bundle: Bundle): Double? = bundle.getDouble(key, defaultValue)
    override fun bundleSet(bundle: Bundle) {
        bundle.putDouble(key, value)
    }
}

class FloatState(
        component: Component<*,*>, key: String, defaultValue: Float
) : Component.State<Float>(component, key, defaultValue) {
    override fun bundleGet(bundle: Bundle): Float? = bundle.getFloat(key, defaultValue)
    override fun bundleSet(bundle: Bundle) {
        bundle.putFloat(key, value)
    }
}

class StringState(
        component: Component<*,*>, key: String, defaultValue: String
) : Component.State<String>(component, key, defaultValue) {
    override fun bundleGet(bundle: Bundle): String? = bundle.getString(key, defaultValue)
    override fun bundleSet(bundle: Bundle) {
        bundle.putString(key, value)
    }
}

class ParcelableState<T: Parcelable>(
        component: Component<*,*>, key: String, defaultValue: T
) : Component.State<T>(component, key, defaultValue) {
    override fun bundleGet(bundle: Bundle): T? = bundle.getParcelable(key)
    override fun bundleSet(bundle: Bundle) {
        bundle.putParcelable(key, value)
    }
}*/