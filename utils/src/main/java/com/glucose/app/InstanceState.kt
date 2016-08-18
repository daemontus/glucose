package com.glucose.app

import android.os.Bundle

interface StateProvider {
    val statePrefix: String
    fun addHook(instance: InstanceState)
    fun nextStateId(): Int
}

abstract class InstanceState(
        provider: StateProvider
) {
    init {
        provider.addHook(this)
    }
    protected val key = "${provider.statePrefix}[state:${provider.nextStateId()}]"
    abstract fun onCreate(instanceState: Bundle?)
    abstract fun onSaveInstanceState(output: Bundle)
}