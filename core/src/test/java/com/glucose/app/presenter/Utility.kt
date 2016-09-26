package com.glucose.app.presenter

import org.robolectric.Robolectric

fun setupEmptyActivity() = Robolectric.buildActivity(EmptyActivity::class.java).create().get()!!