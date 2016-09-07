package com.glucose.app.presenter

import android.os.Bundle
import android.os.Parcelable
import android.util.SparseArray
import java.io.Serializable
import java.util.*

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


interface Bundler<T> {
    fun getter(bundle: Bundle, key: String): T
    fun setter(bundle: Bundle, key: String, value: T): Unit
}

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


// Primitives
@JvmField val booleanBundler = object : Bundler<Boolean> {
    override fun getter(bundle: Bundle, key: String): Boolean = bundle.getBoolean(key)
    override fun setter(bundle: Bundle, key: String, value: Boolean) = bundle.putBoolean(key, value)
}
@JvmField val byteBundler = object : Bundler<Byte> {
    override fun getter(bundle: Bundle, key: String): Byte = bundle.getByte(key)
    override fun setter(bundle: Bundle, key: String, value: Byte) = bundle.putByte(key, value)
}
@JvmField val charBundler = object : Bundler<Char> {
    override fun getter(bundle: Bundle, key: String): Char = bundle.getChar(key)
    override fun setter(bundle: Bundle, key: String, value: Char) = bundle.putChar(key, value)
}
@JvmField val doubleBundler = object : Bundler<Double> {
    override fun getter(bundle: Bundle, key: String): Double = bundle.getDouble(key)
    override fun setter(bundle: Bundle, key: String, value: Double) = bundle.putDouble(key, value)
}
@JvmField val floatBundler = object : Bundler<Float> {
    override fun getter(bundle: Bundle, key: String): Float = bundle.getFloat(key)
    override fun setter(bundle: Bundle, key: String, value: Float) = bundle.putFloat(key, value)
}
@JvmField val intBundler = object : Bundler<Int> {
    override fun getter(bundle: Bundle, key: String): Int = bundle.getInt(key)
    override fun setter(bundle: Bundle, key: String, value: Int) = bundle.putInt(key, value)
}
@JvmField val longBundler = object : Bundler<Long> {
    override fun getter(bundle: Bundle, key: String): Long = bundle.getLong(key)
    override fun setter(bundle: Bundle, key: String, value: Long) = bundle.putLong(key, value)
}
@JvmField val shortBundler = object : Bundler<Short> {
    override fun getter(bundle: Bundle, key: String): Short = bundle.getShort(key)
    override fun setter(bundle: Bundle, key: String, value: Short) = bundle.putShort(key, value)
}

// Primitive Arrays
@JvmField val booleanArrayBundler = object : Bundler<BooleanArray?> {
    override fun getter(bundle: Bundle, key: String): BooleanArray? = bundle.getBooleanArray(key)
    override fun setter(bundle: Bundle, key: String, value: BooleanArray?) = bundle.putBooleanArray(key, value)
}
@JvmField val byteArrayBundler = object : Bundler<ByteArray?> {
    override fun getter(bundle: Bundle, key: String): ByteArray? = bundle.getByteArray(key)
    override fun setter(bundle: Bundle, key: String, value: ByteArray?) = bundle.putByteArray(key, value)
}
@JvmField val charArrayBundler = object : Bundler<CharArray?> {
    override fun getter(bundle: Bundle, key: String): CharArray? = bundle.getCharArray(key)
    override fun setter(bundle: Bundle, key: String, value: CharArray?) = bundle.putCharArray(key, value)
}
@JvmField val doubleArrayBundler = object : Bundler<DoubleArray?> {
    override fun getter(bundle: Bundle, key: String): DoubleArray? = bundle.getDoubleArray(key)
    override fun setter(bundle: Bundle, key: String, value: DoubleArray?) = bundle.putDoubleArray(key, value)
}
@JvmField val floatArrayBundler = object : Bundler<FloatArray?> {
    override fun getter(bundle: Bundle, key: String): FloatArray? = bundle.getFloatArray(key)
    override fun setter(bundle: Bundle, key: String, value: FloatArray?) = bundle.putFloatArray(key, value)
}
@JvmField val intArrayBundler = object : Bundler<IntArray?> {
    override fun getter(bundle: Bundle, key: String): IntArray? = bundle.getIntArray(key)
    override fun setter(bundle: Bundle, key: String, value: IntArray?) = bundle.putIntArray(key, value)
}
@JvmField val longArrayBundler = object : Bundler<LongArray?> {
    override fun getter(bundle: Bundle, key: String): LongArray? = bundle.getLongArray(key)
    override fun setter(bundle: Bundle, key: String, value: LongArray?) = bundle.putLongArray(key, value)
}
@JvmField val shortArrayBundler = object : Bundler<ShortArray?> {
    override fun getter(bundle: Bundle, key: String): ShortArray? = bundle.getShortArray(key)
    override fun setter(bundle: Bundle, key: String, value: ShortArray?) = bundle.putShortArray(key, value)
}

