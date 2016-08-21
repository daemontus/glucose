package com.glucose.app

import android.app.Activity
import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.os.Parcelable
import android.support.annotation.AnyThread
import android.support.annotation.IdRes
import android.support.annotation.LayoutRes
import android.support.annotation.MainThread
import android.util.SparseArray
import android.view.LayoutInflater
import android.view.View
import com.github.daemontus.egholm.functional.Result
import com.glucose.Log
import rx.Observable
import rx.Observer
import rx.Subscription
import rx.android.schedulers.AndroidSchedulers
import rx.subjects.PublishSubject
import rx.subjects.UnicastSubject
import java.util.*
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty


/**
 * Presenter is responsible for a single, possibly modular part of UI.
 * Presenter should be completely isolated in terms of state from it's parent
 * (therefore it doesn't even have a reference to it), however, it can access
 * global data by means of a PresenterContext.
 *
 * Presenter is responsible for managing it's child Presenters (if any) and relationships
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
 **
 * TODO: Handle onRequestPermissionResult.
 */
open class Presenter(
        context: PresenterContext, val view: View
) : LifecycleProvider {

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

    val id: Int
        get() = if (this.isAttached) {
            arguments.getInt("id", View.NO_ID)
        } else View.NO_ID

    private fun assertLifecycleChange(from: State, to: State, transition: () -> Unit) {
        if (state != from) throw IllegalStateException("Something is wrong with the lifecycle!")
        transition()
        if (state != to) throw IllegalStateException("Something is wrong with the lifecycle!")
    }

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

    internal fun performActivityResult(requestCode: Int, resultCode: Int, data: Intent)
            = onActivityResult(requestCode, resultCode, data)

    protected open fun onAttach(arguments: Bundle) {
        lifecycleLog("onAttach")
        state = State.ATTACHED
        lifecycleEventSubject.onNext(LifecycleEvent.ATTACH)
        startProcessingActions()
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

    open fun saveHierarchyState(container: SparseArray<Bundle>) {
        if (id != View.NO_ID) {
            val out = Bundle()
            onSaveInstanceState(out)
            container.put(id, out)
        }
    }

    protected open fun onSaveInstanceState(out: Bundle) {
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
        stopProcessingActions()
        lifecycleEventSubject.onNext(LifecycleEvent.DETACH)
        state = State.ALIVE
        lifecycleLog("onDetach")
    }

    protected open fun onDestroy() {
        destroyActionQueue()
        lifecycleEventSubject.onNext(LifecycleEvent.DESTROY)
        state = State.DESTROYED
        lifecycleLog("onDestroy")
    }

    open val canChangeConfiguration = true

    open fun onConfigurationChanged(newConfig: Configuration) {
        ctx.factory.onConfigurationChange(newConfig)
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

    /**
     *
     * Actions provide a way to serialize execution that is related to a specific Presenter
     * while making sure that the main thread is not blocked if the action needs to
     * perform some long running task while making sure that other actions will wait for
     * the current one to finish.
     *
     * Action is represented by an observable that can emmit any number of items.
     * User does not subscribe to the observable directly. Instead, he gives it to the
     * Presenter which will return a proxy observable emitting items as soon as the action
     * is executed.
     *
     * When the user subscribes to this proxy, the action is executed or put in the execution
     * queue if there already is a running action.
     *
     * Presenter will start executing actions as soon as it becomes attached to the
     * main structure.
     *
     * If the Presenter is not attached when the Action should be executed, the proxy observable
     * will emit an exception.
     * If the Presenter is being detached while an Action is running, the
     * action observable is unsubscribed and the proxy observable will receive an exception
     * plus unspecified number of items previously emitted by the observable.
     *
     * Except for this, the proxy observable should exactly mirror the behaviour of the
     * original action observable (including errors, un-subscribing, etc.).
     *
     * Backpressure note: When the number of pending actions becomes too big (usually more than 3),
     * they will be dropped (Proxy will return an error). In future implementation, this
     * behaviour should be parametrised. (If you want to submit a big number of
     * actions at once, use concatMap with backpressure buffer)
     *
     * Performance note: To ensure that the action is executed only once, the results
     * of each action are cached. Therefore if you want to avoid excessive memory usage,
     * avoid actions that emit high amount of items. Future note: Maybe just make it into a
     * unicast subject and be done with it.
     *
     * Implementation note: Everything regarding actions in this class should be happening
     * on the main thread, so that synchronisation is not needed.
     *
     * TODO: What should happen if I post a proxy as an action? (Right now - deadlock)
     * TODO: Can we make this into an operator? :) Why not?
     * TODO: Post immediate - like Post, but doesn't require subscription (uses unicast subject instead)
     */

    //New subject is created when presenter is attached.
    //If presenter is not attached, actionSubject should be null.
    private var actionSubject = PublishSubject.create<Pair<Observable<*>, UnicastSubject<*>>>()
    private var actionSubscription: Subscription? = null
    private var pendingActions = ArrayList<Observer<*>>()

    @AnyThread
    fun <R> post(action: Observable<R>): Observable<R> {
        return Observable.defer {
            //defer ensures that the action is not queued until subscribed to
            actionSubject?.let { queue ->
                //PublishSubject can't be used here, because the action might execute
                //before onNext returns and subscriber will get proxy only after that.
                val proxy = UnicastSubject.create<R>()
                pendingActions.add(proxy)
                queue.onNext(action
                        .doOnEach(proxy)
                        .doOnTerminate {
                            removeProxy(proxy)
                        } to proxy
                )
                proxy
            } ?: Observable.error<R>(   //fail fast if presenter isn't attached
                    IllegalStateException("Action dropped because presenter is not attached.")
            )
        }.subscribeOn(AndroidSchedulers.mainThread())   //ensure action is started on main thread
        //ensures that the action is queued only when first subscribed to and all
        //future subscriptions are served from the cache
        .cache()
    }

    /**
     * Create a subscription that will process actions and
     * handle possible backpressure and errors.
     */
    @MainThread
    private fun startProcessingActions() {
        actionSubscription = Observable.concat(actionSubject.onBackpressureDrop {
            it.second.onError(IllegalStateException("Action dropped due to backpressure"))
            removeProxy(it.second)
        }.map { it.first.asResult() }).subscribe({
            when (it) {
                is Result.Ok<*,*> -> transitionLog("Action produced an item")
                is Result.Error<*,*> -> transitionLog("Action error: ${it.error}")
            }
        }, {
            Log.e("Something went wrong in the action mechanism", it)
        })
    }

    /**
     * Safely remove proxy from the pendingActions list.
     */
    @AnyThread
    private fun removeProxy(proxy: Observer<*>) {
        if (mainThread()) {
            pendingActions.remove(proxy)
        } else {
            view.post {
                pendingActions.remove(proxy)
            }
        }
    }

    /**
     * Stop processing actions and notify unfinished actions with errors.
     */
    @MainThread
    private fun stopProcessingActions() {
        actionSubscription?.unsubscribe()
        actionSubscription = null
        pendingActions.forEach {
            it.onError(
                    IllegalStateException("Action dropped because presenter is not attached.")
            )
        }
        pendingActions.clear()
    }

    /**
     * Clean up after the action mechanism.
     */
    @MainThread
    private fun destroyActionQueue() {
        actionSubject.onCompleted()
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

    // =========================== Argument related utility functions ==============================

    //TODO: Make more, actually use InstanceState and move it to the file there.
    private class OptionalArgumentDelegate<out T: Any>(
            private val key: String,
            private val default: T?,
            private val getter: (Bundle, String) -> T?
    ) : ReadOnlyProperty<Presenter, T?> {
        override fun getValue(thisRef: Presenter, property: KProperty<*>): T? = getter(thisRef.arguments, key) ?: default
    }

    private class ArgumentDelegate<out T: Any>(
    private val key: String,
    private val default: T?,
    private val getter: (Bundle, String) -> T?
    ) : ReadOnlyProperty<Presenter, T> {
        override fun getValue(thisRef: Presenter, property: KProperty<*>): T
                = getter(thisRef.arguments, key) ?: default ?: throw IllegalStateException("Missing argument $key")
    }

    protected fun stringArgumentOptional(key: String, default: String? = null): ReadOnlyProperty<Presenter, String?>
            = OptionalArgumentDelegate(key, default, { b, k -> b.getString(k) })

    protected fun stringArgument(key: String, default: String? = null): ReadOnlyProperty<Presenter, String>
            = ArgumentDelegate(key, default, { b, k -> b.getString(k) })

    protected fun intArgumentOptional(key: String, default: Int? = null): ReadOnlyProperty<Presenter, Int?>
            = OptionalArgumentDelegate(key, default, { b, k -> b.getInt(k) })

    protected fun intArgument(key: String, default: Int? = null): ReadOnlyProperty<Presenter, Int>
            = ArgumentDelegate(key, default, { b, k -> b.getInt(k) })

    protected fun longArgumentOptional(key: String, default: Long? = null): ReadOnlyProperty<Presenter, Long?>
            = OptionalArgumentDelegate(key, default, { b, k -> b.getLong(k) })

    protected fun longArgument(key: String, default: Long? = null): ReadOnlyProperty<Presenter, Long>
            = ArgumentDelegate(key, default, { b, k -> b.getLong(k) })

    protected fun <T: Parcelable> parcelableArgumentOptional(key: String, default: T? = null): ReadOnlyProperty<Presenter, T?>
            = OptionalArgumentDelegate(key, default, { b, k -> b.getParcelable(k) })

    protected fun <T: Parcelable> parcelableArgument(key: String, default: T? = null): ReadOnlyProperty<Presenter, T>
            = ArgumentDelegate(key, default, { b, k -> b.getParcelable(k) })

    //TODO: Get rid of arguments and make them part of the state instead! That way the whole presenter can be serialized!
}