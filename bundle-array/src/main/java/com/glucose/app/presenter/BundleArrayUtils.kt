package com.glucose.app.presenter

import android.os.Bundle
import android.os.Parcelable

// Primitive Arrays
@JvmField val booleanArrayBundler = object : ObjectBundler<BooleanArray?> {
    override fun getter(bundle: Bundle, key: String): BooleanArray? = bundle.getBooleanArray(key)
    override fun setter(bundle: Bundle, key: String, value: BooleanArray?) = bundle.putBooleanArray(key, value)
}
@JvmField val byteArrayBundler = object : ObjectBundler<ByteArray?> {
    override fun getter(bundle: Bundle, key: String): ByteArray? = bundle.getByteArray(key)
    override fun setter(bundle: Bundle, key: String, value: ByteArray?) = bundle.putByteArray(key, value)
}
@JvmField val charArrayBundler = object : ObjectBundler<CharArray?> {
    override fun getter(bundle: Bundle, key: String): CharArray? = bundle.getCharArray(key)
    override fun setter(bundle: Bundle, key: String, value: CharArray?) = bundle.putCharArray(key, value)
}
@JvmField val doubleArrayBundler = object : ObjectBundler<DoubleArray?> {
    override fun getter(bundle: Bundle, key: String): DoubleArray? = bundle.getDoubleArray(key)
    override fun setter(bundle: Bundle, key: String, value: DoubleArray?) = bundle.putDoubleArray(key, value)
}
@JvmField val floatArrayBundler = object : ObjectBundler<FloatArray?> {
    override fun getter(bundle: Bundle, key: String): FloatArray? = bundle.getFloatArray(key)
    override fun setter(bundle: Bundle, key: String, value: FloatArray?) = bundle.putFloatArray(key, value)
}
@JvmField val intArrayBundler = object : ObjectBundler<IntArray?> {
    override fun getter(bundle: Bundle, key: String): IntArray? = bundle.getIntArray(key)
    override fun setter(bundle: Bundle, key: String, value: IntArray?) = bundle.putIntArray(key, value)
}
@JvmField val longArrayBundler = object : ObjectBundler<LongArray?> {
    override fun getter(bundle: Bundle, key: String): LongArray? = bundle.getLongArray(key)
    override fun setter(bundle: Bundle, key: String, value: LongArray?) = bundle.putLongArray(key, value)
}
@JvmField val shortArrayBundler = object : ObjectBundler<ShortArray?> {
    override fun getter(bundle: Bundle, key: String): ShortArray? = bundle.getShortArray(key)
    override fun setter(bundle: Bundle, key: String, value: ShortArray?) = bundle.putShortArray(key, value)
}

@JvmField val charSequenceArrayBundler = object : ObjectBundler<Array<CharSequence>?> {
    override fun getter(bundle: Bundle, key: String): Array<CharSequence>? = bundle.getCharSequenceArray(key)
    override fun setter(bundle: Bundle, key: String, value: Array<CharSequence>?) = bundle.putCharSequenceArray(key, value)
}

@JvmField val stringArrayBundler = object : ObjectBundler<Array<String>?> {
    override fun getter(bundle: Bundle, key: String): Array<String>? = bundle.getStringArray(key)
    override fun setter(bundle: Bundle, key: String, value: Array<String>?) = bundle.putStringArray(key, value)
}

val parcelableArrayBundler = object : ObjectBundler<Array<out Parcelable>?> {
    override fun getter(bundle: Bundle, key: String): Array<out Parcelable>? = bundle.getParcelableArray(key)
    override fun setter(bundle: Bundle, key: String, value: Array<out Parcelable>?) = bundle.putParcelableArray(key, value)
}

infix fun String.with(value: BooleanArray?): Bundled<BooleanArray?> = Bundled(this, value, booleanArrayBundler)
infix fun String.with(value: ByteArray?): Bundled<ByteArray?> = Bundled(this, value, byteArrayBundler)
infix fun String.with(value: CharArray?): Bundled<CharArray?> = Bundled(this, value, charArrayBundler)
infix fun String.with(value: DoubleArray?): Bundled<DoubleArray?> = Bundled(this, value, doubleArrayBundler)
infix fun String.with(value: FloatArray?): Bundled<FloatArray?> = Bundled(this, value, floatArrayBundler)
infix fun String.with(value: IntArray?): Bundled<IntArray?> = Bundled(this, value, intArrayBundler)
infix fun String.with(value: LongArray?): Bundled<LongArray?> = Bundled(this, value, longArrayBundler)
infix fun String.with(value: ShortArray?): Bundled<ShortArray?> = Bundled(this, value, shortArrayBundler)

infix fun String.with(value: Array<CharSequence>?): Bundled<Array<CharSequence>?> = Bundled(this, value, charSequenceArrayBundler)
infix fun String.with(value: Array<String>?): Bundled<Array<String>?> = Bundled(this, value, stringArrayBundler)
infix fun String.with(value: Array<out Parcelable>?): Bundled<Array<out Parcelable>?> = Bundled(this, value, parcelableArrayBundler)
