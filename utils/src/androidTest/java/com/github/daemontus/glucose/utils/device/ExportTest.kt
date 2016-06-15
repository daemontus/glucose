package com.github.daemontus.glucose.utils.device

import android.app.Application
import android.test.ApplicationTestCase

class ExportTest : ApplicationTestCase<Application>(Application::class.java) {

    override fun setUp() {
        super.setUp()
        createApplication()
    }

    fun testOpen() {
        Export.view(application, "http://google.com")
    }

    fun testSend() {
        Export.send(application, "Some text")
    }

    fun testSendFacebook() {
        Export.send(application, "Some text", Export.App.Facebook)
    }

    fun testSendTwitter() {
        Export.send(application, "Some text", Export.App.Twitter)
    }
}