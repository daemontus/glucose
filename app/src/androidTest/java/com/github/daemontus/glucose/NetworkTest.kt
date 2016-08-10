package com.github.daemontus.glucose

import android.support.test.filters.SmallTest
import android.support.test.rule.ActivityTestRule
import android.support.test.runner.AndroidJUnit4
import com.glucose.Log
import com.glucose.device.Network
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@SmallTest
class NetworkTest {


    @Rule
    @JvmField val rule: ActivityTestRule<EmptyActivity> = ActivityTestRule(EmptyActivity::class.java)

    @Test
    fun testIsConnected() {
        Log.i("Is connected: ${Network.isConnected(rule.activity)}")
    }

    @Test
    fun testIsWifi() {
        Log.i("Is on wifi: ${Network.isWifi(rule.activity)}")
    }

    @Test
    fun testCanDownloadLargeFiles() {
        Log.i("Can download large files: ${Network.canDownloadLargeFiles(rule.activity)}")
    }

    @Test
    fun testWifiName() {
        Log.i("Wifi ID: ${Network.getWifiName(rule.activity)}")
    }

}