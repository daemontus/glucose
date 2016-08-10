package com.github.daemontus.glucose

import android.support.test.filters.SmallTest
import android.support.test.rule.ActivityTestRule
import android.support.test.runner.AndroidJUnit4
import com.glucose.Log
import com.glucose.device.Phone
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@SmallTest
class PhoneTest {
    
    @Rule
    @JvmField val rule: ActivityTestRule<EmptyActivity> = ActivityTestRule(EmptyActivity::class.java)

    @Test
    fun getPhoneNumber() {
        Log.i("Phone number ${Phone.getPrimaryPhoneNumber(rule.activity)}")
    }

}