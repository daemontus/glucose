package com.glucose2.bundle

import android.os.Bundle

/**
 * An object for retrieving and serializing native values from bundles.
 */
interface BundlerNative<T> : Bundler<T> {
    fun getter(bundle: Bundle, key: String, default: T): T
}