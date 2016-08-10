package com.github.daemontus.glucose

import com.glucose.Log
import com.glucose.device.Units
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.shadows.ShadowLog

@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE)
class UnitsTest2 {

    val activity: EmptyActivity = Robolectric.setupActivity(EmptyActivity::class.java)

    @Before
    fun setUp() {
        ShadowLog.stream = System.out
    }

    @Test
    fun dpToPx() {
        Log.i("10dp is ${Units.dpToPx(activity, 10f)}px")
    }

    @Test
    fun pxToDp() {
        Log.i("10px is ${Units.pxToDp(activity, 10f)}dp")
    }

}