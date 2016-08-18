package com.glucose.app

import android.app.Activity
import android.content.res.Configuration
import android.os.Bundle
import android.view.ViewGroup
import java.util.*


open class ContextDelegate(override val activity: Activity) : PresenterContext {

    private val constructors = HashMap<Class<*>, (PresenterContext, ViewGroup?) -> Presenter<*>>()

    private val allPresenters = ArrayList<Presenter<*>>()
    private val freePresenters = ArrayList<Presenter<*>>()

    private var created = false
    private var destroyed = false
    private var state: Bundle? = null

    override fun <P : Presenter<*>> register(clazz: Class<P>, factory: (PresenterContext, ViewGroup?) -> P) {
        if (clazz in constructors.keys) throw IllegalStateException("Constructor for $clazz already registered")
        if (created) throw IllegalStateException("Cannot register constructors after onCreate")
        lifecycleLog("Registered constructor for $clazz")
        constructors[clazz] = factory
    }

    override fun <P : Presenter<*>> obtain(clazz: Class<out P>, parent: ViewGroup?): P {
        if (!created) throw IllegalStateException("Cannot request Presenters before onCreate")
        if (destroyed) throw IllegalStateException("Cannot request Presenters after onDestroy")
        val found = freePresenters.find { it.javaClass == clazz }
                ?: spawnPresenter(clazz, parent)
        freePresenters.remove(found)
        lifecycleLog("Obtained presenter $found")
        // Cast to P is safe by signature of register (and semantics of HashMap and spawnPresenter)
        @Suppress("UNCHECKED_CAST")
        return found as P
    }

    override fun recycle(presenter: Presenter<*>) {
        if (!created) throw IllegalStateException("Cannot recycle $presenter before onCreate")
        if (presenter.ctx != this) throw IllegalStateException("$presenter is not managed by $this but by ${presenter.ctx}")
        if (presenter.isAttached) throw IllegalStateException("$presenter is still attached")
        if (presenter !in allPresenters) throw IllegalStateException("$presenter is corrupted. It has right context but is not managed by $this")
        freePresenters.add(presenter)
        lifecycleLog("Recycled $presenter")
    }

    //Create and initialize presenter with saved state
    private fun spawnPresenter(clazz: Class<*>, parent: ViewGroup?) : Presenter<*> {
        val constructor = constructors[clazz] ?: throw IllegalStateException("No constructor registered for $clazz")
        val presenter = constructor.invoke(this, parent)
        lifecycleLog("Spawned presenter for $clazz: $presenter")
        allPresenters.add(presenter)
        presenter.performCreate(state)
        return presenter
    }

    //Destroy and tear down presenter
    private fun killPresenter(presenter: Presenter<*>) {
        if (presenter.isAttached) throw IllegalStateException("Killing an attached presenter $presenter")
        presenter.performDestroy()
        freePresenters.remove(presenter)
        allPresenters.remove(presenter)
        lifecycleLog("Killed presenter $presenter")
    }

    fun onCreate(savedInstanceState: Bundle?) {
        state = savedInstanceState
        created = true
        lifecycleLog("Presenter delegate created")
    }

    fun onSaveInstanceState(outState: Bundle) {
        allPresenters.forEach {
            it.onSaveInstanceState(outState)
        }
        lifecycleLog("Presenter delegate state saved")
    }

    fun onDestroy() {
        allPresenters.toList().forEach { killPresenter(it) }
        allPresenters.clear()
        freePresenters.clear()
        lifecycleLog("Presenter delegate destroyed")
    }

    fun onLowMemory() {
        lifecycleLog("Clean up free presenters due to low memory")
        freePresenters.toList().forEach { killPresenter(it) }
        freePresenters.clear()
    }

    fun onConfigurationChange(newConfig: Configuration) {
        //kill presenters that can't handle config change
        freePresenters.filter { !it.canChangeConfiguration }.forEach {
            killPresenter(it)
        }
        freePresenters.forEach { it.performConfigurationChange(newConfig) }
    }

}