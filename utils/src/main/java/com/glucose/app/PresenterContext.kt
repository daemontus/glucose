package com.glucose.app

import android.app.Activity
import android.support.annotation.LayoutRes
import android.support.annotation.MainThread
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup


/**
 * Interface implemented by a class that is able to host Presenters.
 * Usually this class would be the Activity of an App.
 * However, if some activities share a lot of functionality, it might be useful to
 * create a separate interface that is then used by presenters
 * which are used by multiple activities.
 *
 * Future work: Add ability to specify caching preferences for various presenters
 * (never keep, keep max. 3, pre-cache 4, etc.)
 */
interface PresenterContext {

    /**
     * Every presenter context is connected to an activity.
     */
    val activity: Activity

    /**
     * Register a Presenter constructor.
     * The constructor takes an optional parent view and a PresenterContext.
     * The method will fail if there already is a registered constructor for the Presenter.
     */
    @MainThread
    fun <P: Presenter> register(clazz: Class<P>, factory: (PresenterContext, ViewGroup?) -> P): Unit

    /**
     * Register a Presenter constructor that does not require the parent view.
     */
    @MainThread
    fun <P: Presenter> register(clazz: Class<P>, factory: (PresenterContext) -> P): Unit
        = register(clazz) { context, parent -> factory(context) }

    /**
     * Register a Presenter constructor that automatically inflates a layout resource and passes it to the factory.
     */
    @MainThread
    fun <P: Presenter> register(clazz: Class<P>, @LayoutRes layout: Int, factory: (View, PresenterContext) -> P): Unit
        = register(clazz) { ctx, parent ->
            val view = LayoutInflater.from(ctx.activity).inflate(layout, parent, false)
            factory(view, ctx)
        }

    /**
     * Get a free instance of specific Presenter. The presenter will be either reused
     * or created from scratch.
     * The method will fail if there is no constructor for the presenter registered.
     *
     * Note: It is advised to delegate Presenter creation and recycling to a PresenterGroups.
     */
    @MainThread
    fun <P: Presenter> obtain(clazz: Class<out P>, parent: ViewGroup? = null): P

    /**
     * Return the Presenter to the context for later reuse or destruction.
     * The method will fail if the presenter is already recycled or managed by other context.
     *
     * Note: It is advised to delegate Presenter creation and recycling to a PresenterGroups.
     */
    @MainThread
    fun recycle(presenter: Presenter): Unit

}