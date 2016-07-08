package com.github.daemontus.glucose

import android.support.test.InstrumentationRegistry
import android.support.test.runner.AndroidJUnit4
import android.test.ActivityInstrumentationTestCase2
import com.glucose.device.Export
import org.junit.Before
import org.junit.Ignore
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ExportTest : ActivityInstrumentationTestCase2<ColorActivity>(ColorActivity::class.java) {

    @Before
    fun init() {
        injectInstrumentation(InstrumentationRegistry.getInstrumentation())
    }

    //Only run these tests one by one

    @Ignore
    @Test
    fun testOpen() {
        Export.view(activity, "http://google.com")
    }

    @Ignore
    @Test
    fun testSend() {
        Export.send(activity, "Some text")
    }

    @Ignore
    @Test
    fun testSendFacebook() {
        Export.send(activity, "Some text", Export.App.Facebook)
    }

    @Ignore
    @Test
    fun testSendTwitter() {
        Export.send(activity, "Some text", Export.App.Twitter)
    }
}