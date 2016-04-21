package com.github.daemontus.glucose.utils.device

import android.annotation.TargetApi
import android.app.Activity
import android.content.Context
import android.os.Build
import android.provider.Settings
import android.util.DisplayMetrics
import android.view.*
import android.view.inputmethod.InputMethodManager
import java.io.File
import java.util.regex.Pattern

object Device {

    /**
     * true if device has generic fingerprint (i.e. should be emulator)
     */
    fun isEmulator(): Boolean {
        return Build.FINGERPRINT.contains("generic") || Build.FINGERPRINT.contains("unknown")
    }

    /**
     * True if device has navigation bar / soft keys.
     */
    fun hasNavigationBar(context: Context): Boolean {
        //on 5.0 and 5.1, the "hasBackKey" technique no longer works (6.0 is fine, but, WTF?!)
        if (Device.OS.atLeastJellyBeanMR1()) {
            return hasNavigationBarCurrent(context)
        } else
            return hasNavigationBarLegacy(context)
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    private fun hasNavigationBarCurrent(context: Context): Boolean {
        val windowManager = context.getSystemService(Activity.WINDOW_SERVICE) as WindowManager
        val d = windowManager.defaultDisplay

        val realDisplayMetrics = DisplayMetrics()
        d.getRealMetrics(realDisplayMetrics)

        val displayMetrics = DisplayMetrics()
        d.getMetrics(displayMetrics)

        return realDisplayMetrics.heightPixels - displayMetrics.heightPixels > 0
    }

    private fun hasNavigationBarLegacy(context: Context): Boolean {
        val hasMenuKey = ViewConfiguration.get(context).hasPermanentMenuKey();
        val hasBackKey = KeyCharacterMap.deviceHasKey(KeyEvent.KEYCODE_BACK);
        return !hasMenuKey && !hasBackKey
    }

    /**
     * Height of navigation bar in pixels (can be different on tablets/phones)
     * Warning: Non zero navigation bar height does not imply presence of navigation bar
     */
    fun getNavBarHeight(context: Context): Int {
        var nav_bar = 0
        val resources = context.resources
        val resourceId = resources.getIdentifier("navigation_bar_height", "dimen", "android")
        if (resourceId > 0) {
            nav_bar = resources.getDimensionPixelSize(resourceId)
        }
        return nav_bar
    }

    /**
     * @param context Context.
     * *
     * @return Height of the status bar in pixels (can be different on tablets/phones)
     */
    fun getStatusBarHeight(context: Context): Int {
        var result = 0
        val resourceId = context.resources.getIdentifier("status_bar_height", "dimen", "android")
        if (resourceId > 0) {
            result = context.resources.getDimensionPixelSize(resourceId)
        }
        return result
    }


    /**
     * Gets the number of cores available in this device, across all processors.
     * Requires: Ability to peruse the filesystem at "/sys/devices/system/cpu"
     * @return The number of cores, or 1 if failed to get result
     */
    fun getNumCores(): Int {
        try {
            //Get directory containing CPU info
            val dir = File("/sys/devices/system/cpu/")
            //Filter to only list the devices we care about
            val files = dir.listFiles { file -> Pattern.matches("cpu[0-9]+", file.name) }
            //Return the number of cores (virtual CPU devices)
            return files.size
        } catch (e: Exception) {
            //Default to return 1 core
            return 1
        }

    }

    /**
     * Return (hopefully) unique device ID
     */
    fun getDeviceId(ctx: Context): String {
        val id = Settings.Secure.getString(ctx.contentResolver, Settings.Secure.ANDROID_ID)
        return id ?: ""
    }

    object Keyboard {

        /**
         * Hide keyboard.
         * @param context Context - used to retrieve input method manager.
         * *
         * @param view A view used to provide a window token.
         */
        fun hide(context: Context, view: View) {
            val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(view.windowToken, 0)
        }

        /**
         * Show keyboard.
         * @param context Context - used to retrieve input method manager.
         * *
         * @param view A view used to provide a window token.
         */
        fun show(context: Context, view: View) {
            val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.showSoftInput(view, 0)
        }

    }

    /**
     * Convenience methods for checking minimal OS versions.
     */
    object OS {
        fun atLeastICS(): Boolean {
            return Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH
        }

        fun atLeastICSMR1(): Boolean {
            return Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1
        }

        fun atLeastJellyBean(): Boolean {
            return Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN
        }

        fun atLeastJellyBeanMR1(): Boolean {
            return Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1
        }

        fun atLeastJellyBeanMR2(): Boolean {
            return Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2
        }

        fun atLeastKitKat(): Boolean {
            return Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT
        }

        fun atLeastLollipop(): Boolean {
            return Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP
        }

        fun atLeastLollipopMR1(): Boolean {
            return Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1
        }

        fun atLeastMarshmallow(): Boolean {
            return Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
        }
    }

}