package com.github.daemontus.glucose

import android.support.test.rule.ActivityTestRule
import android.support.test.runner.AndroidJUnit4
import com.glucose.device.Export
import org.junit.Ignore
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ExportTest {

    @Rule
    @JvmField val rule: ActivityTestRule<EmptyActivity> = ActivityTestRule(EmptyActivity::class.java)

    //Only run these tests one by one because they are going to spam the device

    @Ignore
    @Test
    fun testOpen() {
        Export.view(rule.activity, "http://google.com")
    }

    @Ignore
    @Test
    fun testSend() {
        Export.send(rule.activity, "Some text")
    }

    @Ignore
    @Test
    fun testSendFacebook() {
        Export.send(rule.activity, "Some text", Export.App.Facebook)
    }

    @Ignore
    @Test
    fun testSendTwitter() {
        Export.send(rule.activity, "Some text", Export.App.Twitter)
    }
}