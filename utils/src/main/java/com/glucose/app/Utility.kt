package com.glucose.app

import com.github.daemontus.glucose.utils.BuildConfig
import com.glucose.Log

fun transitionLog(message: String) {
    //if (BuildConfig.DEBUG) {
        Log.d("TRANSITION: $message")
    //}
}

fun lifecycleLog(message: String) {
    //if (BuildConfig.DEBUG) {
        Log.d("LIFECYCLE: $message")
    //}
}

fun Presenter<*>.lifecycleLog(message: String) {
    //if (BuildConfig.DEBUG) {
        Log.d("LIFECYCLE[$this]: $message")
    //}
}