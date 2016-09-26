package com.glucose.app.presenter

import android.os.Bundle
import android.os.Parcelable
import android.util.SparseArray
import java.io.Serializable

/*
    Bundle interface

//primitive
boolean	getBoolean(String key)
byte	getByte(String key)
char	getChar(String key)
double	getDouble(String key)
float	getFloat(String key)
int	getInt(String key)
long	getLong(String key)
short	getShort(String key)

//primitive arrays
boolean[]	getBooleanArray(String key)
byte[]	getByteArray(String key)
char[]	getCharArray(String key)
double[]	getDoubleArray(String key)
float[]	getFloatArray(String key)
int[]	getIntArray(String key)
long[]	getLongArray(String key)
short[]	getShortArray(String key)

//String related
CharSequence	getCharSequence(String key)
CharSequence[]	getCharSequenceArray(String key)
ArrayList<CharSequence>	getCharSequenceArrayList(String key)
String	getString(String key)
String[]	getStringArray(String key)
ArrayList<String>	getStringArrayList(String key)

//Parcelable
<T extends Parcelable> T	getParcelable(String key)
Parcelable[]	getParcelableArray(String key)
<T extends Parcelable> ArrayList<T>	getParcelableArrayList(String key)
<T extends Parcelable> SparseArray<T>	getSparseParcelableArray(String key)

//Special
IBinder	getBinder(String key)
Bundle	getBundle(String key)
Size	getSize(String key)
SizeF	getSizeF(String key)
ArrayList<Integer>	getIntegerArrayList(String key)
Serializable	getSerializable(String key)

 */

/**
 * An object for serializing items into bundles.
 */
interface Bundler<in T> {
    fun setter(bundle: Bundle, key: String, value: T): Unit
}

/**
 * An object for retrieving and serializing objects from bundles.
 */
interface ObjectBundler<T : Any?> : Bundler<T> {
    fun getter(bundle: Bundle, key: String): T
}

/**
 * An object for retrieving and serializing native values from bundles.
 */
interface NativeBundler<T> : Bundler<T> {
    fun getter(bundle: Bundle, key: String, default: T): T
}

/**
 * A key value pair that can be serialized into bundle.
 */
class Bundled<T>(val key: String, val value: T, val bundler: Bundler<T>) {
    fun put(bundle: Bundle) = bundler.setter(bundle, key, value)
}

/**
 * Use this function to create a bundle from multiple key-value pairs.
 *
 * Example: ("key1" with 10) and ("key2" with 20)
 */
infix fun Bundled<*>.and(other: Bundled<*>): Bundle = Bundle().apply {
    this@and.put(this)
    other.put(this)
}

/**
 * Use this function to append data to an existing bundle.
 *
 * Example: Bundle().and("key" with 15)
 */
infix fun Bundle.and(bundled: Bundled<*>): Bundle = this.apply {
    bundled.put(this)

}

/**
 * Use this to build a bundle if you only have one key-value pair.
 *
 * Example: bundle("key" with 10)
 */
fun bundle(bundled: Bundled<*>): Bundle = Bundle().and(bundled)


//For more, see bundle-array and bundle-list

// Primitives
@JvmField val booleanBundler = object : NativeBundler<Boolean> {
    override fun getter(bundle: Bundle, key: String, default: Boolean): Boolean = bundle.getBoolean(key, default)
    override fun setter(bundle: Bundle, key: String, value: Boolean) = bundle.putBoolean(key, value)
}
@JvmField val byteBundler = object : NativeBundler<Byte> {
    override fun getter(bundle: Bundle, key: String, default: Byte): Byte = bundle.getByte(key, default)
    override fun setter(bundle: Bundle, key: String, value: Byte) = bundle.putByte(key, value)
}
@JvmField val charBundler = object : NativeBundler<Char> {
    override fun getter(bundle: Bundle, key: String, default: Char): Char = bundle.getChar(key, default)
    override fun setter(bundle: Bundle, key: String, value: Char) = bundle.putChar(key, value)
}
@JvmField val doubleBundler = object : NativeBundler<Double> {
    override fun getter(bundle: Bundle, key: String, default: Double): Double = bundle.getDouble(key, default)
    override fun setter(bundle: Bundle, key: String, value: Double) = bundle.putDouble(key, value)
}
@JvmField val floatBundler = object : NativeBundler<Float> {
    override fun getter(bundle: Bundle, key: String, default: Float): Float = bundle.getFloat(key, default)
    override fun setter(bundle: Bundle, key: String, value: Float) = bundle.putFloat(key, value)
}
@JvmField val intBundler = object : NativeBundler<Int> {
    override fun getter(bundle: Bundle, key: String, default: Int): Int = bundle.getInt(key, default)
    override fun setter(bundle: Bundle, key: String, value: Int) = bundle.putInt(key, value)
}
@JvmField val longBundler = object : NativeBundler<Long> {
    override fun getter(bundle: Bundle, key: String, default: Long): Long = bundle.getLong(key, default)
    override fun setter(bundle: Bundle, key: String, value: Long) = bundle.putLong(key, value)
}
@JvmField val shortBundler = object : NativeBundler<Short> {
    override fun getter(bundle: Bundle, key: String, default: Short): Short = bundle.getShort(key, default)
    override fun setter(bundle: Bundle, key: String, value: Short) = bundle.putShort(key, value)
}

