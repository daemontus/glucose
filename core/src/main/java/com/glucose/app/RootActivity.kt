package com.glucose.app

import android.annotation.TargetApi
import android.app.Activity
import android.content.Intent
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import kotlin.properties.Delegates

/**
 * An activity that is connected to a [PresenterDelegate] and has exactly one root [Presenter].
 */
abstract class RootActivity(
        private val rootPresenter: Class<out Presenter>,
        private val rootArguments: Bundle = Bundle()
) : Activity() {

    protected var presenterHost by Delegates.notNull<PresenterDelegate>()
        private set

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        presenterHost = PresenterDelegate(this, rootPresenter, rootArguments)
        setContentView(presenterHost.onCreate(savedInstanceState))
    }

    override fun onStart() {
        super.onStart()
        presenterHost.onStart()
    }

    override fun onResume() {
        super.onResume()
        presenterHost.onResume()
    }

    override fun onBackPressed() {
        if (!presenterHost.onBackPressed()) super.onBackPressed()
    }

    override fun onPause() {
        presenterHost.onPause()
        super.onPause()
    }

    override fun onStop() {
        presenterHost.onStop()
        super.onStop()
    }

    override fun onDestroy() {
        presenterHost.onDestroy()
        super.onDestroy()
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        presenterHost.onConfigurationChanged(newConfig)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
        super.onActivityResult(requestCode, resultCode, data)
        presenterHost.onActivityResult(requestCode, resultCode, data)
    }

    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    override fun onTrimMemory(level: Int) {
        super.onTrimMemory(level)
        presenterHost.onTrimMemory(level)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        presenterHost.onSaveInstanceState(outState)
    }

    @TargetApi(Build.VERSION_CODES.M)
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        presenterHost.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }
}

