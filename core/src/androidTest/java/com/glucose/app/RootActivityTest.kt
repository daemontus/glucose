package com.glucose.app

import android.content.pm.ActivityInfo
import android.support.test.rule.ActivityTestRule
import android.support.test.runner.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import rx.Observable
import rx.android.schedulers.AndroidSchedulers

@RunWith(AndroidJUnit4::class)
class RootActivityTest {
/*
    @Rule @JvmField
    val activityRule: ActivityTestRule<LifecycleObservingActivity> = ActivityTestRule(LifecycleObservingActivity::class.java)

    @Test
    fun rootActivity_simpleLifecycle() {
        val activity = activityRule.activity
        val host = activity.host
        val root = host.root!! as LifecycleObservingPresenter
        root.assertAttached()
        root.assertStarted()
        root.assertResumed()
    }

    @Test
    fun rootActivity_configurationChange() {
        val activity = activityRule.activity
        val host = activity.host
        val root = host.root!! as LifecycleObservingPresenter
        activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        root.assertConfig()
    }

    @Test
    fun rootActivity_onBackPressed() {
        val activity = activityRule.activity
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
        val activity = activityRule.activity
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
        val activity = activityRule.activity
        val host = activity.host
        val root = host.root!! as LifecycleObservingPresenter
        Observable.fromCallable {
            activity.onTrimMemory(1)
            root.assertMemory()
        }.subscribeOn(AndroidSchedulers.mainThread())
                .toBlocking().first()
    }

*/
}