package com.glucose.app

import android.app.Activity
import android.app.Application
import android.support.test.rule.ActivityTestRule
import com.glucose.app.presenter.ParentActivity
import com.glucose.app.presenter.ParentApp
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class ContextAccessorsTest {


    @Rule @JvmField
    val activityRule: ActivityTestRule<EmptyActivity> = ActivityTestRule(EmptyActivity::class.java)

    private val host = MockPresenterHost(activityRule)


    @Test
    fun contextAccessors_validActivityAccess() {
        val p = object : SimplePresenter(host) {
            val activity by ParentActivity(EmptyActivity::class.java)
        }
        assertEquals(activityRule.activity, p.activity)
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
        assertEquals(activityRule.activity.application, p.app)
    }

    class OtherApp : Application()

    @Test
    fun contextAccessors_invalidApplicationAccess() {
        val p = object : SimplePresenter(host) {
            val app by ParentApp(OtherApp::class.java)
        }
        assertFailsWith<ClassCastException> {
            assertEquals(activityRule.activity.application, p.app)
        }
    }

}