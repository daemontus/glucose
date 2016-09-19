package com.glucose.app

import android.annotation.TargetApi
import android.app.Activity
import android.content.Intent
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle

/**
 * An activity that is connected to a [PresenterDelegate] and has exactly one root [Presenter].
 */
abstract class RootActivity(
        rootPresenter: Class<out Presenter>,
        rootArguments: Bundle = Bundle()
) : Activity() {

    private val presenterContext = PresenterDelegate(this, rootPresenter, rootArguments)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(presenterContext.onCreate(savedInstanceState))
    }

    override fun onStart() {
        super.onStart()
        presenterContext.onStart()
    }

    override fun onResume() {
        super.onResume()
        presenterContext.onResume()
    }

    override fun onBackPressed() {
        if (!presenterContext.onBackPressed()) super.onBackPressed()
    }

    override fun onPause() {
        presenterContext.onPause()
        super.onPause()
    }

    override fun onStop() {
        presenterContext.onStop()
        super.onStop()
    }

    override fun onDestroy() {
        presenterContext.onDestroy()
        super.onDestroy()
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        presenterContext.onConfigurationChanged(newConfig)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
        super.onActivityResult(requestCode, resultCode, data)
        presenterContext.onActivityResult(requestCode, resultCode, data)
    }

    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    override fun onTrimMemory(level: Int) {
        super.onTrimMemory(level)
        presenterContext.onTrimMemory(level)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        presenterContext.onSaveInstanceState(outState)
    }

    @TargetApi(Build.VERSION_CODES.M)
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        presenterContext.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }
}

