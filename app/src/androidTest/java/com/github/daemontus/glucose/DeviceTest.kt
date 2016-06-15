package com.github.daemontus.glucose

import android.app.Application
import android.test.ApplicationTestCase
import com.github.daemontus.glucose.utils.AndroidConsoleLogger
import com.github.daemontus.glucose.utils.Log
import com.github.daemontus.glucose.utils.device.Device

class DeviceTest : ApplicationTestCase<Application>(Application::class.java) {

    companion object {
        init {
            Log.loggers += AndroidConsoleLogger()
        }
    }

    override fun setUp() {
        super.setUp()
        createApplication()
    }

    fun testIsEmulator() {
        Log.i("Is device an emulator? ${Device.isEmulator()}")
    }

    fun testHasNavigationBar() {
        Log.i("Does device has a navigation bar? ${Device.hasNavigationBar(application)}")
    }

    fun testNavBarHeight() {
        Log.i("Navigation bar height in pixels: ${Device.getNavBarHeight(application)}")
    }

    fun testStatusBarHeight() {
        Log.i("Status bar height in pixels: ${Device.getStatusBarHeight(application)}")
    }

    fun testNumCores() {
        Log.i("Number of cores: ${Device.getNumCores()}")
    }

    fun testDeviceId() {
        Log.i("Device ID: ${Device.getDeviceId(application)}")
    }

    fun testAtLeastICS() {
        Log.i("Device is at least Ice Cream Sandwich: ${Device.OS.atLeastICS()}")
    }

    fun testAtLeastICSMR1() {
        Log.i("Device is at least Ice Cream Sandwich MR1: ${Device.OS.atLeastICSMR1()}")
    }

    fun testAtLeastJellyBean() {
        Log.i("Device is at least Jelly Bean: ${Device.OS.atLeastJellyBean()}")
    }

    fun testAtLeastJellyBeanMR1() {
        Log.i("Device is at least Jelly Bean MR1: ${Device.OS.atLeastJellyBeanMR1()}")
    }

    fun testAtLeastJellyBeanMR2() {
        Log.i("Device is at least Jelly Bean MR2: ${Device.OS.atLeastJellyBeanMR2()}")
    }

    fun testAtLeastKitKat() {
        Log.i("Device is at least Kit Kat: ${Device.OS.atLeastKitKat()}")
    }

    fun testAtLeastLollipop() {
        Log.i("Device is at least Lollipop: ${Device.OS.atLeastLollipop()}")
    }

    fun testAtLeastLollipopMR1() {
        Log.i("Device is at least Lollipop MR1: ${Device.OS.atLeastLollipopMR1()}")
    }

    fun testAtLeastMarshmallow() {
        Log.i("Device is at least Marshmallow: ${Device.OS.atLeastMarshmallow()}")
    }

}