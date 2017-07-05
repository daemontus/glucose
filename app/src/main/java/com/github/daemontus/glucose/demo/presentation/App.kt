package com.github.daemontus.glucose.demo.presentation

import android.app.Activity
import android.app.Application
import timber.log.Timber

class App : Application() {
    override fun onCreate() {
        super.onCreate()
        Timber.plant(Timber.DebugTree())
    }
}

class PresenterActivity : Activity()//: RootCompatActivity(RootPresenter::class.java)