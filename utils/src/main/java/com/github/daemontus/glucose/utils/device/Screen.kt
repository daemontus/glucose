package com.github.daemontus.glucose.utils.device

import android.app.Activity
import android.util.DisplayMetrics

object Screen {

    /**
     * Display width in pixels
     */
    fun getWidth(context: Activity): Int {
        val metrics = DisplayMetrics()
        context.windowManager.defaultDisplay.getMetrics(metrics)
        return metrics.widthPixels
    }

    /**
     * Display height in pixels
     */
    fun getHeight(context: Activity): Int {
        val metrics = DisplayMetrics()
        context.windowManager.defaultDisplay.getMetrics(metrics)
        return metrics.heightPixels
    }

}