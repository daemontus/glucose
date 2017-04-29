package com.glucose2.app.component

import com.glucose2.rx.ObservableBinder

interface LifecycleHost {

    val isDestroyed: Boolean

    val isAlive: Boolean
    val alive: ObservableBinder

    val isAttached: Boolean
    val attached: ObservableBinder

    val isStarted: Boolean
    val started: ObservableBinder

    val isResumed: Boolean
    val resumed: ObservableBinder

}