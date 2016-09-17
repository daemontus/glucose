package com.glucose.app

import android.os.Bundle
import android.support.test.rule.ActivityTestRule
import android.support.test.runner.AndroidJUnit4
import com.glucose.app.presenter.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import kotlin.test.assertEquals

@RunWith(AndroidJUnit4::class)
class BundleUtilsTest {

    @Rule @JvmField
    val activityRule: ActivityTestRule<EmptyActivity> = ActivityTestRule(EmptyActivity::class.java)

    private val host = MockPresenterHost(activityRule)

    @Test
    fun bundleUtils_nativeArguments() {
        val p = object : PresenterFactoryTest.SimplePresenter(host) {
            val intArgument by NativeArgument(-1, intBundler)
            var intState by NativeState(-1, intBundler)
        }
        p.performAttach(Bundle())
        assertEquals(-1, p.intArgument)
        assertEquals(-1, p.intState)
        p.performDetach()
        p.performAttach(("intArgument" with 10) and ("intState" with 20))
        assertEquals(10, p.intArgument)
        assertEquals(20, p.intState)
        p.intState = 15
        assertEquals(15, p.intState)
        val state = Bundle().apply {
            p.onSaveInstanceState(this)
        }
        p.performDetach()
        p.performAttach(state)
        assertEquals(10, p.intArgument)
        assertEquals(15, p.intState)
    }

}