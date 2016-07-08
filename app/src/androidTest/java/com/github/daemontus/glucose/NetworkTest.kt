package com.github.daemontus.glucose

import android.support.test.InstrumentationRegistry
import android.support.test.runner.AndroidJUnit4
import android.test.ActivityInstrumentationTestCase2
import com.glucose.Log
import com.glucose.device.Network
import com.glucose.AndroidConsoleLogger
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class NetworkTest : ActivityInstrumentationTestCase2<ColorActivity>(ColorActivity::class.java) {

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
    fun testIsConnected() {
        Log.i("Is connected: ${Network.isConnected(activity)}")
    }

    @Test
    fun testIsWifi() {
        Log.i("Is on wifi: ${Network.isWifi(activity)}")
    }

    @Test
    fun testCanDownloadLargeFiles() {
        Log.i("Can download large files: ${Network.canDownloadLargeFiles(activity)}")
    }

    @Test
    fun testWifiName() {
        Log.i("Wifi ID: ${Network.getWifiName(activity)}")
    }

}