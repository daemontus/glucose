package com.github.daemontus.glucose

import android.support.test.filters.SmallTest
import android.support.test.rule.ActivityTestRule
import android.support.test.runner.AndroidJUnit4
import com.glucose.Log
import com.glucose.device.Units
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@SmallTest
class UnitsTest {

    @Rule
    @JvmField val rule: ActivityTestRule<EmptyActivity> = ActivityTestRule(EmptyActivity::class.java)

    @Test
    fun dpToPx() {
        Log.i("10dp is ${Units.dpToPx(rule.activity, 10f)}px")
    }

    @Test
    fun pxToDp() {
        Log.i("10px is ${Units.pxToDp(rule.activity, 10f)}dp")
    }

}