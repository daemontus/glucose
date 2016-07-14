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

fun Subscription.until(provider: LifecycleProvider, event: LifecycleEvent): Subscription {
    provider.lifecycleEvents.filter { it == event }
            .first().subscribe { this.unsubscribe() }
    return this
}

fun Subscription.bindToLifecycle(provider: LifecycleProvider): Subscription = this.until(provider, provider.closingEvent())
