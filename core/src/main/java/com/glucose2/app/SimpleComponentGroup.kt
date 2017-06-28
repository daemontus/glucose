package com.glucose2.app

import android.os.Bundle
import java.util.*


class SimpleComponentGroup(
        private val presenter: Presenter
) : ComponentGroup {

    private val _children = ArrayList<Component>()

    override val children: Sequence<Component>
        get() = _children.asSequence()

    override val childrenRecursive: Sequence<Component>
        get() = _children.asSequence().flatMap {
            sequenceOf(it) + ((it as? Presenter)?.childrenRecursive ?: emptySequence())
        }

    override fun performStart() {
        _children.forEach { (it as? Presenter)?.performStart() }
    }

    override fun performStop() {
        _children.forEach { (it as? Presenter)?.performStop() }
    }

    override fun performResume() {
        _children.forEach { (it as? Presenter)?.performResume() }
    }

    override fun performPause() {
        _children.forEach { (it as? Presenter)?.performPause() }
    }

    override fun addChild(component: Component) {
        _children.add(component)
    }

    override fun removeChild(component: Component) {
        _children.remove(component)
    }

    override fun afterChildAttach(component: Component) {
        if (component is Presenter) {
            if (component.isResumed && !presenter.isResumed) component.performPause()
            if (component.isStarted && !presenter.isStarted) component.performStop()
            if (!component.isStarted && presenter.isStarted) component.performStart()
            if (!component.isResumed && presenter.isResumed) component.performResume()
        }
    }

    override fun beforeChildDetach(component: Component) {
        // do nothing
    }

    override fun saveInstanceState(): Bundle? {

    }
}