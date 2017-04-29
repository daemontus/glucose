package com.glucose2.bundle

import android.os.Bundle

/**
 * An object for serializing items into bundles.
 */
interface Bundler<in T> {
    fun setter(bundle: Bundle, key: String, value: T): Unit
}

