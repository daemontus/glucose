package com.glucose.app

import android.os.Bundle
import android.os.Parcelable
import android.support.annotation.IdRes
import android.view.View
import rx.Observable
import rx.Subscription
import rx.subjects.PublishSubject
import java.util.*
import java.util.concurrent.atomic.AtomicInteger
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty


/**
 * Presenter is responsible for a single, possibly modular part of UI.
 * Presenter should be completely isolated in terms of state from it's parent
 * (therefore it doesn't even have a reference to it), however, it can access
 * global data by means of a PresenterContext.
 *
 * Presenter is responsible for managing it's child Presenters (if any) and relationships
 * between them.
 *
 * When saving state, Presenter is identified by it's class (like View is identified by id).
 * If you want to change this behavior to support IDs or something else, override the statePrefix.
 *
 *
 * A Presenter that is currently attached has also associated an argument bundle.
 *
 * TODO: Handle configuration changes.
 * TODO: Handle memory notifications.
 * TODO: Handle onResult calls.
 */
open class Presenter<out Ctx: PresenterContext>(
        val view: View, context: PresenterContext
) : StateProvider, LifecycleProvider {

    // ============================= Lifecycle and Context =========================================

    enum class State {
        DESTROYED, NONE, ALIVE, ATTACHED, STARTED, RESUMED
    }

    private val lifecycleEventSubject = PublishSubject.create<LifecycleEvent>()
    override val lifecycleEvents: Observable<LifecycleEvent> = lifecycleEventSubject

    var state: State = State.NONE
        private set

    val isAlive: Boolean
        get() = state >= State.ALIVE

    val isAttached: Boolean
        get() = state >= State.ATTACHED

    val isStarted: Boolean
        get() = state >= State.STARTED

    val isResumed: Boolean
        get() = state >= State.RESUMED

    val isDestroyed: Boolean
        get() = state == State.DESTROYED

    var arguments: Bundle = Bundle()
        private set
        get() {
            if (!this.isAttached)
                throw IllegalStateException("Presenter is not attached. Cannot access arguments")
            else return field
        }


    val ctx: PresenterContext = context

    //TODO: Figure out safe semantics for this!
    @Suppress("UNCHECKED_CAST")
    val activity: Ctx
        get() {
            if (this.isDestroyed)
                throw IllegalStateException("Accessing state on destroyed presenter.")
            return ctx.activity as Ctx
        }

    private fun assertLifecycleChange(from: State, to: State, transition: () -> Unit) {
        if (state != from) throw IllegalStateException("Something is wrong with the lifecycle!")
        transition()
        if (state != to) throw IllegalStateException("Something is wrong with the lifecycle!")
    }

    internal fun performCreate(savedInstanceState: Bundle?)
            = assertLifecycleChange(State.NONE, State.ALIVE) { onCreate(savedInstanceState) }

    internal fun performAttach(arguments: Bundle)
            = assertLifecycleChange(State.ALIVE, State.ATTACHED) { onAttach(arguments) }

    internal fun performStart()
            = assertLifecycleChange(State.ATTACHED, State.STARTED) { onStart() }

    internal fun performResume()
            = assertLifecycleChange(State.STARTED, State.RESUMED) { onResume() }

    internal fun performPause()
            = assertLifecycleChange(State.RESUMED, State.STARTED) { onPause() }

    internal fun performStop()
            = assertLifecycleChange(State.STARTED, State.ATTACHED) { onStop() }

    internal fun performDetach()
            = assertLifecycleChange(State.ATTACHED, State.ALIVE) { onDetach() }

    internal fun performDestroy()
            = assertLifecycleChange(State.ALIVE, State.DESTROYED) { onDestroy() }

    protected open fun onCreate(savedInstanceState: Bundle?) {
        lifecycleLog("onCreate")
        state = State.ALIVE
        restoreState(savedInstanceState)
        lifecycleEventSubject.onNext(LifecycleEvent.CREATE)
    }

    protected open fun onAttach(arguments: Bundle) {
        lifecycleLog("onAttach")
        state = State.ATTACHED
        lifecycleEventSubject.onNext(LifecycleEvent.ATTACH)
    }

    protected open fun onStart() {
        lifecycleLog("onStart")
        state = State.STARTED
        lifecycleEventSubject.onNext(LifecycleEvent.START)
    }

    protected open fun onResume() {
        lifecycleLog("onResume")
        state = State.RESUMED
        lifecycleEventSubject.onNext(LifecycleEvent.RESUME)
    }

    open fun onBackPressed(): Boolean = false

    open fun onSaveInstanceState(out: Bundle) {
        saveState(out)
    }

    protected open fun onPause() {
        lifecycleEventSubject.onNext(LifecycleEvent.PAUSE)
        state = State.STARTED
        lifecycleLog("onPause")
    }

    protected open fun onStop() {
        lifecycleEventSubject.onNext(LifecycleEvent.STOP)
        state = State.ATTACHED
        lifecycleLog("onStop")
    }

    protected open fun onDetach() {
        lifecycleEventSubject.onNext(LifecycleEvent.DETACH)
        state = State.ALIVE
        lifecycleLog("onDetach")
    }

    protected open fun onDestroy() {
        lifecycleEventSubject.onNext(LifecycleEvent.DESTROY)
        state = State.DESTROYED
        lifecycleLog("onDestroy")
    }

    // ============================== State Management =============================================

    private val stateHooks = ArrayList<InstanceState>()

    override val statePrefix: String = this.javaClass.name

    override fun addHook(instance: InstanceState) {
        if (isAlive) throw IllegalStateException("Cannot add state hooks on presenter that is already created")
        stateHooks.add(instance)
    }

    private var nextStateId = AtomicInteger(0)
    override fun nextStateId(): Int = nextStateId.incrementAndGet()

    private fun restoreState(state: Bundle?) {
        stateHooks.forEach { it.onCreate(state) }
    }

    private fun saveState(out: Bundle) {
        stateHooks.forEach { it.onSaveInstanceState(out) }
    }

    // ============================ View related helper functions ==================================

    @Suppress("UNCHECKED_CAST")
    fun <V: View> findView(@IdRes viewId: Int): V = view.findViewById(viewId) as V

    @Suppress("UNCHECKED_CAST")
    fun <V: View> findOptionalView(@IdRes viewId: Int): V? = view.findViewById(viewId) as V?

    // ============================ Observable helper functions ====================================

    private fun getClosingEvent() = when (state) {
        State.ALIVE -> LifecycleEvent.DESTROY
        State.ATTACHED -> LifecycleEvent.DETACH
        State.STARTED -> LifecycleEvent.STOP
        State.RESUMED -> LifecycleEvent.PAUSE
        State.NONE, State.DESTROYED -> throw IllegalStateException("Cannot bind to lifecycle. State: $state")
    }

    fun <T: Any> Observable<T>.takeUntil(event: LifecycleEvent): Observable<T> {
        return this.takeUntil(lifecycleEvents.filter { it == event })
    }

    fun <T: Any> Observable<T>.bindToLifecycle(): Observable<T> {
        return this.takeUntil(getClosingEvent())
    }

    fun Subscription.until(event: LifecycleEvent): Subscription {
        lifecycleEvents.filter { it == event }
            .first().subscribe { this.unsubscribe() }
        return this
    }

    fun Subscription.bindToLifecycle(): Subscription = this.until(getClosingEvent())

    override fun closingEvent(): LifecycleEvent = when (state) {
        State.ALIVE -> LifecycleEvent.DESTROY
        State.ATTACHED -> LifecycleEvent.DETACH
        State.STARTED -> LifecycleEvent.STOP
        State.RESUMED -> LifecycleEvent.PAUSE
        State.NONE -> LifecycleEvent.CREATE
        State.DESTROYED -> throw IllegalStateException("$state does not have a closing event")
    }

    override fun startingEvent(): LifecycleEvent = when (state) {
        State.ALIVE -> LifecycleEvent.CREATE
        State.ATTACHED -> LifecycleEvent.ATTACH
        State.STARTED -> LifecycleEvent.START
        State.RESUMED -> LifecycleEvent.RESUME
        State.DESTROYED -> LifecycleEvent.DESTROY
        State.NONE -> throw IllegalStateException("$state does not have an opening event")
    }

    // =========================== Argument related utility functions ==============================

    private class OptionalArgumentDelegate<out T: Any>(
            private val key: String,
            private val default: T?,
            private val getter: (Bundle, String) -> T?
    ) : ReadOnlyProperty<Presenter<*>, T?> {
        override fun getValue(thisRef: Presenter<*>, property: KProperty<*>): T? = getter(thisRef.arguments, key) ?: default
    }

    private class ArgumentDelegate<out T: Any>(
    private val key: String,
    private val default: T?,
    private val getter: (Bundle, String) -> T?
    ) : ReadOnlyProperty<Presenter<*>, T> {
        override fun getValue(thisRef: Presenter<*>, property: KProperty<*>): T
                = getter(thisRef.arguments, key) ?: default ?: throw IllegalStateException("Missing argument $key")
    }

    protected fun stringArgumentOptional(key: String, default: String? = null): ReadOnlyProperty<Presenter<*>, String?>
            = OptionalArgumentDelegate(key, default, { b, k -> b.getString(k) })

    protected fun stringArgument(key: String, default: String? = null): ReadOnlyProperty<Presenter<*>, String>
            = ArgumentDelegate(key, default, { b, k -> b.getString(k) })

    protected fun intArgumentOptional(key: String, default: Int? = null): ReadOnlyProperty<Presenter<*>, Int?>
            = OptionalArgumentDelegate(key, default, { b, k -> b.getInt(k) })

    protected fun intArgument(key: String, default: Int? = null): ReadOnlyProperty<Presenter<*>, Int>
            = ArgumentDelegate(key, default, { b, k -> b.getInt(k) })

    protected fun longArgumentOptional(key: String, default: Long? = null): ReadOnlyProperty<Presenter<*>, Long?>
            = OptionalArgumentDelegate(key, default, { b, k -> b.getLong(k) })

    protected fun longArgument(key: String, default: Long? = null): ReadOnlyProperty<Presenter<*>, Long>
            = ArgumentDelegate(key, default, { b, k -> b.getLong(k) })

    protected fun <T: Parcelable> parcelableArgumentOptional(key: String, default: T? = null): ReadOnlyProperty<Presenter<*>, T?>
            = OptionalArgumentDelegate(key, default, { b, k -> b.getParcelable(k) })

    protected fun <T: Parcelable> parcelableArgument(key: String, default: T? = null): ReadOnlyProperty<Presenter<*>, T>
            = ArgumentDelegate(key, default, { b, k -> b.getParcelable(k) })

    //TODO: Make more helper functions! Also consider moving this into extension functions.
    //TODO: Make the state key depend on property name and not some id
    //TODO: Get rid of arguments and make them part of the state instead! That way the whole presenter can be serialized!
}