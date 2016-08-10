package com.github.daemontus.glucose

import android.support.test.filters.SmallTest
import android.support.test.rule.ActivityTestRule
import android.support.test.runner.AndroidJUnit4
import com.glucose.Log
import com.glucose.LogLevel
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import kotlin.test.assertEquals

@RunWith(AndroidJUnit4::class)
@SmallTest
class LoggerTest {


    @Rule
    @JvmField val rule: ActivityTestRule<EmptyActivity> = ActivityTestRule(EmptyActivity::class.java)

    @Test
    fun testLogLevels() {
        assertEquals(android.util.Log.DEBUG, LogLevel.DEBUG.androidLogLevel)
        assertEquals(android.util.Log.INFO, LogLevel.INFO.androidLogLevel)
        assertEquals(android.util.Log.WARN, LogLevel.WARNING.androidLogLevel)
        assertEquals(android.util.Log.ERROR, LogLevel.ERROR.androidLogLevel)
        assertEquals(android.util.Log.VERBOSE, LogLevel.VERBOSE.androidLogLevel)
    }

    @Test
    fun testLazyLogMessage() {
        Log.v { "This is a lazy verbose message" }
        Log.d { "This is a lazy debug message" }
        Log.i { "This is a lazy info message" }
        Log.w { "This is a lazy warning message" }
        Log.e { "This is a lazy error message" }
    }

    @Test
    fun testLogMessage() {
        Log.v("This is a lazy verbose message")
        Log.d("This is a lazy debug message")
        Log.i("This is a lazy info message")
        Log.w("This is a lazy warning message")
        Log.e("This is a lazy error message")
    }

    @Test
    fun testLogThrowable() {
        val e = IllegalArgumentException("Some problem")
        Log.v("This is a verbose message with a throwable", e)
        Log.d("This is a debug message with a throwable", e)
        Log.i("This is an info message with a throwable", e)
        Log.w("This is a warning message with a throwable", e)
        Log.e("This is an error message with a throwable", e)
    }
}