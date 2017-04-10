package com.glucose2.app

import android.support.annotation.IdRes
import android.view.ViewGroup

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
typealias InsertionPoint = ViewGroup.(Holder) -> ViewGroup

/**
 * Create a [InsertionPoint] from an existing view.
 *
 * @throws [IllegalArgumentException] when given parent is not a part of the tree to which
 * we are inserting.
 */
fun HolderGroup.into(parent: ViewGroup): InsertionPoint = { holder ->
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
fun HolderGroup.into(@IdRes parentId: Int): InsertionPoint = { holder ->
    val parent = this.findViewById(parentId) as? ViewGroup
            ?: throw IllegalArgumentException("$parentId is not in the subtree defined by $this")
    parent.addView(holder.view)
    parent
}

/**
 * HolderGroup is an abstraction of types that can serve as a parent to [Holder].
 *
 * Currently, this is only the [Presenter] class. This interface is for documentation
 * purposes only, since to implement it correctly, one needs access to methods
 * declared as internal in this module.
 *
 */
interface HolderGroup {

    /**
     * Attach given [Holder] to this group.
     *
     * This method should insert given holder into the specified [InsertionPoint]
     * and then ensure the holder moves into the attached state.
     */
    fun <T: Holder> attach(holder: T, location: InsertionPoint): T

    /**
     * Detach given [Holder] from this group.
     *
     * This method should first ensure the holder enters detached state
     * and then remove it from its current position in the view hierarchy.
     */
    fun <T: Holder> detach(holder: T): T

    /**
     * Alternative to [HolderGroup.attach].
     */
    fun <T: Holder> T.attachHere(location: InsertionPoint): T = attach(this, location)

    /**
     * Alternative to [HolderGroup.detach].
     */
    fun <T: Holder> T.detachHere(): T = detach(this)

    /**
     * Iterable lazy sequence of all Holders present in this group.
     */
    val children: Sequence<Holder>

    /**
     * Iterable lazy sequence of all Holders present in this and all child groups.
     *
     * Performance note: This method can cause non-trivial overhead since the whole tree has
     * to be traversed. Use with care.
     */
    val childrenRecursive: Sequence<Holder>

}