package com.glucose2.app.event

import android.support.annotation.AnyThread
import io.reactivex.Observable


/**
 * EventHost is an interface implemented by Glucose components which provide access to the event bus.
 *
 * It is responsible for passing events upwards (from leaves to root) and actions downwards
 * (from root to leaves) in the component tree. Note that it does not enforce how the tree
 * is constructed nor does it provide a way to directly inspect it.
 *
 * One can subscribe to both events and actions flowing through the EventHost.
 * Each such subscription specifies the type of the observed class and
 * can have two types: observing and consuming.
 *
 * In case of the consuming subscription, the events (or actions) that are observed are
 * not passed further up (or down) the tree.
 *
 * When observing or consuming, events are filtered according to the standard
 * isInstance relation i.e. if you observe event of type A, event B such that B extends A will be
 * also observed. On the other hand, if you observe B, event of type A will
 * not be observed.
 *
 * When consuming, one has to keep in mind that the event is prohibited from continuing
 * up/down the tree, however, all local consumers/observers will observe the
 * event, regardless of the order of observer registration, etc.
 *
 * Furthermore, the consumption is only active while the observable returned by
 * [consumeEvent] or [consumeAction] is subscribed.
 *
 * All observables should have a publish semantics (i.e. all events/actions that
 * are not observed are forgotten) and run on the [EventScheduler].
 */
@AnyThread
interface EventHost {

    /**
     * @return Observable stream of all events of the given [type] handled by this EventHost.
     */
    fun <T: Event> observeEvent(type: Class<T>): Observable<T>

    /**
     * @return Observable stream of all events of given [type] handled by this EventHost.
     * While the Observable is subscribed, no events of this type are passed to the parent.
     */
    fun <T: Event> consumeEvent(type: Class<T>): Observable<T>

    /**
     * @return Observable stream of all actions of given [type] handled by this EventHost.
     */
    fun <T: Action> observeAction(type: Class<T>): Observable<T>

    /**
     * @return Observable stream of all actions of given [type] handled by this EventHost.
     * While the Observable is subscribed, no actions of this type are passed to the children.
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