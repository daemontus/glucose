package com.glucose2.app

import android.content.res.Configuration
import android.view.ViewGroup
import kotlin.properties.Delegates

/**
 * Note that the class must be exact!
 */
class ComponentFactory {

    internal var host: ComponentHost by Delegates.notNull()

    private val constructors = HashMap<Class<*>, (ComponentHost, ViewGroup?) -> Component>()

    private val allPresenters = ArrayList<Component>()
    private val freePresenters = ArrayList<Component>()

    private var destroyed = false

    /**
     * Explicitly register a constructor that will be used to create component of this class.
     */
    fun <P : Component> register(clazz: Class<P>, constructor: (ComponentHost, ViewGroup?) -> P) {
        constructors[clazz] = constructor
    }

    /**
     * Create or reuse a Presenter instance.
     *
     * Parent is an optional parameter usable if the location of the component is already known.
     */
    fun <P : Component> obtain(clazz: Class<out P>, parent: ViewGroup?): P {
        if (destroyed) throw LifecycleException("Cannot request Components after onDestroy")
        val found = freePresenters.find { it.javaClass == clazz }
                ?: spawnComponent(clazz, parent)
        freePresenters.remove(found)
        // Cast to P is safe assuming no one gave us a fake constructor
        return clazz.cast(found)
    }

    /**
     * Mark Presenter instance as ready for reuse
     */
    fun recycle(component: Component) {
        if (destroyed) throw LifecycleException("Cannot recycle Presenters after onDestroy")
        if (component !in allPresenters) throw LifecycleException("$component is not managed by $this but by ${component.host}")
        if (component in freePresenters) throw LifecycleException("$component is already recycled")
        if (component.isAttached) throw LifecycleException("$component is still attached")
        if (component.canReuse) {
            freePresenters.add(component)
        } else {
            killComponent(component)
        }
    }

    //Create a new component using provided constructor or reflection
    private fun spawnComponent(clazz: Class<out Component>, parent: ViewGroup?) : Component {
        val component = constructors.getOrPut(clazz) {
            try {
                val constructor = clazz.getConstructor(ComponentHost::class.java, ViewGroup::class.java)
                object : (ComponentHost, ViewGroup?) -> Component {
                    override fun invoke(h: ComponentHost, p: ViewGroup?): Component
                            = constructor.newInstance(h, p)
                }
            } catch (e: Exception) {
                throw LifecycleException("No valid constructor found for ${clazz.name}")
            }
        }.invoke(host, parent)
        allPresenters.add(component)
        return component
    }

    //Destroy and tear down component
    private fun killComponent(component: Component) {
        if (component.isAttached) throw LifecycleException("Killing an attached component $component")
        component.destroy()
        freePresenters.remove(component)
        allPresenters.remove(component)
    }

    /**
     * Notify the factory that it is destroyed and all components should be freed.
     */
    fun onDestroy() {
        allPresenters.toList().forEach { killComponent(it) }
        destroyed = true
    }

    /**
     * Notify the factory that it should remove unnecessary components to free memory.
     */
    fun trimMemory() {
        freePresenters.toList().forEach { killComponent(it) }
    }

    /**
     * Notify all DETACHED components about the configuration change and kill
     * all that can't survive it.
     *
     * [ComponentHost] is responsible for notifying all attached components.
     */
    fun performConfigChange(newConfig: Configuration) {
        freePresenters.filter { !it.canChangeConfiguration }.forEach {
            killComponent(it)
        }
        freePresenters.forEach { it.configurationChange(newConfig) }
    }

}