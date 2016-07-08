package com.glucose.app

import com.github.daemontus.glucose.utils.BuildConfig
import com.glucose.Log

fun transactionLog(message: String) {
    if (BuildConfig.DEBUG) {
        Log.d("TRANSACTION: $message")
    }
}

fun lifecycleLog(message: String) {
    if (BuildConfig.DEBUG) {
        Log.d("LIFECYCLE: $message")
    }
}

fun Presenter<*>.lifecycleLog(message: String) {
    if (BuildConfig.DEBUG) {
        Log.d("LIFECYCLE[$this]: $message")
    }
}