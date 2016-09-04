package com.glucose.app.presenter

import android.os.Bundle
import com.glucose.app.Presenter
import kotlin.reflect.KProperty

fun bundle(name: String, value: Int): Bundle {
    return Bundle().apply {
        this.putInt(name, value)
    }
}

fun Bundle.and(name: String, value: Int): Bundle {
    return this.apply {
        this.putInt(name, value)
    }
}


fun test() {
    val arguments = bundle(Presenter::id.name, 10).and(Presenter::id.name, 20)
}