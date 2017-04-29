package com.glucose2.bundle

import android.os.Bundle

// Native bundlers

infix fun String.with(value: Boolean): Bundled<Boolean> = Bundled(this, value, booleanBundler)
infix fun String.with(value: Byte): Bundled<Byte> = Bundled(this, value, byteBundler)
infix fun String.with(value: Char): Bundled<Char> = Bundled(this, value, charBundler)
infix fun String.with(value: Double): Bundled<Double> = Bundled(this, value, doubleBundler)
infix fun String.with(value: Float): Bundled<Float> = Bundled(this, value, floatBundler)
infix fun String.with(value: Int): Bundled<Int> = Bundled(this, value, intBundler)
infix fun String.with(value: Long): Bundled<Long> = Bundled(this, value, longBundler)
infix fun String.with(value: Short): Bundled<Short> = Bundled(this, value, shortBundler)

@JvmField val booleanBundler = object : BundlerNative<Boolean> {
    override fun getter(bundle: Bundle, key: String, default: Boolean): Boolean = bundle.getBoolean(key, default)
    override fun setter(bundle: Bundle, key: String, value: Boolean) = bundle.putBoolean(key, value)
}

@JvmField val byteBundler = object : BundlerNative<Byte> {
    override fun getter(bundle: Bundle, key: String, default: Byte): Byte = bundle.getByte(key, default)
    override fun setter(bundle: Bundle, key: String, value: Byte) = bundle.putByte(key, value)
}

@JvmField val charBundler = object : BundlerNative<Char> {
    override fun getter(bundle: Bundle, key: String, default: Char): Char = bundle.getChar(key, default)
    override fun setter(bundle: Bundle, key: String, value: Char) = bundle.putChar(key, value)
}

@JvmField val doubleBundler = object : BundlerNative<Double> {
    override fun getter(bundle: Bundle, key: String, default: Double): Double = bundle.getDouble(key, default)
    override fun setter(bundle: Bundle, key: String, value: Double) = bundle.putDouble(key, value)
}

@JvmField val floatBundler = object : BundlerNative<Float> {
    override fun getter(bundle: Bundle, key: String, default: Float): Float = bundle.getFloat(key, default)
    override fun setter(bundle: Bundle, key: String, value: Float) = bundle.putFloat(key, value)
}

@JvmField val intBundler = object : BundlerNative<Int> {
    override fun getter(bundle: Bundle, key: String, default: Int): Int = bundle.getInt(key, default)
    override fun setter(bundle: Bundle, key: String, value: Int) = bundle.putInt(key, value)
}

@JvmField val longBundler = object : BundlerNative<Long> {
    override fun getter(bundle: Bundle, key: String, default: Long): Long = bundle.getLong(key, default)
    override fun setter(bundle: Bundle, key: String, value: Long) = bundle.putLong(key, value)
}

@JvmField val shortBundler = object : BundlerNative<Short> {
    override fun getter(bundle: Bundle, key: String, default: Short): Short = bundle.getShort(key, default)
    override fun setter(bundle: Bundle, key: String, value: Short) = bundle.putShort(key, value)
}