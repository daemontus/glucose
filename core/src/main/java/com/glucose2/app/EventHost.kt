package com.glucose2.app

import android.os.Looper
import rx.Observable
import rx.Scheduler
import rx.android.schedulers.AndroidSchedulers
import java.util.concurrent.atomic.AtomicReference

/**
 * Common supertype of objects that are passed upwards through the Presenter tree.
 *
 * These objects usually represent some user generated information, but can
 * be essentially anything meaningful. Examples: Item selected, text entered...
 */
interface Event

/**
 * Common supertype of objects that are passed downwards through the Presenter tree.
 *
 * These objects usually represent some machine generated changes, but can
 * be essentially anything meaningful. Examples: Go to page, show error...
 */
interface Action

/**
 * EventHost is one node in an Event/Action tree. It's responsibility is passing
 * events upwards (from leaves to root) and actions downwards (from root to leaves) in
 * the tree.
 *
 * One can subscribe to both events and actions flowing through this EventHost.
 * Each such subscription specifies the type of observed class and
 * can have two types: observing and consuming.
 *
 * In case of consuming subscription, the events (or actions) that are observed are
 * not passed further up (or down) the tree. Using this mechanism, you can implement tightly
 * coupled hierarchies of event hosts.
 *
 * When observing or consuming, events are filtered according to standard
 * isInstance relation (i.e. if you observe A, event B such that B : A will be
 * also observed. On the other hand, if you observe B, event of type A will
 * not be observed)
 *
 * When consuming, one has to keep in mind that the event is prohibited from continuing
 * up/down the tree, however, all local consumers/observers will observe the
 * event, regardless of the order of observer registration, etc.
 *
 * Furthermore, the consumption is only active while the observable returned by
 * [consumeEvent] or [consumeAction] is subscribed.
 *
 * All observables should have a publish semantics (i.e. all events/actions that
 * are not observed are forgotten).
 *
 * Thread safety: All event related observables run by default on a special [EventScheduler].
 * This is a single thread scheduler, similar to main thread scheduler, however, allows
 * processing off the main thread. This should ensure you can process a high amount of
 * events without slowing down your UI.
 *
 * Note: This interface does not enforce how the tree is constructed. Unless stated otherwise,
 * when the tree is dynamic, due to the usage of special scheduler, there are no tight
 * constrains about events delivered close to the attach/detach boundaries.
 *
 */
interface EventHost {

    /**
     * @return Observable stream of all events of given type handled by this EventHost.
     */
    fun <T: Event> observeEvent(type: Class<T>): Observable<T>

    /**
     * @return Observable stream of all events of given type handled by this EventHost.
     * While the Observable is subscribed, no events of this type can be passed to the parent.
     */
    fun <T: Event> consumeEvent(type: Class<T>): Observable<T>

    /**
     * @return Observable stream of all actions of given type handled by this EventHost.
     */
    fun <T: Action> observeAction(type: Class<T>): Observable<T>

    /**
     * @return Observable stream of all actions of given type handled by this EventHost.
     * While the Observable is subscribed, no actions of this type can be passed to the children.
     */
    fun <T: Action> consumeAction(type: Class<T>): Observable<T>

    /**
     * Send new event upwards through the tree, starting in this node.
     */
    fun emitEvent(event: Event)

    /**
     * Send new action downwards through the three, starting in this node.
     */
    fun emitAction(action: Action)

}

/**
 * Special scheduler based on a single looper thread that is used for event
 * and action processing.
 */
val EventScheduler: Scheduler = run {
    val looper = AtomicReference<Looper?>(null)
    Thread {
        Looper.prepare()
        looper.set(Looper.myLooper())
        Looper.loop()
    }
    while (looper.get() == null) { /* do nothing */ }
    AndroidSchedulers.from(looper.get()!!)
}