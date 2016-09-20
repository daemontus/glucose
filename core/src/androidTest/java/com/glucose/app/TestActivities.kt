package com.glucose.app

import android.app.Activity
import android.os.Bundle
import android.widget.FrameLayout
import com.glucose.app.presenter.NativeArgument
import com.glucose.app.presenter.booleanBundler
import com.glucose.app.presenter.bundle
import com.glucose.app.presenter.with

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

    val host = this.presenterHost
}