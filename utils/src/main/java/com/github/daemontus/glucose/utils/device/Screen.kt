package com.github.daemontus.glucose.utils.device

import android.annotation.TargetApi
import android.app.Activity
import android.os.Build
import android.util.DisplayMetrics

object Screen {

    /**
     * Real display width in pixels
     * This is not available prior API 17,
     * therefore the result for older android versions is the closest approximation,
     * the display width. If you absolutely need this, try adding status bar height
     * based on the device rotation.
     */
    fun getRealWidth(context: Activity): Int {
        if (Device.OS.atLeastJellyBeanMR1()) {
            return realWidth(context)
        } else {
            return getWidth(context)
        }
    }

    /**
     * Real display width in pixels (including decorations).
     * This is not available prior API 17,
     * therefore the result for older android versions is the closest approximation,
     * the display height. If you absolutely need this, try adding status bar height
     * based on the device rotation.
     */
    fun getRealHeight(context: Activity): Int {
        if (Device.OS.atLeastJellyBeanMR1()) {
            return realHeight(context)
        } else {
            return getHeight(context)
        }
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    private fun realHeight(context: Activity): Int {
        val metrics = DisplayMetrics()
        context.windowManager.defaultDisplay.getRealMetrics(metrics)
        return metrics.heightPixels
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    private fun realWidth(context: Activity): Int {
        val metrics = DisplayMetrics()
        context.windowManager.defaultDisplay.getRealMetrics(metrics)
        return metrics.widthPixels
    }


    /**
     * Display width in pixels.
     */
    fun getWidth(context: Activity): Int {
        val metrics = DisplayMetrics()
        context.windowManager.defaultDisplay.getMetrics(metrics)
        return metrics.widthPixels
    }

    /**
     * Display height in pixels.
     */
    fun getHeight(context: Activity): Int {
        val metrics = DisplayMetrics()
        context.windowManager.defaultDisplay.getMetrics(metrics)
        return metrics.heightPixels
    }

}