package com.glucose.app

import android.content.res.Configuration
import android.support.annotation.MainThread
import java.lang.reflect.Constructor
import java.util.*


/**
 * A class responsible for creating and caching presenters.
 *
 * It relies on reflection when creating new Presenters.
 *
 * #futurework: Allow different caching strategies (nothing, max. 3, etc.)
 *
 * @see PresenterContext
 */
@MainThread
open class PresenterFactory(private val context: PresenterContext) {

    private val constructors = HashMap<Class<*>, Constructor<out Presenter>>()

    private val allPresenters = ArrayList<Presenter>()
    private val freePresenters = ArrayList<Presenter>()

    private var destroyed = false

    /**
     * Create or reuse a Presenter instance.
     */
    fun <P : Presenter> obtain(clazz: Class<out P>): P {
        if (destroyed) throw IllegalStateException("Cannot request Presenters after onDestroy")
        val found = freePresenters.find { it.javaClass == clazz }
                ?: spawnPresenter(clazz)
        freePresenters.remove(found)
        lifecycleLog("Obtained presenter $found")
        // Cast to P is safe assuming reflection does not fail
        @Suppress("UNCHECKED_CAST")
        return found as P
    }

    /**
     * Mark Presenter instance as ready for reuse
     */
    fun recycle(presenter: Presenter) {
        if (presenter !in allPresenters) throw IllegalStateException("$presenter is not managed by $this but by ${presenter.ctx}")
        if (presenter.isAttached) throw IllegalStateException("$presenter is still attached")
        freePresenters.add(presenter)
        lifecycleLog("Recycled $presenter")
    }

    //Create a new presenter using reflection
    private fun spawnPresenter(clazz: Class<out Presenter>) : Presenter {
        val presenter = constructors.getOrPut(clazz) {
            try {
                clazz.getConstructor(PresenterContext::class.java)
            } catch (e: Exception) {
                throw IllegalStateException("No constructor taking PresenterContext found for ${clazz.name}", e)
            }
        }.newInstance(context)
        lifecycleLog("Spawned presenter for $clazz: $presenter")
        allPresenters.add(presenter)
        return presenter
    }

    //Destroy and tear down presenter
    private fun killPresenter(presenter: Presenter) {
        if (presenter.isAttached) throw IllegalStateException("Killing an attached presenter $presenter")
        presenter.performDestroy()
        freePresenters.remove(presenter)
        allPresenters.remove(presenter)
        lifecycleLog("Killed presenter $presenter")
    }

    /**
     * Notify the factory that it is destroyed and all presenters should be freed.
     */
    fun onDestroy() {
        allPresenters.toList().forEach { killPresenter(it) }
        destroyed = true
    }

    /**
     * Notify the factory that it should remove unnecessary presenters to free memory.
     */
    fun trimMemory() {
        freePresenters.toList().forEach { killPresenter(it) }
    }

    /**
     * Notify !unattached! presenters about configuration change.
     * (Attached are notified through the tree)
     */
    fun onConfigurationChange(newConfig: Configuration) {
        //kill presenters that can't handle config change
        freePresenters.filter { !it.canChangeConfiguration }.forEach {
            killPresenter(it)
        }
        freePresenters.forEach { it.onConfigurationChanged(newConfig) }
    }

}