package com.github.daemontus.glucose

import android.support.test.InstrumentationRegistry
import android.support.test.runner.AndroidJUnit4
import android.test.ActivityInstrumentationTestCase2
import com.glucose.AndroidConsoleLogger
import com.glucose.Log
import com.glucose.device.Phone
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class PhoneTest : ActivityInstrumentationTestCase2<ColorActivity>(ColorActivity::class.java) {

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
    fun getPhoneNumber() {
        Log.i("Phone number ${Phone.getPrimaryPhoneNumber(activity)}")
    }

}