package com.glucose.app.presenter

import android.app.Activity
import android.app.Application
import com.github.daemontus.glucose.core.BuildConfig
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

@RunWith(RobolectricTestRunner::class)
@Config(constants = BuildConfig::class, sdk = intArrayOf(21))
class ContextAccessorsTest {

    private val activity = setupEmptyActivity()

    private val host = MockPresenterHost(activity)


    @Test
    fun contextAccessors_validActivityAccess() {
        val p = object : SimplePresenter(host) {
            val activity by ParentActivity(EmptyActivity::class.java)
        }
        assertEquals(activity, p.activity)
        //Reified methods don't work with Jacoco for some reason...
        //assertEquals(activityRule.activity, p.getActivity<EmptyActivity>())
    }

    class OtherActivity : Activity()

    @Test
    fun contextAccessors_invalidActivityAccess() {
        val p = object : SimplePresenter(host) {
            val activity by ParentActivity(OtherActivity::class.java)
        }
        assertFailsWith<ClassCastException> {
            p.activity
        }
    }

    @Test
    fun contextAccessors_validApplicationAccess() {
        val p = object : SimplePresenter(host) {
            val app by ParentApp(Application::class.java)
        }
        assertEquals(activity.application, p.app)
    }

    class OtherApp : Application()

    @Test
    fun contextAccessors_invalidApplicationAccess() {
        val p = object : SimplePresenter(host) {
            val app by ParentApp(OtherApp::class.java)
        }
        assertFailsWith<ClassCastException> {
            assertEquals(activity.application, p.app)
        }
    }

}