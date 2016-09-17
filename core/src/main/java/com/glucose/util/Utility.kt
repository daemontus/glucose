package com.glucose.util

import com.github.daemontus.glucose.core.BuildConfig
import com.glucose.app.Presenter
import timber.log.Timber


internal fun lifecycleLog(message: String) {
    /*if (BuildConfig.DEBUG) {
        Timber.d("LIFECYCLE: $message")
    }*/
}

internal fun Presenter.lifecycleLog(message: String) {
    /*if (BuildConfig.DEBUG) {
        Timber.d("LIFECYCLE[${this.javaClass.simpleName}]: $message")
    }*/
}