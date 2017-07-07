package com.glucose2.app

import android.app.Activity
import android.content.res.Configuration
import android.os.Bundle
import android.view.ViewGroup
import android.widget.FrameLayout

abstract class ComponentActivity : Activity() {

    abstract val rootComponent: Class<out Component>

    private lateinit var delegate: ComponentHostDelegate

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val rootView = FrameLayout(this)
        setContentView(rootView, ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
        ))

        delegate = ComponentHostDelegate(rootComponent, rootView, this)

        val state = savedInstanceState?.getBundle("glucose-state") ?: Bundle()
        delegate.attach(state)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>?, grantResults: IntArray?) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        delegate.rootEventHostDelegate.emitAction(PermissionAction(requestCode, permissions, grantResults))
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

        val state = delegate.saveInstanceState()
        outState.putBundle("glucose-state", state)
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        delegate.configurationChange(newConfig)
        super.onConfigurationChanged(newConfig)
    }

    override fun onStart() {
        super.onStart()
        delegate.start()
    }

    override fun onResume() {
        super.onResume()
        delegate.resume()
    }

    override fun onPause() {
        delegate.pause()
        super.onPause()
    }

    override fun onStop() {
        delegate.stop()
        super.onStop()
    }

    override fun onDestroy() {
        delegate.detach()
        delegate.destroy()
        super.onDestroy()
    }
}