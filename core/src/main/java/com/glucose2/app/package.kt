package com.glucose2.app

import android.support.annotation.IdRes
import android.view.View
import android.view.ViewGroup
import com.glucose2.bundle.booleanBundler
import com.glucose2.bundle.intBundler
import com.glucose2.state.StateNative

// reduce the amount of object allocations per each component by reusing stateless delegates.
internal val ID_DELEGATE = StateNative(View.NO_ID, intBundler)
internal val TRUE_DELEGATE = StateNative(true, booleanBundler)

/**
 * Thrown whenever some code tries to perform an operation not allowed
 * by the lifecycle restrictions, such as accessing state of a component
 * that does not have any bound.
 */
class LifecycleException(message: String) : Exception(message)

internal fun lifecycleError(message: String): Nothing = throw LifecycleException(message)

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
/**
 * This type alias represents a lambda function that can be used to insert a [Holder]
 * into the view hierarchy.
 *
 * The target [ViewGroup] is the root of the view tree to which the holder can
 * be placed. Furthermore, the function receives a holder instance that should be
 * inserted.
 *
 * Using this information, the function should determine a proper place to insert
 * the holder and then do so. (optionally, it can also perform some other
 * necessary related actions on the view hierarchy)
 *
 * Finally, it should return the actual [ViewGroup] where the holder has been inserted.
 * This is mainly for validation purposes.
 */
typealias InsertionPoint = ViewGroup.(Component) -> ViewGroup

/**
 * Create a [InsertionPoint] from an existing view.
 *
 * @throws [IllegalArgumentException] when given parent is not a part of the tree to which
 * we are inserting.
 */
fun Presenter.into(parent: ViewGroup): InsertionPoint = { holder ->
    if (!this.hasTransitiveChild(parent)) {
        throw IllegalArgumentException("$parent is not in the subtree defined by $this")
    }
    parent.addView(holder.view)
    parent
}

/**
 * Create a [InsertionPoint] from an existing ID resource.
 *
 * @throws [IllegalArgumentException] when given parent id is not a present in the tree to which
 * we are inserting.
 */
fun Presenter.into(@IdRes parentId: Int): InsertionPoint = { holder ->
    val parent = this.findViewById(parentId) as? ViewGroup
            ?: throw IllegalArgumentException("$parentId is not in the subtree defined by $this")
    parent.addView(holder.view)
    parent
}
