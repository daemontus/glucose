package com.glucose.app.presenter

import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import com.glucose.app.Presenter
import com.glucose.app.PresenterHost
import kotlin.test.assertTrue

class LifecycleTestingPresenter(
        host: PresenterHost,
        private val superAttach: Boolean = true,
        private val superStart: Boolean = true,
        private val superResume: Boolean = true,
        private val superPause: Boolean = true,
        private val superStop: Boolean = true,
        private val superDetach: Boolean = true,
        private val superDestroy: Boolean = true
) : Presenter(host, View(host.activity)) {

    var onAttachCalled = false
    var onStartCalled = false
    var onResumeCalled = false
    var onPauseCalled = false
    var onStopCalled = false
    var onDetachCalled = false
    var onDestroyCalled = false

    override fun onAttach(arguments: Bundle) {
        if (superAttach) super.onAttach(arguments)
        onAttachCalled = true
    }

    override fun onStart() {
        if (superStart) super.onStart()
        onStartCalled = true
    }

    override fun onResume() {
        if (superResume) super.onResume()
        onResumeCalled = true
    }

    override fun onPause() {
        if (superPause) super.onPause()
        onPauseCalled = true
    }

    override fun onStop() {
        if (superStop) super.onStop()
        onStopCalled = true
    }

    override fun onDetach() {
        if (superDetach) super.onDetach()
        onDetachCalled = true
    }

    override fun onDestroy() {
        if (superDestroy) super.onDestroy()
        onDestroyCalled = true
    }
}


class NoReflectionPresenter(host: PresenterHost) : Presenter(host, View(host.activity))

class CanChangeConfiguration(host: PresenterHost, @Suppress("UNUSED_PARAMETER") parent: ViewGroup?) : Presenter(host, View(host.activity)) {

    var config: Configuration? = null

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        config = newConfig
    }
}

class CantChangeConfiguration(host: PresenterHost, @Suppress("UNUSED_PARAMETER") parent: ViewGroup?) : Presenter(host, View(host.activity)) {
    override val canChangeConfiguration: Boolean = false

    var data by NativeState(0, intBundler)
}

class PresenterWithState(host: PresenterHost, @Suppress("UNUSED_PARAMETER") parent: ViewGroup?) : Presenter(host, View(host.activity)) {
    var data by NativeState(0, intBundler)
}

class LifecycleObservingPresenter(host: PresenterHost)
: Presenter(host, View(host.activity)) {

    val goBack by NativeState(false, booleanBundler)

    private var attached = false

    fun assertAttached() {
        assertTrue(attached)
        attached = false
    }

    private var started = false

    fun assertStarted() {
        assertTrue(started)
        started = false
    }

    private var resumed = false

    fun assertResumed() {
        assertTrue(resumed)
        resumed = false
    }

    private var paused = false

    fun assertPaused() {
        assertTrue(paused)
        paused = false
    }

    private var stopped = false

    fun assertStopped() {
        assertTrue(stopped)
        stopped = false
    }

    private var detached = false

    fun assertDetached() {
        assertTrue(detached)
        detached = false
    }

    private var destroyed = false

    fun assertDestroyed() {
        assertTrue(destroyed)
        destroyed = false
    }

    private var back = false

    fun assertBack() {
        assertTrue(back)
        back = false
    }

    private var activityResult = false

    fun assertActivityResult() {
        assertTrue(activityResult)
        activityResult = false
    }

    private var permissionResult = false

    fun assertPermissionResult() {
        assertTrue(permissionResult)
        permissionResult = false
    }

    private var memory = false

    fun assertMemory() {
        assertTrue(memory)
        memory = false
    }

    private var config = false

    fun assertConfig() {
        assertTrue(config)
        config = false
    }


    override fun onAttach(arguments: Bundle) {
        super.onAttach(arguments)
        attached = true
    }

    override fun onStart() {
        super.onStart()
        started = true
    }

    override fun onResume() {
        super.onResume()
        resumed = true
    }

    override fun onStop() {
        super.onStop()
        stopped = true
    }

    override fun onPause() {
        super.onPause()
        paused = true
    }

    override fun onDetach() {
        super.onDetach()
        detached = true
    }

    override fun onDestroy() {
        super.onDestroy()
        destroyed = true
    }

    override fun onBackPressed(): Boolean {
        back = true
        return super.onBackPressed()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        activityResult = true
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        permissionResult = true
    }

    override fun onTrimMemory(level: Int) {
        super.onTrimMemory(level)
        memory = true
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        config = true
    }
}


open class SimplePresenter(host: PresenterHost, val flag: Boolean = true)
: Presenter(host, View(host.activity)) {

    override val canBeReused: Boolean = flag

    //reflection constructor
    @Suppress("unused")
    constructor(host: PresenterHost, @Suppress("UNUSED_PARAMETER") parent: ViewGroup?) : this(host, true)

}