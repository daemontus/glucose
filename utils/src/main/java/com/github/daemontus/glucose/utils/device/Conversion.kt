package com.github.daemontus.glucose.utils.device

import android.content.Context


/**
 * Convert density points to pixels.
 */
fun Context.dpToPx(dp: Float): Float {
    val resources = this.resources
    val metrics = resources.displayMetrics
    return dp * (metrics.densityDpi / 160f)
}

/**
 * Convert pixels to density points.
 */
fun Context.pxToDp(px: Float): Float {
    val resources = this.resources
    val metrics = resources.displayMetrics
    return px / (metrics.densityDpi / 160f)
}