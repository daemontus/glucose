package com.glucose.app

import android.os.Bundle
import android.widget.FrameLayout
import kotlin.properties.Delegates

//TODO: Merge this with ContextDelegate - or at least refactor it into some primitive PresenterGroup
abstract class RootActivity : PresenterActivity() {

    abstract val rootPresenter: Class<out Presenter<*>>
    val rootArguments: Bundle = Bundle()

    private var root: Presenter<*> by Delegates.notNull()
    private var frame: FrameLayout by Delegates.notNull()

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
        root.performStart()
    }

    override fun onResume() {
        super.onResume()
        root.performResume()
    }

    override fun onBackPressed() {
        if (!root.onBackPressed()) super.onBackPressed()
    }

    override fun onPause() {
        super.onPause()
        root.performPause()
    }

    override fun onStop() {
        super.onStop()
        root.performStop()
    }

    override fun onDestroy() {
        frame.removeView(root.view)
        root.performDetach()
        recycle(root)
        super.onDestroy()
    }
}

