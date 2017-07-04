package com.glucose2.app.event

import com.glucose2.app.lifecycleError
import rx.Observable
import rx.Scheduler
import rx.Subscription
import rx.subjects.PublishSubject
import rx.subscriptions.CompositeSubscription
import java.util.*

internal class EventHostDelegate(
        private val scheduler: Scheduler = EventScheduler
) : EventHost {

    // Subjects responsible for handling local event processing
    private val actions = PublishSubject.create<Action>()
    private val events = PublishSubject.create<Event>()

    // Lists that store currently consumed classed.
    // Thread safety: Lists should be accessed only from the associated scheduler!
    // We use lists instead of sets because they can handle duplicates safely.
    private val consumedEvents = ArrayList<Class<*>>()
    private val consumedActions = ArrayList<Class<*>>()

    // Observable of events that are not consumed by this EventHost.
    // Parent EventHost should connect to this stream.
    private val eventsBridge: Observable<Event>
            = events.observeOn(scheduler)
            .filter { event -> consumedEvents.all { !it.isInstance(event) } }

    // Observable of actions that are not consumed by this EventHost.
    // Child EventHosts should connect to this stream.
    private val actionBridge: Observable<Action>
            = actions.observeOn(scheduler)
            .filter { action -> consumedActions.all { !it.isInstance(action) } }


    override fun <T : Event> observeEvent(type: Class<T>): Observable<T>
            = events.observeOn(scheduler)
            .filter { event -> type.isInstance(event) }
            .cast(type)

    override fun <T : Event> consumeEvent(type: Class<T>): Observable<T>
            = observeEvent(type).observeOn(scheduler)
            .doOnSubscribe { consumedEvents.add(type) }
            .doOnUnsubscribe { consumedEvents.remove(type) }

    override fun <T : Action> observeAction(type: Class<T>): Observable<T>
            = actions.observeOn(scheduler)
            .filter { action -> type.isInstance(action) }
            .cast(type)

    override fun <T : Action> consumeAction(type: Class<T>): Observable<T>
            = observeAction(type).observeOn(scheduler)
            .doOnSubscribe { consumedActions.add(type) }
            .doOnUnsubscribe { consumedActions.remove(type) }

    override fun emitEvent(event: Event) {
        this.events.onNext(event)
    }

    override fun emitAction(action: Action) {
        this.actions.onNext(action)
    }

    private var parentSubscription: Subscription? = null

    internal fun attach(parent: EventHostDelegate) {
        if(parentSubscription != null) lifecycleError("This EvenHost is already attached.")
        parentSubscription = CompositeSubscription().apply {
            // while subscribed, pass all events from bridge to the parent presenter
            add(eventsBridge.subscribe(parent.events))
            // while subscribed, receive all actions from bridge from the parent presenter
            add(parent.actionBridge.subscribe(actions))
        }
    }

    internal fun detach() {
        parentSubscription?.unsubscribe()
        parentSubscription = null
    }

    internal fun destroy() {
        this.events.onCompleted()
        this.actions.onCompleted()
    }

}