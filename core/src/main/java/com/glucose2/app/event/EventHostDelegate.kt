package com.glucose2.app.event

import android.support.annotation.AnyThread
import android.support.annotation.MainThread
import com.glucose2.app.lifecycleError
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.subjects.PublishSubject
import java.util.*
import java.util.concurrent.locks.ReentrantReadWriteLock
import kotlin.concurrent.read
import kotlin.concurrent.write

internal class EventHostDelegate : EventHost {

    // Subjects responsible for handling local event processing
    private val actions = PublishSubject.create<Action>()
    private val events = PublishSubject.create<Event>()

    // Lists that store currently consumed classed.
    // We use lists instead of sets because they can handle duplicates safely.
    private val consumedEvents = ArrayList<Class<*>>()
    private val consumedActions = ArrayList<Class<*>>()

    private val eventLock = ReentrantReadWriteLock()
    private val actionLock = ReentrantReadWriteLock()

    // Observable of events that are not consumed by this EventHost.
    // Parent EventHost should connect to this stream.
    private val eventsBridge: Observable<Event>
            = events.observeOn(EventScheduler)
            .filter { event ->
                eventLock.read {
                    consumedEvents.all { !it.isInstance(event) }
                }
            }

    // Observable of actions that are not consumed by this EventHost.
    // Child EventHosts should connect to this stream.
    private val actionBridge: Observable<Action>
            = actions.observeOn(EventScheduler)
            .filter { action ->
                actionLock.read {
                    consumedActions.all { !it.isInstance(action) }
                }
            }


    @AnyThread
    override fun <T : Event> observeEvent(type: Class<T>): Observable<T>
            = events.observeOn(EventScheduler)
            .filter { event -> type.isInstance(event) }
            .cast(type)

    @AnyThread
    override fun <T : Event> consumeEvent(type: Class<T>): Observable<T>
            = observeEvent(type).observeOn(EventScheduler)
            .doOnSubscribe { consumedEvents.add(type) }
            .doOnDispose {
                eventLock.write {
                    consumedEvents.remove(type)
                }
            }

    @AnyThread
    override fun <T : Action> observeAction(type: Class<T>): Observable<T>
            = actions.observeOn(EventScheduler)
            .filter { action -> type.isInstance(action) }
            .cast(type)

    @AnyThread
    override fun <T : Action> consumeAction(type: Class<T>): Observable<T>
            = observeAction(type).observeOn(EventScheduler)
            .doOnSubscribe { consumedActions.add(type) }
            .doOnDispose {
                actionLock.write {
                    consumedActions.remove(type)
                }
            }

    @AnyThread
    override fun emitEvent(event: Event) {
        this.events.onNext(event)
    }

    @AnyThread
    override fun emitAction(action: Action) {
        this.actions.onNext(action)
    }

    private var parentSubscription: CompositeDisposable? = null

    @MainThread
    internal fun attach(parent: EventHostDelegate) {
        if(parentSubscription != null) lifecycleError("This EvenHost is already attached.")
        parentSubscription = CompositeDisposable().apply {
            // while subscribed, pass all events from bridge to the parent presenter
            add(eventsBridge.subscribe(parent.events::onNext))
            // while subscribed, receive all actions from bridge from the parent presenter
            add(parent.actionBridge.subscribe(actions::onNext))
        }
    }

    @MainThread
    internal fun detach() {
        parentSubscription?.dispose()
        parentSubscription = null
    }

    @MainThread
    internal fun destroy() {
        this.events.onComplete()
        this.actions.onComplete()
    }

}