package com.glucose2.view

import android.support.annotation.IdRes
import android.view.View
import android.view.ViewGroup

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
 * Traverse the view tree of this host. The ordering of the traversal is
 * determined by the tree structure.
 *
 * You can switch between two traversal modes: preOrder and postOrder.
 */
fun ViewHost.viewTraversal(postOrder: Boolean = true): Sequence<View> = traverseView(this.view)

private fun traverseView(view: View, postOrder: Boolean): Sequence<View> {
    return if (view !is ViewGroup) {
        sequenceOf(view)
    } else {
        val children = (0 until view.childCount).asSequence().flatMap { index ->
            traverseView(view.getChildAt(index), postOrder)
        }
        if (postOrder) children + sequenceOf(view) else sequenceOf(view) + children
    }
}