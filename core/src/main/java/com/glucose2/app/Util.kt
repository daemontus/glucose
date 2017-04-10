package com.glucose2.app

import android.view.View

/**
 * Check if given view resides somewhere in the view hierarchy rooted
 * in target view.
 *
 * Performance note: Don't use this in performance critical tasks, since it
 * has to traverse the whole in the worst case.
 */
internal fun View.hasTransitiveChild(view: View): Boolean {
    var parent: View? = view
    while (parent != null && parent != this) {
        parent = parent.parent as? View
    }
    return parent == this
}