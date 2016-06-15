package com.github.daemontus.glucose

import android.app.Application
import android.test.ApplicationTestCase
import com.github.daemontus.glucose.utils.AndroidConsoleLogger
import com.github.daemontus.glucose.utils.Log
import com.github.daemontus.glucose.utils.LogLevel

class LoggerTest : ApplicationTestCase<Application>(Application::class.java) {

    companion object {
        init {
            Log.loggers += AndroidConsoleLogger()
        }
    }

    fun testLogLevels() {
        assertEquals(android.util.Log.DEBUG, LogLevel.DEBUG.androidLogLevel)
        assertEquals(android.util.Log.INFO, LogLevel.INFO.androidLogLevel)
        assertEquals(android.util.Log.WARN, LogLevel.WARNING.androidLogLevel)
        assertEquals(android.util.Log.ERROR, LogLevel.ERROR.androidLogLevel)
        assertEquals(android.util.Log.VERBOSE, LogLevel.VERBOSE.androidLogLevel)
    }

    fun testLazyLogMessage() {
        Log.v { "This is a lazy verbose message" }
        Log.d { "This is a lazy debug message" }
        Log.i { "This is a lazy info message" }
        Log.w { "This is a lazy warning message" }
        Log.e { "This is a lazy error message" }
    }

    fun testLogMessage() {
        Log.v("This is a lazy verbose message")
        Log.d("This is a lazy debug message")
        Log.i("This is a lazy info message")
        Log.w("This is a lazy warning message")
        Log.e("This is a lazy error message")
    }

    fun testLogThrowable() {
        val e = IllegalArgumentException("Some problem")
        Log.v("This is a verbose message with a throwable", e)
        Log.d("This is a debug message with a throwable", e)
        Log.i("This is an info message with a throwable", e)
        Log.w("This is a warning message with a throwable", e)
        Log.e("This is an error message with a throwable", e)
    }
}