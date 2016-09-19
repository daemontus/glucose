package com.glucose.app

import android.content.res.Configuration
import android.support.annotation.MainThread
import android.view.ViewGroup
import com.glucose.app.presenter.LifecycleException
import com.glucose.app.presenter.isAttached
import java.util.*


/**
 * A class responsible for creating and caching presenters.
 *
 * PresenterFactory first looks for a registered constructor (see [register]).
 * If no such constructor exists, it will try to find a constructor
 * with two parameters ([PresenterHost], [ViewGroup]?) using reflection.
 * If no such constructor is found, it will throw an [IllegalStateException].
 *
 * PresenterFactory will try to cache detached [Presenter] instances. To
 * ensure this mechanism works properly, it relies on [PresenterHost]
 * to provide appropriate callbacks for detached presenters. To prevent this
 * behavior, use [Presenter.canBeReused].
 *
 * PresenterFactory retains references to all created presenters
 * and at the time of destruction checks if all of them were properly
 * detached. This way you can detect leaked presenters.
 *
 * @see PresenterHost
 */
@MainThread
open class PresenterFactory(private val context: PresenterHost) {

    private val constructors = HashMap<Class<*>, (PresenterHost, ViewGroup?) -> Presenter>()

    private val allPresenters = ArrayList<Presenter>()
    private val freePresenters = ArrayList<Presenter>()

    private var destroyed = false

    /**
     * Explicitly register a constructor that will be used to create presenter of this class.
     */
    fun <P : Presenter> register(clazz: Class<P>, constructor: (PresenterHost, ViewGroup?) -> P) {
        constructors[clazz] = constructor
    }

    /**
     * Create or reuse a Presenter instance.
     */
    fun <P : Presenter> obtain(clazz: Class<out P>, parent: ViewGroup?): P {
        if (destroyed) throw LifecycleException("Cannot request Presenters after onDestroy")
        val found = freePresenters.find { it.javaClass == clazz }
                ?: spawnPresenter(clazz, parent)
        freePresenters.remove(found)
        // Cast to P is safe assuming no one gave us a fake constructor
        @Suppress("UNCHECKED_CAST")
        return found as P
    }

    /**
     * Mark Presenter instance as ready for reuse
     */
    fun recycle(presenter: Presenter) {
        if (destroyed) throw LifecycleException("Cannot recycle Presenters after onDestroy")
        if (presenter !in allPresenters) throw LifecycleException("$presenter is not managed by $this but by ${presenter.host}")
        if (presenter in freePresenters) throw LifecycleException("$presenter is already recycled")
        if (presenter.isAttached) throw LifecycleException("$presenter is still attached")
        if (presenter.canBeReused) {
            freePresenters.add(presenter)
        } else {
            killPresenter(presenter)
        }
    }

    //Create a new presenter using provided constructor or reflection
    private fun spawnPresenter(clazz: Class<out Presenter>, parent: ViewGroup?) : Presenter {
        val presenter = constructors.getOrPut(clazz) {
            try {
                val constructor = clazz.getConstructor(PresenterHost::class.java, ViewGroup::class.java)
                object : (PresenterHost, ViewGroup?) -> Presenter {
                    override fun invoke(p1: PresenterHost, p2: ViewGroup?): Presenter
                            = constructor.newInstance(p1, p2)
                }
            } catch (e: Exception) {
                throw LifecycleException("No valid constructor found for ${clazz.name}", e)
            }
        }.invoke(context, parent)
        allPresenters.add(presenter)
        return presenter
    }

    //Destroy and tear down presenter
    private fun killPresenter(presenter: Presenter) {
        if (presenter.isAttached) throw LifecycleException("Killing an attached presenter $presenter")
        presenter.performDestroy()
        freePresenters.remove(presenter)
        allPresenters.remove(presenter)
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
     * Notify all DETACHED presenters about the configuration change.
     * [PresenterHost] is responsible for notifying all attached presenters.
     */
    fun performConfigChange(newConfig: Configuration) {
        prepareConfigChange()
        freePresenters.forEach { it.onConfigurationChanged(newConfig) }
    }

    /**
     * Clean presenter cache so that everything that can't survive config change is killed.
     */
    fun prepareConfigChange() {
        freePresenters.filter { !it.canChangeConfiguration }.forEach {
            killPresenter(it)
        }
    }

}