package com.github.daemontus.glucose

import android.view.View
import android.view.ViewGroup

inline fun ViewGroup.forEachChild(action: (View) -> Unit) {
    for (i in 0 until childCount) {
        action(getChildAt(i))
    }
}


object Duration {
    val STANDARD = 300L
    val LEAVE = 195L
    val ENTER = 225L
    val COMPLEX = 375L
}