package com.glucose.app.presenter

import rx.Observable
import rx.subjects.PublishSubject
import java.util.*
import com.glucose.app.presenter.Lifecycle.State.*;

//TODO: Get rid of mState
internal class LifecycleDelegate : LifecycleHost {

    private val lifecycleEventSubject = PublishSubject.create<Lifecycle.Event>()
    private val lifecycleCallbacks = ArrayList<Pair<Lifecycle.Event, () -> Unit>>()

    internal var mState: Lifecycle.State = Lifecycle.State.ALIVE
        set(value) {
            when (field to value) {
                //state is increasing
                //Note: Destroyed -> Alive is not allowed
                ALIVE to ATTACHED, ATTACHED to STARTED, STARTED to RESUMED -> {
                    val event = value.openingEvent()
                    field = value
                    onLifecycleEvent(event)
                }
                //state is decreasing
                RESUMED to STARTED, STARTED to ATTACHED, ATTACHED to ALIVE, ALIVE to DESTROYED -> {
                    val event = field.closingEvent()
                    onLifecycleEvent(event)
                    field = value
                }
                else -> throw LifecycleException("Invalid lifecycle transition from $field to $value")
            }
        }

    override val lifecycleEvents: Observable<Lifecycle.Event> = lifecycleEventSubject

    override val state: Lifecycle.State
        get() = mState

    override fun addEventCallback(event: Lifecycle.Event, callback: () -> Unit) {
        lifecycleCallbacks.add(event to callback)
    }

    override fun removeEventCallback(event: Lifecycle.Event, callback: () -> Unit): Boolean {
        return lifecycleCallbacks.remove(event to callback)
    }

    /**
     * Ensures callbacks and notifications regarding a Lifecycle event are dispatched.
     */
    private fun onLifecycleEvent(event: Lifecycle.Event) {
        val victims = lifecycleCallbacks.filter { it.first == event }
        lifecycleCallbacks.removeAll(victims)
        victims.forEach { it.second.invoke() }
        lifecycleEventSubject.onNext(event)
        if (event == Lifecycle.Event.DESTROY) {
            lifecycleEventSubject.onCompleted()
        }
    }


}