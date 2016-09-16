package com.glucose.app

import android.app.Activity
import android.os.Bundle
import android.widget.FrameLayout

class EmptyActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(FrameLayout(this))
    }
}