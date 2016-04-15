package com.github.daemontus.glucose.utils

import android.app.Application
import android.test.ApplicationTestCase
import com.github.daemontus.glucose.utils.device.Device

class DeviceInfoTest : ApplicationTestCase<Application>(Application::class.java) {

    override fun setUp() {
        super.setUp()
        createApplication()
    }

    fun testIsEmulator() {
        logI { "Is device an emulator? ${Device.isEmulator()}"}
    }

    fun testHasNavigationBar() {
        logI { "Does device has a navigation bar? ${Device.hasNavigationBar(application)}" }
    }

    fun testNavBarHeight() {
        logI { "Navigation bar height in pixels: ${Device.getNavBarHeight(application)}" }
    }

    fun testStatusBarHeight() {
        logI { "Status bar height in pixels: ${Device.getStatusBarHeight(application)}" }
    }

    fun testNumCores() {
        logI { "Number of cores: ${Device.getNumCores()}" }
    }

    fun testDeviceId() {
        logI { "Device ID: ${Device.getDeviceId(application)}" }
    }

    fun testAtLeastICS() {
        logI { "Device is at least Ice Cream Sandwich: ${Device.OS.atLeastICS()}" }
    }

    fun testAtLeastICSMR1() {
        logI { "Device is at least Ice Cream Sandwich MR1: ${Device.OS.atLeastICSMR1()}" }
    }

    fun testAtLeastJellyBean() {
        logI { "Device is at least Jelly Bean: ${Device.OS.atLeastJellyBean()}" }
    }

    fun testAtLeastJellyBeanMR1() {
        logI { "Device is at least Jelly Bean MR1: ${Device.OS.atLeastJellyBeanMR1()}" }
    }

    fun testAtLeastJellyBeanMR2() {
        logI { "Device is at least Jelly Bean MR2: ${Device.OS.atLeastJellyBeanMR2()}" }
    }

    fun testAtLeastKitKat() {
        logI { "Device is at least Kit Kat: ${Device.OS.atLeastKitKat()}" }
    }

    fun testAtLeastLollipop() {
        logI { "Device is at least Lollipop: ${Device.OS.atLeastLollipop()}" }
    }

    fun testAtLeastLollipopMR1() {
        logI { "Device is at least Lollipop MR1: ${Device.OS.atLeastLollipopMR1()}" }
    }

    fun testAtLeastMarshmallow() {
        logI { "Device is at least Marshmallow: ${Device.OS.atLeastMarshmallow()}" }
    }

}