package com.github.daemontus.glucose.utils.device

import android.content.Context
import android.view.View
import android.view.inputmethod.InputMethodManager


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