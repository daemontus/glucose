package com.glucose2.bundle

import android.os.Bundle
import java.util.*

// String related

infix fun String.with(value: CharSequence?): Bundled<CharSequence?>
        = Bundled(this, value, charSequenceBundler)
infix fun String.with(value: String?): Bundled<String?>
        = Bundled(this, value, stringBundler)
infix fun String.withCharSequenceList(value: ArrayList<CharSequence>?): Bundled<ArrayList<CharSequence>?>
        = Bundled(this, value, charSequenceArrayListBundler)
infix fun String.withStringList(value: ArrayList<String>?): Bundled<ArrayList<String>?>
        = Bundled(this, value, stringArrayListBundler)
infix fun String.with(value: Array<CharSequence>?): Bundled<Array<CharSequence>?>
        = Bundled(this, value, charSequenceArrayBundler)
infix fun String.with(value: Array<String>?): Bundled<Array<String>?>
        = Bundled(this, value, stringArrayBundler)


@JvmField val charSequenceBundler = object : BundlerObject<CharSequence?> {
    override fun getter(bundle: Bundle, key: String): CharSequence? = bundle.getCharSequence(key)
    override fun setter(bundle: Bundle, key: String, value: CharSequence?) = bundle.putCharSequence(key, value)
}

@JvmField val stringBundler = object : BundlerObject<String?> {
    override fun getter(bundle: Bundle, key: String): String? = bundle.getString(key)
    override fun setter(bundle: Bundle, key: String, value: String?) = bundle.putString(key, value)
}

@JvmField val charSequenceArrayListBundler = object : BundlerObject<ArrayList<CharSequence>?> {
    override fun getter(bundle: Bundle, key: String): ArrayList<CharSequence>? = bundle.getCharSequenceArrayList(key)
    override fun setter(bundle: Bundle, key: String, value: ArrayList<CharSequence>?) = bundle.putCharSequenceArrayList(key, value)
}

@JvmField val stringArrayListBundler = object : BundlerObject<ArrayList<String>?> {
    override fun getter(bundle: Bundle, key: String): ArrayList<String>? = bundle.getStringArrayList(key)
    override fun setter(bundle: Bundle, key: String, value: ArrayList<String>?) = bundle.putStringArrayList(key, value)
}

@JvmField val charSequenceArrayBundler = object : BundlerObject<Array<CharSequence>?> {
    override fun getter(bundle: Bundle, key: String): Array<CharSequence>? = bundle.getCharSequenceArray(key)
    override fun setter(bundle: Bundle, key: String, value: Array<CharSequence>?) = bundle.putCharSequenceArray(key, value)
}

@JvmField val stringArrayBundler = object : BundlerObject<Array<String>?> {
    override fun getter(bundle: Bundle, key: String): Array<String>? = bundle.getStringArray(key)
    override fun setter(bundle: Bundle, key: String, value: Array<String>?) = bundle.putStringArray(key, value)
}
