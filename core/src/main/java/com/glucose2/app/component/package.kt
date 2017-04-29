package com.glucose2.app.component

import android.os.Bundle
import com.glucose2.app.LifecycleException
import com.glucose2.rx.ObservableBinder

open class Component : DataHost, LifecycleHost {

    // ========== Lifecycle handling ==========

    // I'm not making this public now, because it might confuse people with
    // regards to the bind semantics.
    private enum class State {
        DESTROYED, ALIVE, ATTACHED, STARTED, RESUMED
    }

    private var state: State = State.ALIVE

    override final val isDestroyed get() = state == State.DESTROYED

    override final val isAlive get() = state >= State.ALIVE
    override final val alive: ObservableBinder = ObservableBinder()

    override final val isAttached get() = state >= State.ATTACHED
    override final val attached: ObservableBinder = ObservableBinder()

    override final val isStarted get() = state >= State.STARTED
    override final val started: ObservableBinder = ObservableBinder()

    override final val isResumed get() = state >= State.RESUMED
    override final val resumed: ObservableBinder = ObservableBinder()

    // ========== DataHost interface ==========

    private var _data: Bundle? = null

    override final val data: Bundle
        get() = _data ?: throw LifecycleException("Accessing data on a component which is not bound.")

    override final val isBound get() = _data != null

    override final val dataBound: ObservableBinder = ObservableBinder()

    override fun bindData(data: Bundle): Bundle? {
        val old = _data
        _data = data
        dataBound.performStart()
        return old
    }

    override fun unbindData(): Bundle {
        dataBound.performStop()
        val r = data
        _data = null
        return r
    }

}