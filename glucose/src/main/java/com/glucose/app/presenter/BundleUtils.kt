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
open class Bundler<T>(
        val getter: Bundle.(String) -> T,
        val setter: Bundle.(String, T) -> Unit
)

class Bundled<T>(val key: String, val value: T, val bundler: Bundler<T>) {
    fun put(bundle: Bundle) = bundler.setter.invoke(bundle, key, value)
}

infix fun Bundled<*>.and(other: Bundled<*>) = Bundle().apply {
    this@and.put(this)
    other.put(this)
}

infix fun Bundle.and(bundled: Bundled<*>): Bundle = this.apply {
    bundled.put(this)

}


// Primitives
val booleanBundler = Bundler(Bundle::getBoolean, Bundle::putBoolean)
val byteBundler = Bundler(Bundle::getByte, Bundle::putByte)
val charBundler = Bundler(Bundle::getChar, Bundle::putChar)
val doubleBundler = Bundler(Bundle::getDouble, Bundle::putDouble)
val floatBundler = Bundler(Bundle::getFloat, Bundle::putFloat)
val intBundler = Bundler(Bundle::getInt, Bundle::putInt)
val longBundler = Bundler(Bundle::getLong, Bundle::putLong)
val shortBundler = Bundler(Bundle::getShort, Bundle::putShort)

// Primitive Arrays
val booleanArrayBundler = Bundler<BooleanArray?>(Bundle::getBooleanArray, Bundle::putBooleanArray)
val byteArrayBundler = Bundler<ByteArray?>(Bundle::getByteArray, Bundle::putByteArray)
val charArrayBundler = Bundler<CharArray?>(Bundle::getCharArray, Bundle::putCharArray)
val doubleArrayBundler = Bundler<DoubleArray?>(Bundle::getDoubleArray, Bundle::putDoubleArray)
val floatArrayBundler = Bundler<FloatArray?>(Bundle::getFloatArray, Bundle::putFloatArray)
val intArrayBundler = Bundler<IntArray?>(Bundle::getIntArray, Bundle::putIntArray)
val longArrayBundler = Bundler<LongArray?>(Bundle::getLongArray, Bundle::putLongArray)
val shortArrayBundler = Bundler<ShortArray?>(Bundle::getShortArray, Bundle::putShortArray)

// String related
val charSequenceBundler = Bundler<CharSequence?>(Bundle::getCharSequence, Bundle::putCharSequence)
val charSequenceArrayBundler = Bundler<Array<CharSequence>?>(Bundle::getCharSequenceArray, Bundle::putCharSequenceArray)
val charSequenceArrayListBundler = Bundler<ArrayList<CharSequence>?>(Bundle::getCharSequenceArrayList, Bundle::putCharSequenceArrayList)
val stringBundler = Bundler<String?>(Bundle::getString, Bundle::putString)
val stringArrayBundler = Bundler<Array<String>?>(Bundle::getStringArray, Bundle::putStringArray)
val stringArrayListBundler = Bundler<ArrayList<String>?>(Bundle::getStringArrayList, Bundle::putStringArrayList)

// Special
val bundleBundler = Bundler<Bundle?>(Bundle::getBundle, Bundle::putBundle)
val intArrayListBundler = Bundler<ArrayList<Int>?>(Bundle::getIntegerArrayList, Bundle::putIntegerArrayList)
val serializableBundler = Bundler<Serializable?>(Bundle::getSerializable, Bundle::putSerializable)
//These objects are not supported on all API levels. Before including them, figure out how to do it safely.
//val binderBundler = Bundler<IBinder?>(Bundle::getBinder, Bundle::putBinder)
//val sizeBundler = Bundler<Size?>(Bundle::getSize, Bundle::putSize)
//val sizeFBundler = Bundler<SizeF?>(Bundle::getSizeF, Bundle::putSizeF)

// Parcelable
fun <P: Parcelable> parcelableBundler() = Bundler<P?>({ this.getParcelable(it) }, { k, v -> this.putParcelable(k, v) })
fun parcelableArrayBundler() = Bundler<Array<out Parcelable>?>(Bundle::getParcelableArray, Bundle::putParcelableArray)
fun <P: Parcelable> parcelableArrayListBundler() = Bundler<ArrayList<P>?>({ this.getParcelableArrayList(it) }, Bundle::putParcelableArrayList)
fun <P: Parcelable> sparseParcelableArrayBundler() = Bundler<SparseArray<P>?>({ this.getSparseParcelableArray(it) }, { k, v -> this.putSparseParcelableArray(k, v) })

//Bundled

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
//infix fun String.with(value: IBinder?): Bundled<IBinder?> = Bundled(this, value, binderBundler)
//infix fun String.with(value: Size?): Bundled<Size?> = Bundled(this, value, sizeBundler)
//infix fun String.with(value: SizeF?): Bundled<SizeF?> = Bundled(this, value, sizeFBundler)

infix fun <P: Parcelable> String.with(value: P?): Bundled<P?> = Bundled(this, value, parcelableBundler())
infix fun <P: Parcelable> String.withParcelableList(value: ArrayList<P>?): Bundled<ArrayList<P>?> = Bundled(this, value, parcelableArrayListBundler<P>())
infix fun String.with(value: Array<out Parcelable>?): Bundled<Array<out Parcelable>?> = Bundled(this, value, parcelableArrayBundler())
infix fun <P: Parcelable> String.with(value: SparseArray<P>?): Bundled<SparseArray<P>?> = Bundled(this, value, sparseParcelableArrayBundler<P>())