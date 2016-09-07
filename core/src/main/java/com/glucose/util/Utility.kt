package com.glucose.util

import android.os.Looper
import com.github.daemontus.Result
import com.github.daemontus.glucose.BuildConfig
import com.glucose.app.Presenter
import rx.Observable
import timber.log.Timber
import java.util.concurrent.atomic.AtomicInteger


internal fun actionLog(message: String) {
    if (BuildConfig.DEBUG) {
        Timber.d("TRANSITION $message")
    }
}

internal fun lifecycleLog(message: String) {
    if (BuildConfig.DEBUG) {
        Timber.d("LIFECYCLE: $message")
    }
}

internal fun Presenter.lifecycleLog(message: String) {
    if (BuildConfig.DEBUG) {
        Timber.d("LIFECYCLE[${this.javaClass.simpleName}]: $message")
    }
}

internal fun mainThread() = Looper.myLooper() == Looper.getMainLooper()