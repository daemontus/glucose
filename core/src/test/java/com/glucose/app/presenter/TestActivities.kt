package com.glucose.app.presenter

import android.app.Activity
import android.os.Bundle
import android.widget.FrameLayout
import com.glucose.app.PresenterHost
import com.glucose.app.RootActivity

class EmptyActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(FrameLayout(this))
    }
}

class LifecycleObservingActivity : RootActivity(LifecycleObservingPresenter::class.java,
        bundle(LifecycleObservingPresenter::goBack.name with true)
) {

    init {
        presenterHost.factory.register(LifecycleObservingPresenter::class.java) { host, parent ->
            LifecycleObservingPresenter(host)
        }
    }

    val host: PresenterHost
        get() = this.presenterHost
}