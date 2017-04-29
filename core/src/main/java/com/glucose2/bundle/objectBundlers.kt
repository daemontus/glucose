package com.glucose2.bundle

import android.os.Bundle
import android.os.Parcelable
import android.util.SparseArray
import java.io.Serializable
import java.util.*

// Bundlers for complex object types

infix fun String.with(value: Bundle?): Bundled<Bundle?>
        = Bundled(this, value, bundleBundler)
infix inline fun <reified S: Serializable> String.with(value: S?): Bundled<S?>
        = Bundled(this, value, serializableBundler(S::class.java))
infix fun <P: Parcelable> String.with(value: SparseArray<P>?): Bundled<SparseArray<P>?>
        = Bundled(this, value, sparseParcelableArrayBundler<P>())
infix fun <P: Parcelable> String.with(value: P?): Bundled<P?>
        = Bundled(this, value, parcelableBundler())
infix fun <P: Parcelable> String.withParcelableList(value: ArrayList<P>?): Bundled<ArrayList<P>?>
        = Bundled(this, value, parcelableArrayListBundler<P>())
infix fun String.with(value: Array<out Parcelable>?): Bundled<Array<out Parcelable>?>
        = Bundled(this, value, parcelableArrayBundler)

@JvmField val bundleBundler = object : BundlerObject<Bundle?> {
    override fun getter(bundle: Bundle, key: String): Bundle? = bundle.getBundle(key)
    override fun setter(bundle: Bundle, key: String, value: Bundle?) = bundle.putBundle(key, value)
}

fun <S: Serializable> serializableBundler(clazz: Class<S>) = object : BundlerObject<S?> {
    override fun getter(bundle: Bundle, key: String): S? = clazz.cast(bundle.getSerializable(key))
    override fun setter(bundle: Bundle, key: String, value: S?) = bundle.putSerializable(key, value)
}

fun <P: Parcelable> parcelableBundler() = object : BundlerObject<P?> {
    override fun getter(bundle: Bundle, key: String): P? = bundle.getParcelable(key)
    override fun setter(bundle: Bundle, key: String, value: P?) = bundle.putParcelable(key, value)
}

fun <P: Parcelable> sparseParcelableArrayBundler() = object : BundlerObject<SparseArray<P>?> {
    override fun getter(bundle: Bundle, key: String): SparseArray<P>? = bundle.getSparseParcelableArray(key)
    override fun setter(bundle: Bundle, key: String, value: SparseArray<P>?) = bundle.putSparseParcelableArray(key, value)
}

fun <P: Parcelable> parcelableArrayListBundler() = object : BundlerObject<ArrayList<P>?> {
    override fun getter(bundle: Bundle, key: String): ArrayList<P>? = bundle.getParcelableArrayList(key)
    override fun setter(bundle: Bundle, key: String, value: ArrayList<P>?) = bundle.putParcelableArrayList(key, value)
}

@JvmField val parcelableArrayBundler = object : BundlerObject<Array<out Parcelable>?> {
    override fun getter(bundle: Bundle, key: String): Array<out Parcelable>? = bundle.getParcelableArray(key)
    override fun setter(bundle: Bundle, key: String, value: Array<out Parcelable>?) = bundle.putParcelableArray(key, value)
}