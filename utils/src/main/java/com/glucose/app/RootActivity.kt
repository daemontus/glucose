package com.glucose.app

import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.widget.FrameLayout
import kotlin.properties.Delegates

//TODO: Merge this with ContextDelegate - or at least refactor it into some primitive PresenterGroup
abstract class RootActivity : PresenterActivity() {

    abstract val rootPresenter: Class<out Presenter>
    val rootArguments: Bundle = Bundle()

    private var root: Presenter by Delegates.notNull()
    private var frame: FrameLayout by Delegates.notNull()

    private var started = false
    private var resumed = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        frame = FrameLayout(this)
        setContentView(frame)
        root = obtain(rootPresenter, frame)
        root.performAttach(rootArguments)
        frame.addView(root.view)
    }

    override fun onStart() {
        super.onStart()
        started = true
        root.performStart()
    }

    override fun onResume() {
        super.onResume()
        resumed = true
        root.performResume()
    }

    override fun onBackPressed() {
        if (!root.onBackPressed()) super.onBackPressed()
    }

    override fun onPause() {
        super.onPause()
        root.performPause()
        resumed = false
    }

    override fun onStop() {
        super.onStop()
        root.performStop()
        started = false
    }

    override fun onDestroy() {
        frame.removeView(root.view)
        root.performDetach()
        recycle(root)
        super.onDestroy()
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        val keepRoot = root.canChangeConfiguration
        if (keepRoot) {
            root.performConfigurationChange(newConfig)
        } else {
            if (resumed) root.performPause()
            if (started) root.performStop()
            root.performDetach()
            frame.removeView(root.view)
            recycle(root)
        }
        super.onConfigurationChanged(newConfig)
        root = obtain(rootPresenter, frame)
        root.performAttach(rootArguments)
        frame.addView(root.view)
        if (started) root.performStart()
        if (resumed) root.performResume()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
        super.onActivityResult(requestCode, resultCode, data)
        root.performActivityResult(requestCode, resultCode, data)
    }
}

