package com.glucose2.app

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import com.glucose.app.presenter.LifecycleException
import com.glucose.app.presenter.booleanBundler
import com.glucose.app.presenter.intBundler
import rx.Observable
import rx.Subscription
import rx.subjects.PublishSubject
import rx.subscriptions.CompositeSubscription
import java.util.*

interface HolderFactory {
    fun <T: Holder> obtain(clazz: Class<T>): T
    fun recycle(holder: Holder)
}

interface PresenterHost : HolderFactory {



}

open class Presenter(
        view: View,
        host: PresenterHost
) : Holder(view, host), EventHost, HolderGroup, LifecycleHost<Presenter> {



    /* ========== Event Host ============ */

    // Subjects responsible for handling local event processing
    private val actions = PublishSubject.create<Action>()
    private val events = PublishSubject.create<Event>()

    // Lists that store currently consumed classed.
    // Thread safety: Lists should be accessed only from EventScheduler!
    // We use lists instead of sets because they can handle duplicates safely.
    private val consumedEvents = ArrayList<Class<*>>()
    private val consumedActions = ArrayList<Class<*>>()

    // Observable of events that are not consumed by this Presenter.
    // Parent Presenter should connect to this stream.
    private val eventsBridge: Observable<Event>
            = events.observeOn(EventScheduler)
                .filter { event -> consumedEvents.all { !it.isInstance(event) } }

    // Observable of actions that are not consumed by this Presenter.
    // Child Presenters should connect to this stream.
    private val actionBridge: Observable<Action>
            = actions.observeOn(EventScheduler)
            .filter { action -> consumedActions.all { !it.isInstance(action) } }

    override fun <T : Event> observeEvent(type: Class<T>): Observable<T>
            = events.observeOn(EventScheduler)
            .filter { event -> type.isInstance(event) }
            .cast(type)

    override fun <T : Event> consumeEvent(type: Class<T>): Observable<T>
            = observeEvent(type).observeOn(EventScheduler)
            .doOnSubscribe { consumedEvents.add(type) }
            .doOnUnsubscribe { consumedEvents.remove(type) }

    override fun <T : Action> observeAction(type: Class<T>): Observable<T>
            = actions.observeOn(EventScheduler)
            .filter { action -> type.isInstance(action) }
            .cast(type)

    override fun <T : Action> consumeAction(type: Class<T>): Observable<T>
            = observeAction(type).observeOn(EventScheduler)
            .doOnSubscribe { consumedActions.add(type) }
            .doOnUnsubscribe { consumedActions.remove(type) }

    override fun emitEvent(event: Event) {
        this.events.onNext(event)
    }

    override fun emitAction(action: Action) {
        this.actions.onNext(action)
    }



    /* ========== Holder Group ============ */

    private val attached = ArrayList<Holder>()

    override val children: Sequence<Holder>
            get() = attached.asSequence()

    override val childrenRecursive: Sequence<Holder>
            get() = attached.asSequence().flatMap {
                if (it is Presenter) {
                    sequenceOf(it) + it.childrenRecursive
                } else sequenceOf(it)
            }

    override fun <T : Holder> attach(holder: T, location: InsertionPoint): T {
        // insert into view hierarchy
        location.invoke(view as ViewGroup, holder)
        if (!view.hasTransitiveChild(holder.view)) {
            throw LifecycleException("$holder view has not been inserted properly into $this")
        }
        // move holder into attached state
        attached.add(holder)
        holder.performAttach(this)
        return holder
    }

    override fun <T : Holder> detach(holder: T): T {
        if (holder !in attached || holder.parent != this) {
            throw LifecycleException("$holder not attached to $this (attached: $attached)")
        }
        if (!view.hasTransitiveChild(holder.view)) {
            throw LifecycleException("$holder is not in the view tree of this presenter. Has it been moved?")
        }
        // move holder into detached state
        holder.performDetach()
        attached.remove(holder)
        // remove holder from view hierarchy
        (holder.view.parent as ViewGroup).removeView(holder.view)
        return holder
    }



    /* ========== Lifecycle Host ============ */

    private val whileStarted = CompositeSubscription()
    private val whileResumed = CompositeSubscription()

    private var _started = false
    private var _resumed = false

    override val isStarted: Boolean
        get() = _started

    override val isResumed: Boolean
        get() = _resumed

    internal fun performStart() {
        if (!isAttached) {
            throw LifecycleException("Starting presenter $this which is not attached.")
        }
        if (isStarted) {
            throw LifecycleException("Presenter $this is already started.")
        }
        onStart()
        if (!isStarted) {
            throw LifecycleException("Super.onStart not called properly in $this.")
        }
    }

    internal fun performResume() {
        if (!isStarted) {
            throw LifecycleException("Resuming presenter $this which is not started.")
        }
        if (isResumed) {
            throw LifecycleException("Presenter $this is already resumed.")
        }
        onResume()
        if (!isResumed) {
            throw LifecycleException("Super.onResume not called properly in $this.")
        }
    }

    internal fun performPause() {
        if (!isResumed) {
            throw LifecycleException("Pausing presenter $this which is not resumed.")
        }
        onPause()
        if (isResumed) {
            throw LifecycleException("Super.onPause not called properly in $this.")
        }
    }

    internal fun performStop() {
        if (!isStarted) {
            throw LifecycleException("Stopping presenter $this which is not started.")
        }
        onStop()
        if (isStarted) {
            throw LifecycleException("Super.onStop not called properly in $this.")
        }
    }

    protected open fun onStart() {
        _started = true
        attached.forEach { if (it is Presenter) it.performStart() }
    }

    protected open fun onResume() {
        _resumed = true
        attached.forEach { if (it is Presenter) it.performResume() }
    }

    protected open fun onPause() {
        attached.forEach { if (it is Presenter) it.performPause() }
        whileResumed.clear()
        _resumed = false
    }

    protected open fun onStop() {
        attached.forEach { if (it is Presenter) it.performStop() }
        whileStarted.clear()
        _started = false
    }

    override fun Subscription.whileResumed(): Subscription {
        if (isResumed) {
            whileResumed.add(this)
        } else {
            unsubscribe()
        }
        return this
    }

    override fun Subscription.whileStarted(): Subscription {
        if (isStarted) {
            whileStarted.add(this)
        } else {
            unsubscribe()
        }
        return this
    }

    override fun performAttach(parent: Presenter) {
        super.performAttach(parent)
        if (parent.isStarted) this.performStart()
        if (parent.isResumed) this.performResume()
    }

    override fun performDetach() {
        if (this.isResumed) this.performResume()
        if (this.isStarted) this.performStart()
        super.performDetach()
    }

    /* ========== Holder overrides ============ */

    override fun onAttach(parent: Presenter) {
        super.onAttach(parent)
        // while attached, pass all eventsBridge to the parent presenter
        this.eventsBridge.subscribe(parent.events).whileAttached()
        // while attached, receive actionBridge from the parent presenter
        parent.actionBridge.subscribe(this.actions).whileAttached()
    }

    override fun performReset() {
        // ensure proper lifecycle semantics
        if (isResumed) performPause()
        if (isStarted) performStop()
        super.performReset()
    }

}