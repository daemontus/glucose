package com.github.daemontus.glucose

import android.support.test.InstrumentationRegistry
import android.support.test.runner.AndroidJUnit4
import android.test.ActivityInstrumentationTestCase2
import com.glucose.AndroidConsoleLogger
import com.glucose.Log
import com.glucose.device.Screen
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ScreenTest : ActivityInstrumentationTestCase2<ColorActivity>(ColorActivity::class.java) {

    companion object {
        init {
            Log.loggers += AndroidConsoleLogger()
        }
    }

    @Before
    fun init() {
        injectInstrumentation(InstrumentationRegistry.getInstrumentation())
    }

    @Test
    fun getRealWidth() {
        Log.i("Real width: ${Screen.getRealWidth(activity)}")
    }

    @Test
    fun getRealHeight() {
        Log.i("Real height: ${Screen.getRealHeight(activity)}")
    }

    @Test
    fun getWidth() {
        Log.i("Width: ${Screen.getWidth(activity)}")
    }

    @Test
    fun getHeight() {
        Log.i("Height: ${Screen.getHeight(activity)}")
    }

}