package com.glucose.app.presenter

import android.support.annotation.AnyThread
import android.support.annotation.MainThread
import rx.Observable
import rx.Subscription
import rx.android.schedulers.AndroidSchedulers

object Lifecycle {

    /**
     * Possible events that can occur during a lifecycle.
     */
    enum class Event {
        ATTACH, START, RESUME, PAUSE, STOP, DETACH, DESTROY
    }

    /**
     * Possible states an object can be within a lifecycle.
     * Each state includes also states that are smaller (except for [DESTROYED]),
     * meaning that a component that is [STARTED] is also [ATTACHED] and [ALIVE].
     *
     * Possible state transitions are as follows:
     * . -> ALIVE
     * ALIVE -> ATTACHED
     * ALIVE -> DESTROYED
     * ATTACHED -> STARTED
     * ATTACHED -> ALIVE
     * STARTED -> RESUMED
     * STARTED -> ATTACHED
     * RESUMED -> STARTED
     */
    enum class State {
        DESTROYED, ALIVE, ATTACHED, STARTED, RESUMED;

        /**
         * Return the event that will end current state.
         */
        fun closingEvent(): Event {
            return when (this) {
                ALIVE -> Event.DESTROY
                ATTACHED -> Event.DETACH
                STARTED -> Event.STOP
                RESUMED -> Event.PAUSE
                DESTROYED -> throw IllegalStateException("$this does not have a closing event.")
            }
        }

        /**
         * Return the event that started current state.
         */
        fun openingEvent(): Event {
            return when (this) {
                ALIVE -> throw IllegalStateException("$this does not have an opening event.")
                ATTACHED -> Event.ATTACH
                STARTED -> Event.STOP
                RESUMED -> Event.RESUME
                DESTROYED -> Event.DESTROY
            }
        }

    }
}

/**
 * [LifecycleHost] provides access to the lifecycle information of a component.
 *
 * That means it provides:
 *  - current state the component is in
 *  - asynchronous notifications in the form of an observable
 *  - synchronous notifications in the form of callbacks
 */
interface LifecycleHost {

    /**
     * Current state of the component.
     */
    val state: Lifecycle.State

    /**
     * Notifications about lifecycle events.
     * Note that there is no guarantee about delay between the lifecycle event and
     * the event delivery (especially when using schedulers) and that
     * the events aren't cached. So upon subscribing, you will always receive only
     * the very last event.
     *
     * Hence one can't assume that after receiving a START notification,
     * the host is actually started - it may have been stopped already.
     */
    val lifecycleEvents: Observable<Lifecycle.Event>

    /**
     * Add a callback that will be executed during lifecycle change.
     *
     * Note that the component can't change state again until all callbacks
     * are finished. So if you add a START callback, it will be executed
     * before the component can be stopped.
     */
    @MainThread
    fun addEventCallback(event: Lifecycle.Event, callback: () -> Unit)

    /**
     * Remove a previously added callback. Returns the callback if
     * it was found and null if there was no such pending callback.
     */
    @MainThread
    fun removeEventCallback(event: Lifecycle.Event, callback: () -> Unit): Boolean

    /**
     * @see [takeUntil]
     */
    fun <T> Observable<T>.takeUntil(event: Lifecycle.Event): Observable<T>
            = this.takeUntil(this@LifecycleHost, event)

    /**
     * @see [takeWhileIn]
     */
    fun <T> Observable<T>.takeWhileIn(state: Lifecycle.State): Observable<T>
            = this.takeWhileIn(this@LifecycleHost, state)

    /**
     * @see [until]
     */
    fun <T> Observable<T>.until(
            event: Lifecycle.Event, subscribe: Observable<T>.() -> Subscription
    ) : Subscription = this.until(this@LifecycleHost, event, subscribe)

    /**
     * @see [whileIn]
     */
    fun <T> Observable<T>.whileIn(
            state: Lifecycle.State, subscribe: Observable<T>.() -> Subscription
    ) : Subscription? = this.whileIn(this@LifecycleHost, state, subscribe)

}

/**
 * Returns an [Observable] which will stop emitting items as soon as given [LifecycleHost]
 * emits specified [Lifecycle.Event].
 *
 * Note that the termination will be achieved by emitting an onCompleted event
 * and depends on asynchronous notifications (therefore there can be some delay).
 *
 * If you need synchronous behavior, use [until].
 * If you want take hosts state into account, see [takeWhileIn].
 *
 * Scheduler: No particular info.
 *
 * @see LifecycleHost
 * @see until
 * @see takeWhileIn
 */
