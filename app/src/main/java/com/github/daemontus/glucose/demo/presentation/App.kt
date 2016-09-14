package com.github.daemontus.glucose.demo.presentation

import android.app.Application
import com.glucose.app.RootCompatActivity
import timber.log.Timber

class App : Application() {
    override fun onCreate() {
        super.onCreate()
        Timber.plant(Timber.DebugTree())
    }
}

class PresenterActivity : RootCompatActivity(RootPresenter::class.java)