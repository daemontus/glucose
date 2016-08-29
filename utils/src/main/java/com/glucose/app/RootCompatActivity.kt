package com.glucose.app

import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.ViewGroup
import kotlin.properties.Delegates

/**
 * An activity that is connected to a [PresenterContext] and has exactly one root [Presenter].
 */
abstract class RootCompatActivity(
        rootPresenter: Class<out Presenter>,
        rootArguments: Bundle = Bundle()
) : AppCompatActivity() {

    private val presenterContext = PresenterContext(this, rootPresenter, rootArguments)

    private var rootView: ViewGroup by Delegates.notNull()

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
        rootView.removeAllViews()
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

    override fun onTrimMemory(level: Int) {
        super.onTrimMemory(level)
        presenterContext.onTrimMemory(level)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        presenterContext.onSaveInstanceState(outState)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        presenterContext.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }
}