@AnyThread
fun <T> Observable<T>.takeUntil(host: LifecycleHost, event: Lifecycle.Event): Observable<T> {
    return this.takeUntil(host.lifecycleEvents.filter { it == event })
}

/**
 * Return an [Observable] which will emit items while given [LifecycleHost] is
 * in given (or higher) [Lifecycle.State]. If (at the time of subscription) the host is
 * in a lower state, no items will be emitted.
 *
 * Note that the termination will be achieved by emitting an onCompleted event
 * and depends on asynchronous notifications (therefore there can be some delay).
 *
 * If you need synchronous behavior, use [whileIn].
 * If you don't need to check hosts state, use [takeUntil]
 *
 * Scheduler: The returned observable will operate on the main thread.
 *
 * @see LifecycleHost
 * @see takeUntil
 * @see whileIn
 */
@AnyThread
fun <T> Observable<T>.takeWhileIn(host: LifecycleHost, state: Lifecycle.State): Observable<T> {
    return Observable.defer {
        if (host.state >= state) {
            this.takeUntil(host, state.closingEvent())
        } else {
            Observable.empty()
        }
    }.subscribeOn(AndroidSchedulers.mainThread())
}

/**
 * Adds a callback to the [LifecycleHost] that will ensure that as soon as it
 * changes state due to specified [Lifecycle.Event], this subscription will be
 * unsubscribed.
 *
 * If all you need is asynchronous behavior, use [takeUntil].
 * If you want to take hosts state into account, us [whileIn].
 *
 * @see LifecycleHost
 * @see takeUntil
 * @see whileIn
 */
@MainThread
inline fun <T> Observable<T>.until(
        host: LifecycleHost, event: Lifecycle.Event, subscribe: Observable<T>.() -> Subscription
): Subscription {
    val s = subscribe.invoke(this)
    host.addEventCallback(event) {
        if (!s.isUnsubscribed) s.unsubscribe()
    }
    return s
}

/**
 * Ensures that this observable is subscribed only while given [LifecycleHost] is
 * in a specified (or higher) [Lifecycle.State]. If the host is not in specified
 * state, the subscription won't be created. If it is, it will be created and
 * a callback will be added to ensure that the subscription will be unsubscribed as
 * soon as the host leaves the state.
 *
 * If all you need is asynchronous behavior, use [takeWhileIn].
 * If you don't need to take hosts state into account, us [until].
 *
 * @see LifecycleHost
 * @see takeWhileIn
 * @see until
 */
@MainThread
inline fun <T> Observable<T>.whileIn(
        host: LifecycleHost, state: Lifecycle.State, subscribe: Observable<T>.() -> Subscription
): Subscription? {
    return if (host.state >= state) {
        this.until(host, state.closingEvent(), subscribe)
    } else {
        null
    }
}

/**
 * @see whileIn
 */
@MainThread
inline fun <T> Observable<T>.whileAlive(
        host: LifecycleHost, subscribe: Observable<T>.() -> Subscription
) : Subscription? = this.whileIn(host, Lifecycle.State.ALIVE, subscribe)

/**
 * @see whileIn
 */
@MainThread
inline fun <T> Observable<T>.whileAttached(
        host: LifecycleHost, subscribe: Observable<T>.() -> Subscription
) : Subscription? = this.whileIn(host, Lifecycle.State.ATTACHED, subscribe)

/**
 * @see whileIn
 */
@MainThread
inline fun <T> Observable<T>.whileStarted(
        host: LifecycleHost, subscribe: Observable<T>.() -> Subscription
) : Subscription? = this.whileIn(host, Lifecycle.State.STARTED, subscribe)

/**
 * @see whileIn
 */
@MainThread
inline fun <T> Observable<T>.whileResumed(
        host: LifecycleHost, subscribe: Observable<T>.() -> Subscription
) : Subscription? = this.whileIn(host, Lifecycle.State.RESUMED, subscribe)

//fun some simple helper functions regarding state

val LifecycleHost.isAlive: Boolean
    get() = this.state >= Lifecycle.State.ALIVE
val LifecycleHost.isAttached: Boolean
    get() = this.state >= Lifecycle.State.ATTACHED
val LifecycleHost.isStarted: Boolean
    get() = this.state >= Lifecycle.State.STARTED
val LifecycleHost.isResumed: Boolean
    get() = this.state >= Lifecycle.State.RESUMED
val LifecycleHost.isDestroyed: Boolean
    get() = this.state <= Lifecycle.State.DESTROYED
