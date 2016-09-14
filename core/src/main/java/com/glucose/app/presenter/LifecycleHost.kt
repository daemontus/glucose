package com.glucose.app.presenter

import android.support.annotation.AnyThread
import android.support.annotation.MainThread
import rx.Observable
import rx.Observer
import rx.Subscriber
import rx.Subscription
import rx.android.schedulers.AndroidSchedulers
import rx.functions.Action0
import rx.functions.Action1

object Lifecycle {

    /**
     * Possible events that can occur during a lifecycle.
     */
    enum class Event {
        ATTACH, START, RESUME, PAUSE, STOP, DETACH, DESTROY
    }

    /**
     * Possible states an object can be in within a lifecycle.
     * Each state hierarchy is inclusive with respect to states that are smaller
     * (except for [DESTROYED]), meaning that a component that
     * is [STARTED] is also [ATTACHED] and [ALIVE].
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
         * Return the event that will end this state.
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
         * Return the event that started this state.
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
     * the upcoming events.
     *
     * Hence one can't assume that after receiving a START notification,
     * the host is actually started - it may have been stopped already.
     * But at some point in the past, it was indeed started.
     */
    val lifecycleEvents: Observable<Lifecycle.Event>

    /**
     * Add a callback that will be executed during lifecycle change.
     *
     * Note that the component can't change state again until all callbacks
     * are finished. So if you add a START callback, it will be executed
     * before the component can be stopped, providing a synchronous
     * notification.
     */
    @MainThread
    fun addEventCallback(event: Lifecycle.Event, callback: () -> Unit)

    /**
     * Remove a previously added callback. Returns true if the callback
     * was found and false if there was no such pending callback.
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
    fun Subscription.until(event: Lifecycle.Event) : Subscription
            = this.until(this@LifecycleHost, event)

    /**
     * @see [whileIn]
     */
    fun <T> Observable<T>.whileIn(state: Lifecycle.State) : BoundSubscription<T>
            = this.whileIn(this@LifecycleHost, state)

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
fun Subscription.until(
        host: LifecycleHost, event: Lifecycle.Event
): Subscription {
    host.addEventCallback(event) {
        if (!this.isUnsubscribed) this.unsubscribe()
    }
    return this
}

/**
 * Returns a [BoundSubscription] that ensures that this observable is subscribed to
 * only while given [LifecycleHost] is in a specified (or higher) [Lifecycle.State].
 * If the host is not in the specified state, the subscription won't be created.
 * If it is, it will be created and a callback will be added to ensure that the
 * subscription will be unsubscribed as soon as the host leaves the state.
 *
 * If all you need is asynchronous behavior, use [takeWhileIn].
 * If you don't need to take hosts state into account, use [until].
 *
 * @see LifecycleHost
 * @see takeWhileIn
 * @see until
 */
@MainThread
fun <T> Observable<T>.whileIn(host: LifecycleHost, state: Lifecycle.State): BoundSubscription<T>
        = BoundSubscription(this, state, host)

/**
 * @see whileIn
 */
@MainThread
fun <T> Observable<T>.whileAlive(host: LifecycleHost) : BoundSubscription<T>
        = this.whileIn(host, Lifecycle.State.ALIVE)

/**
 * @see whileIn
 */
@MainThread
fun <T> Observable<T>.whileAttached(host: LifecycleHost) : BoundSubscription<T>
        = this.whileIn(host, Lifecycle.State.ATTACHED)

/**
 * @see whileIn
 */
@MainThread
fun <T> Observable<T>.whileStarted(host: LifecycleHost) : BoundSubscription<T>
        = this.whileIn(host, Lifecycle.State.STARTED)

/**
 * @see whileIn
 */
@MainThread
fun <T> Observable<T>.whileResumed(host: LifecycleHost) : BoundSubscription<T>
        = this.whileIn(host, Lifecycle.State.RESUMED)

/**
 * A helper class for implementing lifecycle-bound subscriptions.
 * [BoundSubscription] mimics the subscription API of an [Observable],
 * but asserts that during subscription, the [LifecycleHost] connected
 * to this [BoundSubscription] is in the desired state and that the
 * subscription will be unsubscribed when the [LifecycleHost] leaves this state.
 */
class BoundSubscription<out T>(
        private val observable: Observable<T>,
        private val state: Lifecycle.State,
        private val host: LifecycleHost
) {

    fun subscribe(): Subscription?
            = checkState { observable.subscribe() }
    fun subscribe(onNext: (T) -> Unit): Subscription?
            = checkState { observable.subscribe(onNext) }
    fun subscribe(onNext: (T) -> Unit, onError: (Throwable) -> Unit): Subscription?
            = checkState { observable.subscribe(onNext, onError) }
    fun subscribe(onNext: (T) -> Unit, onError: (Throwable) -> Unit, onCompleted: () -> Unit)
            = checkState { observable.subscribe(onNext, onError, onCompleted) }
    fun subscribe(observer: Observer<in T>): Subscription?
            = checkState { observable.subscribe(observer) }
    fun subscribe(subscriber: Subscriber<in T>): Subscription?
            = checkState { observable.subscribe(subscriber) }

    private inline fun checkState(andDo: () -> Subscription): Subscription? {
        return if (host.state >= state) andDo().until(host, state.closingEvent()) else null
    }

}

//some simple helper functions regarding state

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
