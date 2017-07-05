package com.glucose2.app.group

import android.content.res.Configuration
import android.os.Bundle
import android.view.ViewGroup
import com.glucose2.app.*
import com.glucose2.bundle.parcelableBundler
import com.glucose2.state.StateOptional

open class SingletonGroup internal constructor(
        private val component: Class<out Component>,
        private val view: ViewGroup,
        private val parent: Parent
) : ComponentGroup<Unit>(parent) {

    constructor(component: Class<out Component>, view: ViewGroup, host: Presenter)
            : this(component, view, wrapForGroup(host))

    private var componentState: Bundle? by StateOptional(parcelableBundler())

    private var attached: Component? = null

    private var isStarted: Boolean = false
    private var isResumed: Boolean = false

    override fun attach(state: Bundle) {
        super.attach(state)
        val component = parent.factory.obtain(component, view)
        component.attach(this, Unit, componentState ?: Bundle())
    }

    override fun detach(): Bundle {
        attached?.let { detaching ->
            componentState = detaching.detach()
            parent.factory.recycle(detaching)
        }
        return super.detach()
    }

    override fun saveInstanceState(): Bundle {
        componentState = attached?.saveInstanceState()
        return super.saveInstanceState()
    }

    override fun addChild(child: Component, location: Unit) {
        super.addChild(child, location)
        view.addView(child.view)
    }

    override fun attachChild(child: Component) {
        super.attachChild(child)
        attached = child
        // sync lifecycle
        if (child is Presenter) {
            if (child.isResumed && !this.isResumed) child.pause()
            if (child.isStarted && !this.isStarted) child.stop()
            if (!child.isStarted && this.isStarted) child.start()
            if (!child.isResumed && this.isResumed) child.resume()
        }
    }

    override fun detachChild(child: Component) {
        attached = null
        super.detachChild(child)
    }

    override fun removeChild(child: Component) {
        view.removeView(child.view)
        super.removeChild(child)
    }

    override fun configurationChange(newConfig: Configuration) {
        attached?.let { attached ->
            if (!attached.canChangeConfiguration) {
                componentState = attached.detach()
                val component = parent.factory.obtain(component, view)
                component.attach(this, Unit, componentState ?: Bundle())
            }
        }
        super.configurationChange(newConfig)
    }

    override fun start() {
        super.start()
        isStarted = true
        (attached as? Presenter)?.start()
    }

    override fun resume() {
        super.resume()
        isResumed = true
        (attached as? Presenter)?.resume()
    }

    override fun pause() {
        (attached as? Presenter)?.pause()
        isResumed = false
        super.pause()
    }

    override fun stop() {
        (attached as? Presenter)?.stop()
        isStarted = false
        super.stop()
    }
}