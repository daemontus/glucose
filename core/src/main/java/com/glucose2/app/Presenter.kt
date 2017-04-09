package com.glucose2.app

import android.os.Bundle
import android.view.View
import com.glucose.app.presenter.LifecycleException
import com.glucose.app.presenter.NativeBundler
import com.glucose.app.presenter.booleanBundler
import com.glucose.app.presenter.intBundler
import rx.Observable
import rx.Observer
import rx.Subscription
import rx.android.schedulers.AndroidSchedulers
import rx.subjects.PublishSubject
import rx.subscriptions.CompositeSubscription
import java.util.*
import kotlin.properties.ReadOnlyProperty
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

/*

interface Holder {

    val view: View
    val parent: Presenter?

    fun onAttach(parent: Presenter)
    fun onBind(data: Bundle)

    fun onReset()
    fun onDetach()

}

interface Presenter : Holder {

    val host: PresenterHost

    fun onResume()
    fun onStart()

    fun onPause()
    fun onStop()
}


*/
interface PresenterHost

open class Presenter(
        view: View,
        host: PresenterHost
) : Holder(view, host), EventHost {

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

    /* ========== Holder overrides ============ */

    override fun onAttach(parent: Presenter) {
        super.onAttach(parent)
        // while attached, pass all eventsBridge to the parent presenter
        this.eventsBridge.subscribe(parent.events).whileAttached()
        // while attached, receive actionBridge from the parent presenter
        parent.actionBridge.subscribe(this.actions).whileAttached()
    }

}


class NativeState<T>(
        private val default: T,
        private val bundler: NativeBundler<T>
) : ReadWriteProperty<Holder, T>, ReadOnlyProperty<Holder, T> {
    override fun getValue(thisRef: Holder, property: KProperty<*>): T
            = bundler.getter(thisRef.instanceState, property.name, default)

    override fun setValue(thisRef: Holder, property: KProperty<*>, value: T)
            = bundler.setter(thisRef.instanceState, property.name, value)

}

open class Holder(
        val view: View,
        val host: PresenterHost
) : Attachable<Presenter> {

    /* ========== Configuration properties ============ */

    /**
     * Default: true. When false, the holder won't be saved into the main hierarchy bundle.
     * Use to safely provide holders inside adapters.
     **/
    val saveHierarchy by NativeState(true, booleanBundler)

    /** Default: true. When false, the holder will be recreated upon configuration change. **/
    val surviveConfigChange by NativeState(true, booleanBundler)

    /** Default: [View.NO_ID]. Uniquely identifies this holder in the hierarchy. **/
    val id by NativeState(View.NO_ID, intBundler)

    /* ========== Attachable<Presenter> ============ */

    private var _parent: Presenter? = null

    override val parent: Presenter
        get() = _parent ?: throw LifecycleException("Holder ($this) is not attached to presenter.")

    override val isAttached: Boolean
        get() = _parent != null

    private val whileAttached = CompositeSubscription()

    override fun Subscription.whileAttached(): Subscription {
        if (isAttached) {
            whileAttached.add(this)
        } else {
            unsubscribe()
        }
        return this
    }


    private var _instanceState: Bundle? = null

    val instanceState: Bundle
        get() = _instanceState ?: throw LifecycleException("Holder ($this) is not bound to a state.")

    val isBound: Boolean
        get() = _instanceState != null

    /**
     * Only update the parent value. Presenter is responsible for placing this holder into the
     * view hierarchy BEFORE calling this.
     */
    internal fun performAttach(parent: Presenter) {
        if (isAttached) {
            throw LifecycleException("Holder ($this) is already attached to ${this.parent}.")
        }
        onAttach(parent)
        if (!isAttached) {
            throw LifecycleException("Super.onAttach hasn't been called properly in $this.")
        }
    }

    /**
     * Only update parent value. Presenter is responsible for removing this holder from the
     * view hierarchy AFTER calling this.
     */
    internal fun performDetach() {
        if (!isAttached) {
            throw LifecycleException("Holder ($this) is not attached to anything.")
        }
        onDetach()
        if (isAttached) {
            throw LifecycleException("Super.onDetach hasn't been called properly in $this.")
        }
    }

    /**
     * The holder is connected to the view hierarchy before onAttach is called.
     * After super.onAttach returns, the [parent] property should not be null.
     */
    protected open fun onAttach(parent: Presenter) {
        this._parent = parent
    }

    /**
     * After super.onDetach returns, the [parent] property should be null.
     * The holder is removed from the view hierarchy after onDetach is called.
     */
    protected open fun onDetach() {
        whileAttached.clear()
        this._parent = null
    }

    internal fun performBind(instanceState: Bundle) {
        if (isBound) {
            throw LifecycleException("Holder ($this) is already bound to ${this.instanceState}.")
        }
        onBind(instanceState)
        if (!isBound) {
            throw LifecycleException("Super.onBind hasn't been called properly in $this.")
        }
    }

    internal fun performReset() {
        if (!isBound) {
            throw LifecycleException("Holder ($this) is not bound to anything.")
        }
        onReset()
        if (isBound) {
            throw LifecycleException("Super.onReset hasn't been called properly in $this.")
        }
    }

    protected open fun onBind(instanceState: Bundle) {
        this._instanceState = instanceState
    }

    protected open fun onReset() {
        whileBound.clear()
        this._instanceState = null
    }

    private val whileBound = CompositeSubscription()

    fun Subscription.whileBound(): Subscription {
        if (isBound) {
            whileBound.add(this)
        } else {
            unsubscribe()
        }
        return this
    }

}

fun Subscription.whileBound(holder: Holder): Subscription = holder.run {
    whileBound()
}