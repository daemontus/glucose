package com.glucose2.bundle

import android.os.Bundle

/**
 * A key value pair that can be serialized into bundle.
 *
 * Note: We don't support extension functions on KProperty, because each usage will create an
 * anonymous class while only calling the name will insert a constant.
 */
class Bundled<T>(val key: String, val value: T, val bundler: Bundler<T>) {
    fun put(bundle: Bundle) = bundler.setter(bundle, key, value)
}