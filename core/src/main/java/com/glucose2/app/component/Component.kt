package com.glucose2.app.component

import android.os.Bundle
import android.support.annotation.CallSuper
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
        get() = _data ?: throw LifecycleException("Accessing data on a component which does not have any.")

    override final val isBound get() = _data != null

    override final val dataBound: ObservableBinder = ObservableBinder()

    override fun bindData(data: Bundle): Bundle? {
        val old = _data
        onDataBind(data)
        if (_data !== data) {
            throw LifecycleException("super.onDataBind not called properly in $this")
        }
        return old
    }

    override fun resetData(): Bundle? {
        val old = _data
        onDataReset()
        if (isBound) {
            throw LifecycleException("super.onDataReset not called properly in $this")
        }
        return old
    }

    /**
     * Use this method to react to data bundle changes.
     *
     * Before you call super.onDataBind, the old data bundle is still available,
     * so that you can distinguish fresh binds and rebinds.
     */
    @CallSuper
    protected open fun onDataBind(data: Bundle) {
        dataBound.performStop()
        _data = data
        dataBound.performStart()
    }

    /**
     * Use this method to react to data bundle resets.
     */
    @CallSuper
    protected open fun onDataReset() {
        dataBound.performStop()
        _data = null
    }

}