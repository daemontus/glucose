package com.glucose2.view

import android.support.annotation.IdRes
import android.view.View

/**
 * Find the view with given ID in this hosts view tree.
 * If no such view is present, [KotlinNullPointerException] will be thrown.
 */
inline fun <reified V: View> ViewHost.findView(@IdRes viewId: Int): V
        = view.findViewById(viewId) as V

/**
 * Find an optional view with given ID in this hosts tree.
 * If no such view is present, null is returned.
 */
inline fun <reified V: View> ViewHost.findOptionalView(@IdRes viewId: Int): V?
        = view.findViewById(viewId) as V?

/**
 * Traverse the view structure originating in this view.
 *
 * You can choose between two types of traversal: top-down (postOrder = false) and
 * bottom-up (postOrder = true).
 *
 * Note that the sequence will not survive concurrent modifications of the tree structure.
 * So you should avoid removing/adding views to the tree while traversing.
 */
/*private fun View.traverse(postOrder: Boolean = true): Sequence<View> {
    return if (this !is ViewGroup) {
        sequenceOf(this)
    } else {
        val children = (0 until this.childCount).asSequence().flatMap { index ->
            this.getChildAt(index).traverse(postOrder)
        }
        if (postOrder) children + sequenceOf(this) else sequenceOf(this) + children
    }
}*/