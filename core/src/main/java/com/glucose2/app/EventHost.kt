package com.glucose2.app

import rx.Observable
import rx.Observer


/*

    Explanation of the event mechanism and correctness reasoning:

    When child presenter is attached, its events are connected
    to our event bridge. We can either observe (no change in chain,
    returns just the appropriate observable) or consume them.

    While consumption observable is active, events of the consumed type
    are not forwarded upstream.

 */

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
 * events
 */
interface EventHost {

    val events: Observable<Event>
    val actions: Observer<Event>

    fun <T: Event> observeEvent(type: Class<T>): Observable<T>
    fun <T: Event> consumeEvent(type: Class<T>): Observable<T>

    fun <T: Action> observeAction(type: Class<T>): Observable<T>
    fun <T: Action> consumeAction(type: Class<T>): Observable<T>

    fun emitEvent(event: Event)

    fun emitAction(action: Action)

}