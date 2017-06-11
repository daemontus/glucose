package com.glucose2.app

/**
 * ComponentGroup is an abstraction of types that can serve as a parent to [Holder].
 *
 * Currently, this is only the [Presenter] class. This interface is for documentation
 * purposes only, since to implement it correctly, one needs access to methods
 * declared as internal in this module.
 *
 */
interface ComponentGroup {

    /**
     * Attach given [Component] to this group.
     *
     * This method should insert given component into the specified [InsertionPoint]
     * and then synchronize the child's lifecycle with this component.
     *
     * Note that this method will fail if the component is already attached.
     * Use [reattach] to safely move components between two locations.
     */
    fun <T: Component> attach(component: T, location: InsertionPoint): T

    /**
     * Detach given [Component] from this group.
     *
     * This method should first ensure the holder enters detached state
     * and then remove it from its current position in the view hierarchy.
     */
    fun <T: Component> detach(component: T): T

    /**
     * Alternative to [ComponentGroup.attach].
     */
    fun <T: Component> T.attachHere(location: InsertionPoint): T = attach(this, location)

    /**
     * Alternative to [ComponentGroup.detach].
     */
    fun <T: Component> T.detachHere(): T = detach(this)

    /**
     * Iterable lazy sequence of all components present in this group.
     */
    val children: Sequence<Component>

    /**
     * Iterable lazy sequence of all components present in this and all child groups.
     *
     * Performance note: This method can cause non-trivial overhead since the whole tree has
     * to be traversed. Use with care.
     *
     * Also note that the iteration order of this sequence should be a pre-order, i.e. parents
     * go first.
     */
    val childrenRecursive: Sequence<Component>

}