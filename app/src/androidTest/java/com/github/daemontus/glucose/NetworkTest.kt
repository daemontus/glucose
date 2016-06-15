package com.github.daemontus.glucose

import android.app.Application
import android.test.ApplicationTestCase
import com.github.daemontus.glucose.utils.Log
import com.github.daemontus.glucose.utils.device.Network

class NetworkTest : ApplicationTestCase<Application>(Application::class.java) {

    companion object {
        init {
            Log.loggers += com.github.daemontus.glucose.utils.AndroidConsoleLogger()
        }
    }

    override fun setUp() {
        super.setUp()
        createApplication()
    }

    fun testIsConnected() {
        Log.i("Is connected: ${Network.isConnected(application)}")
    }

    fun testIsWifi() {
        Log.i("Is on wifi: ${Network.isWifi(application)}")
    }

    fun testCanDownloadLargeFiles() {
        Log.i("Can download large files: ${Network.canDownloadLargeFiles(application)}")
    }

    fun testWifiName() {
        Log.i("Wifi SSID: ${Network.getWifiName(application)}")
    }

}