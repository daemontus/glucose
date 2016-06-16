package com.github.daemontus.glucose

import android.support.test.InstrumentationRegistry
import android.support.test.runner.AndroidJUnit4
import android.test.ActivityInstrumentationTestCase2
import com.github.daemontus.glucose.utils.AndroidConsoleLogger
import com.github.daemontus.glucose.utils.Log
import com.github.daemontus.glucose.utils.device.Phone
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class PhoneTest : ActivityInstrumentationTestCase2<MainActivity>(MainActivity::class.java) {

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