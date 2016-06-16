package com.github.daemontus.glucose

import android.app.Application
import com.github.daemontus.glucose.utils.AndroidConsoleLogger
import com.github.daemontus.glucose.utils.Log

class App : Application() {

    companion object {
        init {
            Log.loggers += AndroidConsoleLogger()
        }
    }
}