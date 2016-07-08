package com.github.daemontus.glucose

import android.support.test.InstrumentationRegistry
import android.support.test.runner.AndroidJUnit4
import android.test.ActivityInstrumentationTestCase2
import com.glucose.device.Keyboard
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class KeyboardTest : ActivityInstrumentationTestCase2<ColorActivity>(ColorActivity::class.java) {

    @Before
    fun init() {
        injectInstrumentation(InstrumentationRegistry.getInstrumentation())
    }

    @Test
    fun openKeyboard() {
        //Keyboard
        val content = activity.findViewById(android.R.id.content)!!
        Keyboard.show(activity, content)
    }

    @Test
    fun hideKeyboard() {
        //Keyboard
        val content = activity.findViewById(android.R.id.content)!!
        Keyboard.hide(activity, content)
    }
}