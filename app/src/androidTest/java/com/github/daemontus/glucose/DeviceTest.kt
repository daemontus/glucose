package com.github.daemontus.glucose

import android.support.test.InstrumentationRegistry
import android.support.test.runner.AndroidJUnit4
import android.test.ActivityInstrumentationTestCase2
import com.github.daemontus.glucose.utils.AndroidConsoleLogger
import com.github.daemontus.glucose.utils.Log
import com.github.daemontus.glucose.utils.device.Device
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class DeviceTest : ActivityInstrumentationTestCase2<MainActivity>(MainActivity::class.java) {

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
    fun testIsEmulator() {
        Log.i("Is device an emulator? ${Device.isEmulator()}")
    }

    @Test
    fun testHasNavigationBar() {
        Log.i("Does device has a navigation bar? ${Device.hasNavigationBar(activity)}")
    }

    @Test
    fun testNavBarHeight() {
        Log.i("Navigation bar height in pixels: ${Device.getNavBarHeight(activity)}")
    }

    @Test
    fun testStatusBarHeight() {
        Log.i("Status bar height in pixels: ${Device.getStatusBarHeight(activity)}")
    }

    @Test
    fun testNumCores() {
        Log.i("Number of cores: ${Device.getNumCores()}")
    }

    @Test
    fun testDeviceId() {
        Log.i("Device ID: ${Device.getDeviceId(activity)}")
    }

    @Test
    fun testAtLeastICS() {
        Log.i("Device is at least Ice Cream Sandwich: ${Device.OS.atLeastICS()}")
    }

    @Test
    fun testAtLeastICSMR1() {
        Log.i("Device is at least Ice Cream Sandwich MR1: ${Device.OS.atLeastICSMR1()}")
    }

    @Test
    fun testAtLeastJellyBean() {
        Log.i("Device is at least Jelly Bean: ${Device.OS.atLeastJellyBean()}")
    }

    @Test
    fun testAtLeastJellyBeanMR1() {
        Log.i("Device is at least Jelly Bean MR1: ${Device.OS.atLeastJellyBeanMR1()}")
    }

    @Test
    fun testAtLeastJellyBeanMR2() {
        Log.i("Device is at least Jelly Bean MR2: ${Device.OS.atLeastJellyBeanMR2()}")
    }

    @Test
    fun testAtLeastKitKat() {
        Log.i("Device is at least Kit Kat: ${Device.OS.atLeastKitKat()}")
    }

    @Test
    fun testAtLeastLollipop() {
        Log.i("Device is at least Lollipop: ${Device.OS.atLeastLollipop()}")
    }

    @Test
    fun testAtLeastLollipopMR1() {
        Log.i("Device is at least Lollipop MR1: ${Device.OS.atLeastLollipopMR1()}")
    }

    @Test
    fun testAtLeastMarshmallow() {
        Log.i("Device is at least Marshmallow: ${Device.OS.atLeastMarshmallow()}")
    }

}