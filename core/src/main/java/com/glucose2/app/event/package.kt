package com.glucose2.app.event

import io.reactivex.Observable
import io.reactivex.schedulers.Schedulers
import java.util.concurrent.Executors

/**
 * Common supertype of objects that are passed upwards through the component tree.
 *
 * These objects represent an event that happened in the subtree but needs to be handled by
 * some higher parent component. Examples: Item selected, text entered, error...
 */
interface Event

/**
 * Common supertype of objects that are passed downwards through the component tree.
 *
 * These objects represent an action which was triggered by some parent component, but needs to be
 * taken by a child component. Examples: Display content, reload, play/pause...
 */
interface Action


// inline reified variants of observe/consume functions

/**
 * Reified variant of [EventHost.observeEvent].
 */
inline fun <reified T: Event> EventHost.observeEvent(): Observable<T>
        = this.observeEvent(T::class.java)

/**
 * Reified variant of [EventHost.consumeEvent].
 */
inline fun <reified T: Event> EventHost.consumeEvent(): Observable<T>
        = this.consumeEvent(T::class.java)

/**
 * Reified variant of [EventHost.observeAction].
 */
inline fun <reified T: Action> EventHost.observeAction(): Observable<T>
        = this.observeAction(T::class.java)

/**
 * Reified variant of [EventHost.consumeAction].
 */
inline fun <reified T: Action> EventHost.consumeAction(): Observable<T>
        = this.consumeAction(T::class.java)

/**
 * Scheduler used to run the event pipeline.
 */
val EventScheduler = Schedulers.from(Executors.newSingleThreadExecutor())