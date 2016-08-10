package com.github.daemontus.glucose

import android.support.test.filters.SmallTest
import android.support.test.rule.ActivityTestRule
import android.support.test.runner.AndroidJUnit4
import com.glucose.device.Keyboard
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@SmallTest
class KeyboardTest {

    @Rule
    @JvmField val rule: ActivityTestRule<EmptyActivity> = ActivityTestRule(EmptyActivity::class.java)

    @Test
    fun openKeyboard() {
        val content = rule.activity.findViewById(android.R.id.content)!!
        Keyboard.show(rule.activity, content)
    }

    @Test
    fun hideKeyboard() {
        val content = rule.activity.findViewById(android.R.id.content)!!
        Keyboard.hide(rule.activity, content)
    }
}