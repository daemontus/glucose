package com.glucose2.app

import android.os.Bundle
import android.view.View
import com.glucose.app.presenter.LifecycleException
import com.glucose.app.presenter.NativeBundler
import com.glucose.app.presenter.booleanBundler
import com.glucose.app.presenter.intBundler
import rx.Observable
import rx.Subscription
import rx.android.schedulers.AndroidSchedulers
import rx.subjects.PublishSubject
import rx.subscriptions.CompositeSubscription
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

interface Event
interface Action
interface PresenterHost

open class Presenter(
        view: View,
        host: PresenterHost
) : Holder(view, host) {

    private val eventPublisher = PublishSubject.create<Event>()
    private val eventReceiver = PublishSubject.create<Event>()

    val events: Observable<Event> = eventPublisher

    private val consumedEvents = ArrayList<Class<*>>()

    fun emmitEvent(event: Event) {
        this.eventReceiver.onNext(event)
    }

    fun <T: Event> consumeEvent(kind: Class<T>): Observable<T> {
        return observeEvent(kind)
                .doOnSubscribe { consumedEvents.add(kind) }
                .doOnUnsubscribe { consumedEvents.remove(kind) }
    }

    fun <T: Event> observeEvent(kind: Class<T>): Observable<T> {
        return eventReceiver
                .filter { kind.isInstance(it) }
                .cast(kind)
    }

    fun attach(holder: Holder) {
        //do some other stuff
        holder.performAttach(this)

        // stream events from attached presenter
        if (holder is Presenter) {
            holder.events.subscribe(eventReceiver).whileAttached(holder)
        }
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
) {

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

    private var _parent: Presenter? = null

    val parent: Presenter
        get() = _parent ?: throw LifecycleException("Holder ($this) is not attached to presenter.")

    val isAttached: Boolean
        get() = _parent != null

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

    private val whileAttached = CompositeSubscription()
    private val whileBound = CompositeSubscription()

    fun Subscription.whileAttached(): Subscription {
        if (isAttached) {
            whileAttached.add(this)
        } else {
            unsubscribe()
        }
        return this
    }

    fun Subscription.whileBound(): Subscription {
        if (isBound) {
            whileBound.add(this)
        } else {
            unsubscribe()
        }
        return this
    }

}

fun Subscription.whileAttached(holder: Holder): Subscription = holder.run {
    whileAttached()
}

fun Subscription.whileBound(holder: Holder): Subscription = holder.run {
    whileBound()
}