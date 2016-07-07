package com.glucose.device

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

fun Float.dpToPx(ctx: Context): Float = Units.dpToPx(ctx, this)

fun Int.dpToPx(ctx: Context): Int = this.toFloat().dpToPx(ctx).toInt()

fun Float.pxToDp(ctx: Context): Float = Units.pxToDp(ctx, this)

fun Int.pxToDp(ctx: Context): Int = this.toInt().pxToDp(ctx).toInt()