package com.github.daemontus.glucose.utils.device

import android.content.Context

object Units {

    /**
     * Convert density points to pixels.
     */
    fun dpToPx(ctx: Context, dp: Float): Float {
        val metrics = ctx.resources.displayMetrics
        return dp * (metrics.densityDpi / 160f)
    }


    /**
     * Convert pixels to density points.
     */
    fun pxToDp(ctx: Context, px: Float): Float {
        val metrics = ctx.resources.displayMetrics
        return px / (metrics.densityDpi / 160f)
    }

}