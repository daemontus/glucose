package com.glucose.app.presenter

import android.os.Bundle
import android.os.Parcelable
import java.util.*

@JvmField val charSequenceArrayListBundler = object : ObjectBundler<ArrayList<CharSequence>?> {
    override fun getter(bundle: Bundle, key: String): ArrayList<CharSequence>? = bundle.getCharSequenceArrayList(key)
    override fun setter(bundle: Bundle, key: String, value: ArrayList<CharSequence>?) = bundle.putCharSequenceArrayList(key, value)
}
@JvmField val stringArrayListBundler = object : ObjectBundler<ArrayList<String>?> {
    override fun getter(bundle: Bundle, key: String): ArrayList<String>? = bundle.getStringArrayList(key)
    override fun setter(bundle: Bundle, key: String, value: ArrayList<String>?) = bundle.putStringArrayList(key, value)
}
@JvmField val intArrayListBundler = object : ObjectBundler<ArrayList<Int>?> {
    override fun getter(bundle: Bundle, key: String): ArrayList<Int>? = bundle.getIntegerArrayList(key)
    override fun setter(bundle: Bundle, key: String, value: ArrayList<Int>?) = bundle.putIntegerArrayList(key, value)
}
fun <P: Parcelable> parcelableArrayListBundler() = object : ObjectBundler<ArrayList<P>?> {
    override fun getter(bundle: Bundle, key: String): ArrayList<P>? = bundle.getParcelableArrayList(key)
    override fun setter(bundle: Bundle, key: String, value: ArrayList<P>?) = bundle.putParcelableArrayList(key, value)
}

infix fun String.withCharSequenceList(value: ArrayList<CharSequence>?): Bundled<ArrayList<CharSequence>?> = Bundled(this, value, charSequenceArrayListBundler)
infix fun String.withStringList(value: ArrayList<String>?): Bundled<ArrayList<String>?> = Bundled(this, value, stringArrayListBundler)
infix fun String.withIntList(value: ArrayList<Int>?): Bundled<ArrayList<Int>?> = Bundled(this, value, intArrayListBundler)
infix fun <P: Parcelable> String.withParcelableList(value: ArrayList<P>?): Bundled<ArrayList<P>?> = Bundled(this, value, parcelableArrayListBundler<P>())
