package com.glucose2.bundle

import android.os.Bundle

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

/*

    Bundle interface

//primitive
boolean	getBoolean(String key)
byte	getByte(String key)
char	getChar(String key)
double	getDouble(String key)
float	getFloat(String key)
int	    getInt(String key)
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