package com.glucose2.bundle

import android.os.Bundle
import java.util.*

infix fun String.with(value: BooleanArray?): Bundled<BooleanArray?>
        = Bundled(this, value, booleanArrayBundler)
infix fun String.with(value: ByteArray?): Bundled<ByteArray?>
        = Bundled(this, value, byteArrayBundler)
infix fun String.with(value: CharArray?): Bundled<CharArray?>
        = Bundled(this, value, charArrayBundler)
infix fun String.with(value: DoubleArray?): Bundled<DoubleArray?>
        = Bundled(this, value, doubleArrayBundler)
infix fun String.with(value: FloatArray?): Bundled<FloatArray?>
        = Bundled(this, value, floatArrayBundler)
infix fun String.with(value: IntArray?): Bundled<IntArray?>
        = Bundled(this, value, intArrayBundler)
infix fun String.with(value: LongArray?): Bundled<LongArray?>
        = Bundled(this, value, longArrayBundler)
infix fun String.with(value: ShortArray?): Bundled<ShortArray?>
        = Bundled(this, value, shortArrayBundler)
infix fun String.withIntList(value: ArrayList<Int>?): Bundled<ArrayList<Int>?>
        = Bundled(this, value, intArrayListBundler)

@JvmField val booleanArrayBundler = object : BundlerObject<BooleanArray?> {
    override fun getter(bundle: Bundle, key: String): BooleanArray? = bundle.getBooleanArray(key)
    override fun setter(bundle: Bundle, key: String, value: BooleanArray?) = bundle.putBooleanArray(key, value)
}

@JvmField val byteArrayBundler = object : BundlerObject<ByteArray?> {
    override fun getter(bundle: Bundle, key: String): ByteArray? = bundle.getByteArray(key)
    override fun setter(bundle: Bundle, key: String, value: ByteArray?) = bundle.putByteArray(key, value)
}

@JvmField val charArrayBundler = object : BundlerObject<CharArray?> {
    override fun getter(bundle: Bundle, key: String): CharArray? = bundle.getCharArray(key)
    override fun setter(bundle: Bundle, key: String, value: CharArray?) = bundle.putCharArray(key, value)
}

@JvmField val doubleArrayBundler = object : BundlerObject<DoubleArray?> {
    override fun getter(bundle: Bundle, key: String): DoubleArray? = bundle.getDoubleArray(key)
    override fun setter(bundle: Bundle, key: String, value: DoubleArray?) = bundle.putDoubleArray(key, value)
}

@JvmField val floatArrayBundler = object : BundlerObject<FloatArray?> {
    override fun getter(bundle: Bundle, key: String): FloatArray? = bundle.getFloatArray(key)
    override fun setter(bundle: Bundle, key: String, value: FloatArray?) = bundle.putFloatArray(key, value)
}

@JvmField val intArrayBundler = object : BundlerObject<IntArray?> {
    override fun getter(bundle: Bundle, key: String): IntArray? = bundle.getIntArray(key)
    override fun setter(bundle: Bundle, key: String, value: IntArray?) = bundle.putIntArray(key, value)
}

@JvmField val longArrayBundler = object : BundlerObject<LongArray?> {
    override fun getter(bundle: Bundle, key: String): LongArray? = bundle.getLongArray(key)
    override fun setter(bundle: Bundle, key: String, value: LongArray?) = bundle.putLongArray(key, value)
}

@JvmField val shortArrayBundler = object : BundlerObject<ShortArray?> {
    override fun getter(bundle: Bundle, key: String): ShortArray? = bundle.getShortArray(key)
    override fun setter(bundle: Bundle, key: String, value: ShortArray?) = bundle.putShortArray(key, value)
}

@JvmField val intArrayListBundler = object : BundlerObject<ArrayList<Int>?> {
    override fun getter(bundle: Bundle, key: String): ArrayList<Int>? = bundle.getIntegerArrayList(key)
    override fun setter(bundle: Bundle, key: String, value: ArrayList<Int>?) = bundle.putIntegerArrayList(key, value)
}
