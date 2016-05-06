package com.github.daemontus.glucose.utils.device

import android.app.Activity
import android.util.DisplayMetrics

object Screen {

    /**
     * Real display width in pixels
     */
    fun getRealWidth(context: Activity): Int {
        val metrics = DisplayMetrics()
        context.windowManager.defaultDisplay.getRealMetrics(metrics)
        return metrics.widthPixels
    }

    /**
     * Real display width in pixels
     */
    fun getRealHeight(context: Activity): Int {
        val metrics = DisplayMetrics()
        context.windowManager.defaultDisplay.getRealMetrics(metrics)
        return metrics.heightPixels
    }

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