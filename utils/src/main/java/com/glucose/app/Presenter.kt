package com.glucose.app

import android.app.Activity
import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.support.annotation.IdRes
import android.support.annotation.LayoutRes
import android.util.SparseArray
import android.view.LayoutInflater
import android.view.View
import rx.Observable
import rx.Subscription
import rx.subjects.PublishSubject


/**
 * Presenter is responsible for a single, possibly modular part of UI.
 * Presenter should be completely isolated in terms of state from it's parent
 * (therefore it doesn't even have a reference to it), however, it can access
 * global data by means of a [PresenterContext].
 *
 * Presenter is responsible for managing it's child Presenters and relationships
 * between them (see [PresenterGroup]).
 *
 * Presenter lifecycle is similar to that of an [Activity], but instead of the
 * onCreate -> onRestart loop, it has onAttach -> onDetach.
 *
 * Other lifecycle information:
 * - Presenter is always connected to it's view, however, this view is only connected to the
 * view hierarchy after [onAttach] (and before [onStart]).
 * - Similarly, presenter's view is detached from the hierarchy before [onDetach], but after [onStop].
 * - Presenter is identified by it's [id]. If the user does not override the ID getter, the
 * id is assumed to be given in the argument bundle.
 * - Similar to views, ID is used to identify presenters when saving/restoring state, so
 * a presenter without an ID won't have it's state saved and in case of an ID clash, the
 * behavior is undefined.
 * - The existence of a child will be saved only if the parent has an ID and the [PresenterLayout]
 * the child is in also has an ID.
 * - Presenter can use [canChangeConfiguration] to indicate if it wants to be recreated or if
 * it will handle a configuration change on it's own.
 * - In case of a configuration change, presenter's parent should save it's state and recreate if,
 * or notify it about the change (depending on the presenter)
 * - Default implementation for [onBackPressed] will traverse the presenter hierarchy and stop as
 * soon as one of the presenters returns true (hence only the first presenter will actually
 * do a step back).
 * - On the other hand, [onActivityResult] is delivered to all presenters in the hierarchy.
 * (This might be changed later on with a better mechanism as soon as the permissions are
 * also solved)
 *
 * Presenter can always be placed only in the [PresenterLayout] - this is mainly to simplify
 * the reuse (All them layout params).
 *
 * ### Presenter as [ActionHost]
 *
 * Each presenter can be used as an [ActionHost]. Internally, it uses [MainThreadActionHost]
 * so that each action is executed on main thread by default. Action execution is started after
 * the call to [onAttach] (to ensure that all state has been restored) and stopped
 * before the call to [onDetach] (to ensure state isn't changing any more). Actions posted
 * outside of this window are thrown away with an error.
 *
 * @see [ActionHost]
 * TODO: Handle onRequestPermissionResult.
 */
open class Presenter(
        context: PresenterContext, val view: View
) : LifecycleProvider, ActionHost {

    /**
     * Create a Presenter with view initialized from layout resource.
     */
    constructor(context: PresenterContext, @LayoutRes layout: Int) : this(
            context, LayoutInflater.from(context.activity).inflate(layout, PresenterLayout(context.activity), false)
    )

    // ============================= Lifecycle and Context =========================================

    enum class State {
        DESTROYED, ALIVE, ATTACHED, STARTED, RESUMED
    }

    private val lifecycleEventSubject = PublishSubject.create<LifecycleEvent>()
    override val lifecycleEvents: Observable<LifecycleEvent> = lifecycleEventSubject

    var state: State = State.ALIVE
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
                throw LifecycleException("Presenter is not attached. Cannot access arguments.")
            else return field
        }

    val ctx: PresenterContext = context
        get() {
            if (this.isDestroyed)
                throw LifecycleException("Accessing Context on a destroyed presenter.")
            return field
        }

    open val id: Int
        get() = if (this.isAttached) {
            arguments.getInt("id", View.NO_ID)
        } else View.NO_ID

    private fun assertLifecycleChange(from: State, to: State, transition: () -> Unit) {
        if (state != from) throw IllegalStateException("Something is wrong with the lifecycle!")
        transition()
        if (state != to) throw IllegalStateException("Something is wrong with the lifecycle! Maybe forgot to call super?")
    }

    internal fun performAttach(arguments: Bundle)
            = assertLifecycleChange(State.ALIVE, State.ATTACHED) {
        onAttach(arguments)
        actionHost.startProcessingActions()
    }

    internal fun performStart()
            = assertLifecycleChange(State.ATTACHED, State.STARTED) { onStart() }

    internal fun performResume()
            = assertLifecycleChange(State.STARTED, State.RESUMED) { onResume() }

    internal fun performPause()
            = assertLifecycleChange(State.RESUMED, State.STARTED) { onPause() }

    internal fun performStop()
            = assertLifecycleChange(State.STARTED, State.ATTACHED) { onStop() }

    internal fun performDetach()
            = assertLifecycleChange(State.ATTACHED, State.ALIVE) {
        actionHost.stopProcessingActions()
        onDetach()
    }

    internal fun performDestroy()
            = assertLifecycleChange(State.ALIVE, State.DESTROYED) { onDestroy() }

    internal fun performActivityResult(requestCode: Int, resultCode: Int, data: Intent)
            = onActivityResult(requestCode, resultCode, data)

    protected open fun onAttach(arguments: Bundle) {
        lifecycleLog("onAttach")
        this.arguments = arguments
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

    /**
     * Notification that the Presenter should move to the previous state.
     * @return false if presenter can't go back, true if it just did
     */
    open fun onBackPressed(): Boolean = false

    /**
     * Save state of this [Presenter] and all it's children into a container,
     * assuming they have an ID set.
     */
    open fun saveHierarchyState(container: SparseArray<Bundle>) {
        if (id != View.NO_ID) {
            val out = Bundle()
            onSaveInstanceState(out)
            container.put(id, out)
        }
    }

    /**
     * Save state of this [Presenter] into a given Bundle.
     */
    open fun onSaveInstanceState(out: Bundle) {
        out.putAll(arguments)   //make a copy of the current state
    }

    open fun onTrimMemory(level: Int) {}

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

    open val canChangeConfiguration = true

    open fun onConfigurationChanged(newConfig: Configuration) {
        ctx.factory.cleanUpBeforeConfigChange()
        if (!canChangeConfiguration) {
            throw IllegalStateException("$this cannot change configuration and should have been destroyed.")
        }
        lifecycleLog("onConfigurationChanged")
        lifecycleEventSubject.onNext(LifecycleEvent.CONFIG_CHANGE)
    }

    protected open fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
        lifecycleLog("onActivityResult $resultCode for request $requestCode")
    }

    // ================================= Action loop ===============================================

    private val actionHost = MainThreadActionHost()

    override fun <R> post(action: Observable<R>): Observable<R> = actionHost.post(action)

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
        State.DESTROYED -> throw IllegalStateException("Cannot bind to lifecycle. State: $state")
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
        State.DESTROYED -> throw IllegalStateException("$state does not have a closing event")
    }

    override fun startingEvent(): LifecycleEvent = when (state) {
        State.ATTACHED -> LifecycleEvent.ATTACH
        State.STARTED -> LifecycleEvent.START
        State.RESUMED -> LifecycleEvent.RESUME
        State.DESTROYED -> LifecycleEvent.DESTROY
        State.ALIVE -> throw IllegalStateException("$state does not have an opening event")
    }

}