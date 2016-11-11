package com.glucose.app.presenter

import android.annotation.TargetApi
import android.content.res.Configuration
import android.os.Bundle
import android.os.PersistableBundle
import com.github.daemontus.glucose.core.BuildConfig
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import rx.Observable
import rx.android.schedulers.AndroidSchedulers
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

@RunWith(RobolectricTestRunner::class)
@Config(constants = BuildConfig::class, sdk = intArrayOf(23))
class RootActivityTest {

    private val controller = Robolectric.buildActivity(LifecycleObservingActivity::class.java)!!

    @Test
    fun rootActivity_simpleLifecycle() {
        val activity = controller.get()
        val host = activity.host
        controller.create()
        val root = host.root!! as LifecycleObservingPresenter
        root.assertAttached()
        controller.start()
        root.assertStarted()
        controller.resume()
        root.assertResumed()
        controller.pause()
        root.assertPaused()
        controller.stop()
        root.assertStopped()
        controller.destroy()
        root.assertDestroyed()
        assertFailsWith(LifecycleException::class) {
            activity.host
        }
    }

    @TargetApi(23)
    @Test
    fun rootActivity_saveState() {
        val activity = controller.create(bundle(LifecycleObservingPresenter::goBack.name with true)).get()
        val root = activity.host.root!! as LifecycleObservingPresenter
        assertTrue(root.goBack)
        val state = Bundle().apply { activity.onSaveInstanceState(this, PersistableBundle()) }
        controller.destroy()
        val controller2 = Robolectric.buildActivity(LifecycleObservingActivity::class.java)!!
        val activity2 = controller2.create(state).get()
        val root2 = activity2.host.root!! as LifecycleObservingPresenter
        assertTrue(root2.goBack)
    }

    @Test
    fun rootActivity_configurationChange() {
        val activity = controller.create().get()
        val host = activity.host
        val root = host.root!! as LifecycleObservingPresenter
        activity.onConfigurationChanged(Configuration())
        root.assertConfig()
    }

    @Test
    fun rootActivity_onBackPressed() {
        val activity = controller.create().get()
        val host = activity.host
        val root = host.root!! as LifecycleObservingPresenter
        Observable.fromCallable {
            activity.onBackPressed()
            root.assertBack()
        }.subscribeOn(AndroidSchedulers.mainThread())
                .toBlocking().first()
    }

    @Test
    fun rootActivity_permissionResult() {
        val activity = controller.create().get()
        val host = activity.host
        val root = host.root!! as LifecycleObservingPresenter
        Observable.fromCallable {
            activity.onRequestPermissionsResult(0, arrayOf(), intArrayOf())
            root.assertPermissionResult()
        }.subscribeOn(AndroidSchedulers.mainThread())
                .toBlocking().first()
    }

    @Test
    fun rootActivity_trimMemory() {
        val activity = controller.create().get()
        val host = activity.host
        val root = host.root!! as LifecycleObservingPresenter
        Observable.fromCallable {
            activity.onTrimMemory(1)
            root.assertMemory()
        }.subscribeOn(AndroidSchedulers.mainThread())
                .toBlocking().first()
    }

}