package com.glucose2.app.group

import android.content.res.Configuration
import android.os.Bundle
import android.view.ViewGroup
import com.glucose2.app.*

class StackGroup(
        private val root: Class<out Component>?,
        private val view: ViewGroup,
        private val presenter: Presenter
) : ComponentGroup<Unit>(presenter) {

    private val CLASS_KEY = ":glucose:class"
    private val STACK_KEY = ":glucose:stack"

    private val stack = ArrayList<Component>()

    val isEmpty: Boolean
        get() = stack.isEmpty()

    fun push(component: Class<out Component>, state: Bundle = Bundle()) {
        val child = presenter.host.factory.obtain(component, view)
        child.attach(this, Unit, state)
    }

    fun pop(removeBottom: Boolean = false) {
        if (stack.size > 1 || removeBottom) {
            stack.lastOrNull()?.let { top ->
                top.detach()
                presenter.host.factory.recycle(top)
            }
        }
    }

    override fun attach(state: Bundle) {
        super.attach(state)

        val stackState = state.getParcelableArrayList<Bundle>(STACK_KEY) ?: ArrayList()

        // add stack bottom if empty
        if (stackState.isEmpty()) {
            stackState.add(Bundle().apply { putSerializable(CLASS_KEY, root) })
        }

        // restore state
        for (childState in stackState) {
            (childState.getSerializable(CLASS_KEY) as? Class<out Component>)?.let { childClass ->
                childState.remove(CLASS_KEY)
                val child = presenter.host.factory.obtain(childClass, view)
                child.attach(this, Unit, childState)
            }
        }
    }

    override fun saveInstanceState(): Bundle {
        val state = super.saveInstanceState()
        val stackState = ArrayList<Bundle>()
        for (child in stack) {
            val childState = child.saveInstanceState()
            childState.putSerializable(CLASS_KEY, child.javaClass)
        }

        state.putParcelableArrayList(STACK_KEY, stackState)
        return state
    }

    override fun detach(): Bundle {
        val state = super.detach()
        for (child in stack) {
            child.detach()
            presenter.host.factory.recycle(child)
        }
        return state
    }

    override fun addChild(child: Component, location: Unit) {
        super.addChild(child, location)
        view.addView(child.view)
    }

    override fun attachChild(child: Component) {
        super.attachChild(child)
        // stop previous child
        (stack.lastOrNull() as? Presenter)?.let { oldTop ->
            if (oldTop.isResumed) oldTop.pause()
            if (oldTop.isStarted) oldTop.stop()
        }
        stack.add(child)
        // sync this child
        if (child is Presenter) {
            if (!presenter.isResumed && child.isResumed) child.pause()
            if (!presenter.isStarted && child.isStarted) child.stop()
            if (presenter.isStarted && !child.isStarted) child.start()
            if (presenter.isResumed && !child.isResumed) child.resume()
        }
    }

    override fun detachChild(child: Component) {
        stack.remove(child)
        (stack.lastOrNull() as? Presenter)?.let { newTop ->
            if (presenter.isStarted && !newTop.isStarted) newTop.start()
            if (presenter.isResumed && !newTop.isResumed) newTop.resume()
        }
        super.detachChild(child)
    }

    override fun removeChild(child: Component) {
        view.removeView(child.view)
        super.removeChild(child)
    }

    override fun configurationChange(newConfig: Configuration) {
        super.configurationChange(newConfig)
        //TODO
    }

    override fun pause() {
        (stack.lastOrNull() as? Presenter)?.pause()
        super.pause()
    }

    override fun resume() {
        super.resume()
        (stack.lastOrNull() as? Presenter)?.resume()
    }

    override fun start() {
        super.start()
        (stack.lastOrNull() as? Presenter)?.start()
    }

    override fun stop() {
        (stack.lastOrNull() as? Presenter)?.stop()
        super.stop()
    }

}