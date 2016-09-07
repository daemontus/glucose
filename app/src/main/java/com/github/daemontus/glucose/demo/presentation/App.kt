package com.github.daemontus.glucose.demo.presentation

import android.app.Application
import com.glucose.app.RootActivity
import timber.log.Timber

class App : Application() {
    override fun onCreate() {
        super.onCreate()
        Timber.plant(Timber.DebugTree())
    }
}

class PresenterActivity : RootActivity(RootPresenter::class.java)