// String related
@JvmField val charSequenceBundler = object : ObjectBundler<CharSequence?> {
    override fun getter(bundle: Bundle, key: String): CharSequence? = bundle.getCharSequence(key)
    override fun setter(bundle: Bundle, key: String, value: CharSequence?) = bundle.putCharSequence(key, value)
}
@JvmField val stringBundler = object : ObjectBundler<String?> {
    override fun getter(bundle: Bundle, key: String): String? = bundle.getString(key)
    override fun setter(bundle: Bundle, key: String, value: String?) = bundle.putString(key, value)
}

// Special
@JvmField val bundleBundler = object : ObjectBundler<Bundle?> {
    override fun getter(bundle: Bundle, key: String): Bundle? = bundle.getBundle(key)
    override fun setter(bundle: Bundle, key: String, value: Bundle?) = bundle.putBundle(key, value)
}
@JvmField val serializableBundler = object : ObjectBundler<Serializable?> {
    override fun getter(bundle: Bundle, key: String): Serializable? = bundle.getSerializable(key)
    override fun setter(bundle: Bundle, key: String, value: Serializable?) = bundle.putSerializable(key, value)
}

// Parcelable
fun <P: Parcelable> parcelableBundler() = object : ObjectBundler<P?> {
    override fun getter(bundle: Bundle, key: String): P? = bundle.getParcelable(key)
    override fun setter(bundle: Bundle, key: String, value: P?) = bundle.putParcelable(key, value)
}
fun <P: Parcelable> sparseParcelableArrayBundler() = object : ObjectBundler<SparseArray<P>?> {
    override fun getter(bundle: Bundle, key: String): SparseArray<P>? = bundle.getSparseParcelableArray(key)
    override fun setter(bundle: Bundle, key: String, value: SparseArray<P>?) = bundle.putSparseParcelableArray(key, value)
}

//Bundled
//Note: We don't support extension functions on KProperty, because each usage will create an
//anonymous class while only calling the name will insert a constant.

infix fun String.with(value: Boolean): Bundled<Boolean> = Bundled(this, value, booleanBundler)
infix fun String.with(value: Byte): Bundled<Byte> = Bundled(this, value, byteBundler)
infix fun String.with(value: Char): Bundled<Char> = Bundled(this, value, charBundler)
infix fun String.with(value: Double): Bundled<Double> = Bundled(this, value, doubleBundler)
infix fun String.with(value: Float): Bundled<Float> = Bundled(this, value, floatBundler)
infix fun String.with(value: Int): Bundled<Int> = Bundled(this, value, intBundler)
infix fun String.with(value: Long): Bundled<Long> = Bundled(this, value, longBundler)
infix fun String.with(value: Short): Bundled<Short> = Bundled(this, value, shortBundler)

infix fun String.with(value: CharSequence?): Bundled<CharSequence?> = Bundled(this, value, charSequenceBundler)
infix fun String.with(value: String?): Bundled<String?> = Bundled(this, value, stringBundler)
infix fun String.with(value: Bundle?): Bundled<Bundle?> = Bundled(this, value, bundleBundler)
infix fun String.with(value: Serializable?): Bundled<Serializable?> = Bundled(this, value, serializableBundler)
infix fun <P: Parcelable> String.with(value: SparseArray<P>?): Bundled<SparseArray<P>?> = Bundled(this, value, sparseParcelableArrayBundler<P>())
infix fun <P: Parcelable> String.with(value: P?): Bundled<P?> = Bundled(this, value, parcelableBundler())

//These objects are not supported on all API levels. Before including them, figure out how to do it safely.
//infix fun String.with(value: IBinder?): Bundled<IBinder?> = Bundled(this, value, binderBundler)
//infix fun String.with(value: Size?): Bundled<Size?> = Bundled(this, value, sizeBundler)
//infix fun String.with(value: SizeF?): Bundled<SizeF?> = Bundled(this, value, sizeFBundler)

