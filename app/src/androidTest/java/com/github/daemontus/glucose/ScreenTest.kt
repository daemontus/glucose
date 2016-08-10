package com.github.daemontus.glucose

import android.support.test.filters.SmallTest
import android.support.test.rule.ActivityTestRule
import android.support.test.runner.AndroidJUnit4
import com.glucose.Log
import com.glucose.device.Screen
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@SmallTest
class ScreenTest {

    @Rule
    @JvmField val rule: ActivityTestRule<EmptyActivity> = ActivityTestRule(EmptyActivity::class.java)

    @Test
    fun getRealWidth() {
        Log.i("Real width: ${Screen.getRealWidth(rule.activity)}")
    }

    @Test
    fun getRealHeight() {
        Log.i("Real height: ${Screen.getRealHeight(rule.activity)}")
    }

    @Test
    fun getWidth() {
        Log.i("Width: ${Screen.getWidth(rule.activity)}")
    }

    @Test
    fun getHeight() {
        Log.i("Height: ${Screen.getHeight(rule.activity)}")
    }

}