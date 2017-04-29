package com.glucose2.bundle

import android.os.Bundle

/**
 * An object for retrieving and serializing objects from bundles.
 */
interface BundlerObject<T : Any?> : Bundler<T> {
    fun getter(bundle: Bundle, key: String): T
}