// String related
@JvmField val charSequenceBundler = object : Bundler<CharSequence?> {
    override fun getter(bundle: Bundle, key: String): CharSequence? = bundle.getCharSequence(key)
    override fun setter(bundle: Bundle, key: String, value: CharSequence?) = bundle.putCharSequence(key, value)
}
@JvmField val charSequenceArrayBundler = object : Bundler<Array<CharSequence>?> {
    override fun getter(bundle: Bundle, key: String): Array<CharSequence>? = bundle.getCharSequenceArray(key)
    override fun setter(bundle: Bundle, key: String, value: Array<CharSequence>?) = bundle.putCharSequenceArray(key, value)
}
@JvmField val charSequenceArrayListBundler = object : Bundler<ArrayList<CharSequence>?> {
    override fun getter(bundle: Bundle, key: String): ArrayList<CharSequence>? = bundle.getCharSequenceArrayList(key)
    override fun setter(bundle: Bundle, key: String, value: ArrayList<CharSequence>?) = bundle.putCharSequenceArrayList(key, value)
}
@JvmField val stringBundler = object : Bundler<String?> {
    override fun getter(bundle: Bundle, key: String): String? = bundle.getString(key)
    override fun setter(bundle: Bundle, key: String, value: String?) = bundle.putString(key, value)
}
@JvmField val stringArrayBundler = object : Bundler<Array<String>?> {
    override fun getter(bundle: Bundle, key: String): Array<String>? = bundle.getStringArray(key)
    override fun setter(bundle: Bundle, key: String, value: Array<String>?) = bundle.putStringArray(key, value)
}
@JvmField val stringArrayListBundler = object : Bundler<ArrayList<String>?> {
    override fun getter(bundle: Bundle, key: String): ArrayList<String>? = bundle.getStringArrayList(key)
    override fun setter(bundle: Bundle, key: String, value: ArrayList<String>?) = bundle.putStringArrayList(key, value)
}

// Special
@JvmField val bundleBundler = object : Bundler<Bundle?> {
    override fun getter(bundle: Bundle, key: String): Bundle? = bundle.getBundle(key)
    override fun setter(bundle: Bundle, key: String, value: Bundle?) = bundle.putBundle(key, value)
}
@JvmField val intArrayListBundler = object : Bundler<ArrayList<Int>?> {
    override fun getter(bundle: Bundle, key: String): ArrayList<Int>? = bundle.getIntegerArrayList(key)
    override fun setter(bundle: Bundle, key: String, value: ArrayList<Int>?) = bundle.putIntegerArrayList(key, value)
}
@JvmField val serializableBundler = object : Bundler<Serializable?> {
    override fun getter(bundle: Bundle, key: String): Serializable? = bundle.getSerializable(key)
    override fun setter(bundle: Bundle, key: String, value: Serializable?) = bundle.putSerializable(key, value)
}

