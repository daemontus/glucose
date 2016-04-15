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
        LogV { "This is a verbose message" }
        LogD { "This is a debug message" }
        LogI { "This is an info message" }
        LogW { "This is a warning message" }
        LogE { "This is an error message" }
    }

    fun testLogThrowable() {
        val e = IllegalArgumentException("Some problem")
        LogV(e) { "This is a verbose message with a throwable" }
        LogD(e) { "This is a debug message with a throwable" }
        LogI(e) { "This is an info message with a throwable" }
        LogW(e) { "This is a warning message with a throwable" }
        LogE(e) { "This is an error message with a throwable" }
    }
}