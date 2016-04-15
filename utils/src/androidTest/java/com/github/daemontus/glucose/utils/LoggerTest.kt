package com.github.daemontus.glucose.utils

import android.app.Application
import android.test.ApplicationTestCase
import android.util.Log

class LoggerTest : ApplicationTestCase<Application>(Application::class.java) {

    fun testLogLevels() {
        assertEquals(Log.DEBUG, LogLevel.DEBUG.androidLogLevel)
        assertEquals(Log.INFO, LogLevel.INFO.androidLogLevel)
        assertEquals(Log.WARN, LogLevel.WARNING.androidLogLevel)
        assertEquals(Log.ERROR, LogLevel.ERROR.androidLogLevel)
        assertEquals(Log.VERBOSE, LogLevel.VERBOSE.androidLogLevel)
    }

    fun testLogMessage() {
        logV { "This is a verbose message" }
        logD { "This is a debug message" }
        logI { "This is an info message" }
        logW { "This is a warning message" }
        logE { "This is an error message" }
    }

    fun testLogThrowable() {
        val e = IllegalArgumentException("Some problem")
        logV(e) { "This is a verbose message with a throwable" }
        logD(e) { "This is a debug message with a throwable" }
        logI(e) { "This is an info message with a throwable" }
        logW(e) { "This is a warning message with a throwable" }
        logE(e) { "This is an error message with a throwable" }
    }
}