// Parcelable
fun <P: Parcelable> parcelableBundler() = object : Bundler<P?> {
    override fun getter(bundle: Bundle, key: String): P? = bundle.getParcelable(key)
    override fun setter(bundle: Bundle, key: String, value: P?) = bundle.putParcelable(key, value)
}
val parcelableArrayBundler = object : Bundler<Array<out Parcelable>?> {
    override fun getter(bundle: Bundle, key: String): Array<out Parcelable>? = bundle.getParcelableArray(key)
    override fun setter(bundle: Bundle, key: String, value: Array<out Parcelable>?) = bundle.putParcelableArray(key, value)
}
fun <P: Parcelable> parcelableArrayListBundler() = object : Bundler<ArrayList<P>?> {
    override fun getter(bundle: Bundle, key: String): ArrayList<P>? = bundle.getParcelableArrayList(key)
    override fun setter(bundle: Bundle, key: String, value: ArrayList<P>?) = bundle.putParcelableArrayList(key, value)
}
fun <P: Parcelable> sparseParcelableArrayBundler() = object : Bundler<SparseArray<P>?> {
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

infix fun String.with(value: BooleanArray?): Bundled<BooleanArray?> = Bundled(this, value, booleanArrayBundler)
infix fun String.with(value: ByteArray?): Bundled<ByteArray?> = Bundled(this, value, byteArrayBundler)
infix fun String.with(value: CharArray?): Bundled<CharArray?> = Bundled(this, value, charArrayBundler)
infix fun String.with(value: DoubleArray?): Bundled<DoubleArray?> = Bundled(this, value, doubleArrayBundler)
infix fun String.with(value: FloatArray?): Bundled<FloatArray?> = Bundled(this, value, floatArrayBundler)
infix fun String.with(value: IntArray?): Bundled<IntArray?> = Bundled(this, value, intArrayBundler)
infix fun String.with(value: LongArray?): Bundled<LongArray?> = Bundled(this, value, longArrayBundler)
infix fun String.with(value: ShortArray?): Bundled<ShortArray?> = Bundled(this, value, shortArrayBundler)

infix fun String.with(value: CharSequence?): Bundled<CharSequence?> = Bundled(this, value, charSequenceBundler)
infix fun String.with(value: Array<CharSequence>?): Bundled<Array<CharSequence>?> = Bundled(this, value, charSequenceArrayBundler)
infix fun String.withCharSequenceList(value: ArrayList<CharSequence>?): Bundled<ArrayList<CharSequence>?> = Bundled(this, value, charSequenceArrayListBundler)
infix fun String.with(value: String?): Bundled<String?> = Bundled(this, value, stringBundler)
infix fun String.with(value: Array<String>?): Bundled<Array<String>?> = Bundled(this, value, stringArrayBundler)
infix fun String.withStringList(value: ArrayList<String>?): Bundled<ArrayList<String>?> = Bundled(this, value, stringArrayListBundler)


infix fun String.with(value: Bundle?): Bundled<Bundle?> = Bundled(this, value, bundleBundler)
infix fun String.withIntList(value: ArrayList<Int>?): Bundled<ArrayList<Int>?> = Bundled(this, value, intArrayListBundler)
infix fun String.with(value: Serializable?): Bundled<Serializable?> = Bundled(this, value, serializableBundler)

//These objects are not supported on all API levels. Before including them, figure out how to do it safely.
//infix fun String.with(value: IBinder?): Bundled<IBinder?> = Bundled(this, value, binderBundler)
//infix fun String.with(value: Size?): Bundled<Size?> = Bundled(this, value, sizeBundler)
//infix fun String.with(value: SizeF?): Bundled<SizeF?> = Bundled(this, value, sizeFBundler)

infix fun <P: Parcelable> String.with(value: P?): Bundled<P?> = Bundled(this, value, parcelableBundler())
infix fun <P: Parcelable> String.withParcelableList(value: ArrayList<P>?): Bundled<ArrayList<P>?> = Bundled(this, value, parcelableArrayListBundler<P>())
infix fun String.with(value: Array<out Parcelable>?): Bundled<Array<out Parcelable>?> = Bundled(this, value, parcelableArrayBundler)
infix fun <P: Parcelable> String.with(value: SparseArray<P>?): Bundled<SparseArray<P>?> = Bundled(this, value, sparseParcelableArrayBundler<P>())