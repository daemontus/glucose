package com.glucose2.app.component

import android.os.Bundle
import com.glucose.app.presenter.LifecycleException

open class Component {

    // ========== Lifecycle handling ==========

    // I'm not making this public now, because it might confuse people with
    // regards to the bind semantics.
    private enum class State {
        DESTROYED, ALIVE, ATTACHED, STARTED, RESUMED
    }

    private var state: State = State.ALIVE

    /**
     * Indicates that the component is destroyed and can't be used again.
     */
    val isDestroyed get() = state == State.DESTROYED

    /**
     * Indicates that the component has not been destroyed.
     */
    val isAlive get() = state >= State.ALIVE

    /**
     * Indicates that the component is part of the main tree.
     */
    val isAttached get() = state >= State.ATTACHED

    /**
     * Indicates that the component is part of the main tree and the activity is started.
     */
    val isStarted get() = state >= State.STARTED

    /**
     * Indicates that the component is part of the main tree and the activity is resumed.
     */
    val isResumed get() = state >= State.RESUMED

    private var _data: Bundle? = null

    protected val data: Bundle
        get() = _data ?: throw LifecycleException("Accessing data on a component which is not bound.")

    /**
     * Indicates that the component has an associated data bundle.
     */
    val isBound get() = _data != null



}