package com.glucose.app

import android.support.test.rule.ActivityTestRule
import android.support.test.runner.AndroidJUnit4
import android.view.View
import android.widget.FrameLayout
import com.github.daemontus.glucose.core.test.R
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@RunWith(AndroidJUnit4::class)
class PresenterTest {


    @Rule @JvmField
    val activityRule: ActivityTestRule<EmptyActivity> = ActivityTestRule(EmptyActivity::class.java)

    private val host = MockPresenterHost(activityRule)

    @Test
    fun presenter_createWithArtificialView() {
        val view = View(activityRule.activity)
        val p = Presenter(host, view)
        assertEquals(view, p.view)
    }

    @Test
    fun presenter_createWithInflatedView() {
        val p = Presenter(host, R.layout.presenter_test, null)
        assertTrue(p.view is FrameLayout)
        assertEquals(R.id.inflated_view, p.view.id)
    }



}