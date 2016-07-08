package com.glucose.app

import android.app.Activity
import android.content.ComponentCallbacks2
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.ViewGroup

class PresenterAppCompatActivity : AppCompatActivity(), PresenterContext {

    private val contextDelegate = ContextDelegate(this)

    override val activity: Activity = this

    override fun <P : Presenter<*>> register(clazz: Class<P>, factory: (PresenterContext, ViewGroup?) -> P) {
        contextDelegate.register(clazz, factory)
    }

    override fun <P : Presenter<*>> obtain(clazz: Class<out P>, parent: ViewGroup?): P {
        return contextDelegate.obtain(clazz, parent)
    }

    override fun recycle(presenter: Presenter<*>) {
        contextDelegate.recycle(presenter)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        contextDelegate.onCreate(savedInstanceState)
    }

    override fun onDestroy() {
        contextDelegate.onDestroy()
        super.onDestroy()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        contextDelegate.onSaveInstanceState(outState)
    }

    override fun onTrimMemory(level: Int) {
        super.onTrimMemory(level)
        if (level > ComponentCallbacks2.TRIM_MEMORY_RUNNING_LOW) contextDelegate.onLowMemory()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        contextDelegate.onLowMemory()
    }

}