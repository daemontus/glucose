package com.glucose.app

import rx.Observable
import rx.Subscription

//TODO: Add methods like untilDetach(provider), untilStop(), etc...
//TODO: Add filter shortcuts like lifecycleEvents.nextDetach or lifecycleEvents.onDetach

enum class LifecycleEvent {
    CREATE, ATTACH, START, RESUME, PAUSE, STOP, DETACH, DESTROY, CONFIG_CHANGE
}

interface LifecycleProvider {
    /**
     * Provides notifications about lifecycle changes.
     * Note that there is no guarantee about delay between
     * the lifecycle event and the event delivery.
     *
     * Hence one can't assume that after receiving a START notification,
     * the provider is actually started - it may have been paused already.
     */
    val lifecycleEvents: Observable<LifecycleEvent>

    /**
     * Returns an event which will end current provider state.
     * (Resume -> Pause, Start -> Stop, ...)
     */
    fun closingEvent(): LifecycleEvent

    /**
     * Returns an event which initiated current provider state.
     * (Alive -> Create, Started -> Start, ...)
     */
    fun startingEvent(): LifecycleEvent
}

fun <T> Observable<T>.takeUntil(provider: LifecycleProvider, event: LifecycleEvent): Observable<T> {
    return this.takeUntil(provider.lifecycleEvents.filter { it == event })
}

fun <T: Any> Observable<T>.bindToLifecycle(provider: LifecycleProvider): Observable<T> {
    return this.takeUntil(provider, provider.closingEvent())
}

//TODO: As we have seen, this is terribly slow and there are no guarantees. We should implement this
//in form of callback/etc. that will be guaranteed to execute when the event happens
fun Subscription.until(provider: LifecycleProvider, event: LifecycleEvent): Subscription {
    provider.lifecycleEvents.filter { it == event }
            .first().subscribe { this.unsubscribe() }
    return this
}

fun Subscription.bindToLifecycle(provider: LifecycleProvider): Subscription = this.until(provider, provider.closingEvent())