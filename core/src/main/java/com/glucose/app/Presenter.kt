package com.glucose.app

import android.app.Activity
import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.support.annotation.LayoutRes
import android.util.SparseArray
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.glucose.app.presenter.*
import com.glucose.app.presenter.Lifecycle.State.*
import com.glucose.util.lifecycleLog
import rx.Observable
import rx.android.schedulers.AndroidSchedulers
import rx.subjects.ReplaySubject

/**
 * Presenter is a class responsible for a single, possibly modular part of UI.
 *
 * Presenter should be completely isolated in terms of state from it's parent
 * (therefore it doesn't even have a reference to it), however, it can access
 * global data by means of a [PresenterContext].
 *
 * Presenter is responsible for managing it's child presenters and relationships
 * between them (see [PresenterGroup]).
 *
 * Presenter lifecycle is similar to that of an [Activity], but instead of the
 * onCreate -> onRestart loop, it has onAttach -> onDetach.
 *
 * Other lifecycle information:
 * - Presenter is always connected to it's view, however, this view is only connected to the
 * view hierarchy after [onAttach] (and before [onStart]).
 * - Similarly, presenter's view is detached from the hierarchy before [onDetach], but after [onStop].
 * - Presenter is identified by it's [id]. Id is provided as part of [arguments].
 * - Presenter can use [canChangeConfiguration] to indicate if it wants to be recreated or if
 * it will handle a configuration change on it's own.
 * - In case of a configuration change, presenter's parent should save it's state and recreate it,
 * or notify it about the change.
 * - Default implementation for [onBackPressed] will traverse the presenter hierarchy and stop as
 * soon as one of the presenters returns true (hence only the first presenter will actually
 * do a step back).
 * - On the other hand, [onActivityResult] and [onRequestPermissionsResult] is delivered to all
 * presenters in the hierarchy.
 *
 * Presenter state:
 * - Root is always saved.
 * - To identify argument bundles that were restored from saved state, use [isRestored]
 * - To guarantee that the presenter is recreated properly, make sure it's host view
 * has a unique ID and it's parent group's state is saved.
 * - If presenter's host view doesn't have an ID and the presenter does, it's state will
 * still be saved, but it won't be recreated automatically.
 *
 * Configuration changes (assuming the activity can change configuration):
 * - if [canChangeConfiguration] is marked as true, presenter will be just notified
 * - if [canChangeConfiguration] is false, presenters parent has to detach it
 * and recreate it from saved state. From the point of view of the detached presenter,
 * it is as if the activity was shut down and now is being restored from saved state.
 * - if there are multiple presenters in one host view and some can change configuration and
 * some can't, automatic configuration restore might change their order (recreated
 * presenters are added to the back). To prevent this, either handle those separately
 * in your code, or create a wrapper layout with it's own id that will identify
 * the position where the presenter should be restored.
 *
 * Presenter can be placed in any [ViewGroup], so you shouldn't assume that the
 * root view has any particular [ViewGroup.LayoutParams].
 *
 * ### Presenter as [ActionHost]
 *
 * Each presenter can be used as an [ActionHost]. By default each action is subscribed on the main
 * thread (unless the action is configured otherwise). Action execution is started after
 * the call to [onAttach] (to ensure that all state has been restored) and stopped
 * before the call to [onDetach] (to ensure state isn't changing any more). Actions posted
 * outside of this window are thrown away with an error.
 *
 * [Presenter] uses [ReplaySubject] as the proxy observable. Hence multiple
 * subscriptions to the proxy observables will not execute actions multiple times and will
 * always return the same results.
 * However, results of each action are cached, so try to avoid actions emitting
 * a high amount of items.
 *
 * @see [ActionHost]
 * @see [LifecycleHost]
 */
open class Presenter(
        host: PresenterHost, val view: View
) : LifecycleHost, ActionHost {

    companion object {
        @JvmField val IS_RESTORED_KEY = "glucose:is_restored"
    }

    /**
     * Create a Presenter with view inflated from layout resource.
     */
    constructor(context: PresenterHost, @LayoutRes layout: Int, parent: ViewGroup?) : this(
            context, LayoutInflater.from(context.activity).inflate(layout, parent, false)
    )

    /**************** External configuration properties (parent/user sets this) *******************/

    private var _arguments: Bundle? = null
    private var _host: PresenterHost? = host

    /**
     * The state [Bundle] of this presenter. Available when [state] >= ATTACHED,
     * otherwise throws a [LifecycleException].
     */
    val arguments: Bundle
        get() = _arguments ?: throw LifecycleException("Presenter is $state and has no arguments.")

    /**
     * The [PresenterHost] (context) of this presenter. Available when
     * [state] >= ALIVE, otherwise throws a [LifecycleException].
     */
    val host: PresenterHost
        get() = _host ?: throw LifecycleException("Presenter is $state and is disconnected from it's host.")

    /**
     * The id of this presenter (should be unique within the whole tree). It is used to identify
     * the state bundle when restoring the presenter hierarchy. For more info, see main description.
     *
     * This is a part of the presenters state and hence can be modified using the arguments bundle.
     * Default: [View.NO_ID]
     */
    val id: Int by NativeArgument(intBundler, View.NO_ID)

    /**
     * This property indicates whether the presenter can be attached automatically when the
     * hierarchy is being restored. Use this property for presenters that are managed
     * by an adapter or when the amount of recreated presenters is too high for smooth start.
     *
     * This is a part of the presenters state and hence can be modified using the arguments bundle.
     * Default: true
     */
    val canReattachAfterStateChange: Boolean by NativeArgument(booleanBundler, true)

    /**************** Internal configuration properties (parent shouldn't modify these) ***********/

    /**
     * Override this property to modify action queue size of this presenter.
     * Note that this value is used only once during creation (you can't change the host capacity
     * dynamically).
     *
     * Default: 5
     */
    open val actionHostCapacity = 5

    /**
     * Override this property to indicate that the presenter can't change configuration
     * and should be recreated in case of a configuration change.
     *
     * Default: true
     */
    open val canChangeConfiguration = true

    /**
     * Override this property to indicate that the presenter can't be reused and should
     * be destroyed as soon as it is recycled.
     *
     * Default: true
     */
    open val canBeReused = true

    /******************** Methods driving the lifecycle of this Presenter. ************************/

    private inline fun assertLifecycleChange(
            from: Lifecycle.State, to: Lifecycle.State, transition: () -> Unit
    ) {
        if (state != from) throw IllegalStateException("Something is wrong with the lifecycle!")
        transition()
        if (state != to) throw IllegalStateException("Something is wrong with the lifecycle! Maybe forgot to call super?")
    }

    // perform* methods are public so that other people can implement their own PresenterHosts

    /**
     * Perform a lifecycle change from ALIVE to ATTACHED.
     * Note: Use with caution. (You probably don't need this unless you are making a custom
     * [PresenterHost] or [PresenterGroup])
     */
    fun performAttach(arguments: Bundle) = assertLifecycleChange(ALIVE, ATTACHED) {
        onAttach(arguments)
        actionHost.startProcessingActions()
    }

    /**
     * Perform a lifecycle change from ATTACHED to STARTED.
     * Note: Use with caution. (You probably don't need this unless you are making a custom
     * [PresenterHost] or [PresenterGroup])
     */
    fun performStart() = assertLifecycleChange(ATTACHED, STARTED) { onStart() }

    /**
     * Perform a lifecycle change from STARTED to RESUMED.
     * Note: Use with caution. (You probably don't need this unless you are making a custom
     * [PresenterHost] or [PresenterGroup])
     */
    fun performResume() = assertLifecycleChange(STARTED, RESUMED) { onResume() }

    /**
     * Perform a lifecycle change from RESUMED to STARTED.
     * Note: Use with caution. (You probably don't need this unless you are making a custom
     * [PresenterHost] or [PresenterGroup])
     */
    fun performPause() = assertLifecycleChange(RESUMED, STARTED) { onPause() }

    /**
     * Perform a lifecycle change from STARTED to ATTACHED.
     * Note: Use with caution. (You probably don't need this unless you are making a custom
     * [PresenterHost] or [PresenterGroup])
     */
    fun performStop() = assertLifecycleChange(STARTED, ATTACHED) { onStop() }

    /**
     * Perform a lifecycle change from ATTACHED to ALIVE.
     * Note: Use with caution. (You probably don't need this unless you are making a custom
     * [PresenterHost] or [PresenterGroup])
     */
    fun performDetach() = assertLifecycleChange(ATTACHED, ALIVE) {
        actionHost.stopProcessingActions()
        onDetach()
    }

    /**
     * Perform a lifecycle change from ALIVE to DESTROYED.
     * Note: Use with caution. (You probably don't need this unless you are making a custom
     * [PresenterHost] or [PresenterGroup])
     */
    fun performDestroy() = assertLifecycleChange(ALIVE, DESTROYED) { onDestroy() }

    /**
     * Called when this presenter is attached to the main tree.
     *
     * Here you should initialize the presenter based on its arguments.
     *
     * Note that if [canBeReused] is false, this will be called only once.
     *
     * @see canBeReused
     */
    protected open fun onAttach(arguments: Bundle) {
        lifecycleLog("onAttach")
        _arguments = arguments
        lifecycleHost.state = ATTACHED
    }

    /**
     * @see Activity.onStart
     */
    protected open fun onStart() {
        lifecycleLog("onStart")
        lifecycleHost.state = STARTED
    }

    /**
     * @see Activity.onResume
     */
    protected open fun onResume() {
        lifecycleLog("onResume")
        lifecycleHost.state = RESUMED
    }

    /**
     * @see Activity.onPause
     */
    protected open fun onPause() {
        lifecycleHost.state = STARTED
        lifecycleLog("onPause")
    }

    /**
     * @see Activity.onStop
     */
    protected open fun onStop() {
        lifecycleHost.state = ATTACHED
        lifecycleLog("onStop")
    }

    /**
     * Called when this presenter is detached from the main tree.
     *
     * Here you should clean up all argument-based variables (and view state)
     * to make sure next attach goes smoothly.
     *
     * If [canBeReused] is false, this will be called only once and you don't
     * have to worry about next attach.
     *
     * @see canBeReused
     */
    protected open fun onDetach() {
        lifecycleHost.state = ALIVE
        lifecycleLog("onDetach")
        _arguments = null
    }

    /**
     * @see Activity.onDestroy
     */
    protected open fun onDestroy() {
        lifecycleHost.state = DESTROYED
        lifecycleLog("onDestroy")
        _host = null
    }

    /**
     * Called when this presenter changes configuration.
     *
     * Won't be called if [canChangeConfiguration] is false - instead, presenter
     * will be recreated.
     *
     * @see canChangeConfiguration
     * @see Activity.onConfigurationChanged
     */
    open fun onConfigurationChanged(newConfig: Configuration) {
        if (!canChangeConfiguration) {
            throw IllegalStateException("$this cannot change configuration and should have been destroyed.")
        }
        host.factory.prepareConfigChange()
        lifecycleLog("onConfigurationChanged")
    }

    /**
     * @see Activity.onActivityResult
     */
    open fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
        lifecycleLog("onActivityResult $resultCode for request $requestCode")
    }

    /**
     * Notification that the Presenter should move to the previous state.
     * @return false if presenter can't go back, true if it successfully moved back.
     *
     * @see Activity.onBackPressed
     */
    open fun onBackPressed(): Boolean = false

    /**
     * Save state of this [Presenter] and all it's children.
     *
     * Given container holds a flattened parent-id <-> child-state "map" and returned
     * bundle holds a state tree. This means that child bundles can be
     * present twice - once if their parent view has an ID (in container) and second time
     * if they have an ID (in bundle).
     */
    open fun saveHierarchyState(container: SparseArray<Bundle>): Bundle {
        val state = Bundle()
        onSaveInstanceState(state)
        if (id != View.NO_ID) {
            container.append(id, state)
        }
        return state
    }

    /**
     * Save the state of this [Presenter] (without children) into a bundle.
     *
     * For more info about state preservation, see class description.
     *
     * @see Activity.onSaveInstanceState
     * @see Presenter.saveHierarchyState
     */
    open fun onSaveInstanceState(out: Bundle) {
        out.putAll(arguments)   //make a copy of the current state
        out.setRestored(true)
    }

    /**
     * @see Activity.onTrimMemory
     */
    open fun onTrimMemory(level: Int) {}

    /**
     * @see Activity.onRequestPermissionsResult
     */
    open fun onRequestPermissionsResult(
            requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    ) { }

    /******************** [LifecycleHost] implementation ******************************************/

    private val lifecycleHost = LifecycleDelegate()

    override val state: Lifecycle.State
        get() = lifecycleHost.state

    override val lifecycleEvents: Observable<Lifecycle.Event>
        get() = lifecycleHost.lifecycleEvents

    /**
     * @see [LifecycleHost.addEventCallback]
     */
    override fun addEventCallback(event: Lifecycle.Event, callback: () -> Unit) {
        lifecycleHost.addEventCallback(event, callback)
    }

    /**
     * @see [LifecycleHost.removeEventCallback]
     */
    override fun removeEventCallback(event: Lifecycle.Event, callback: () -> Unit): Boolean {
        return lifecycleHost.removeEventCallback(event, callback)
    }

    /******************** [ActionHost] implementation *********************************************/

    private val actionHost = ActionDelegate(AndroidSchedulers.mainThread(), actionHostCapacity)

    /**
     * @see [ActionHost.post]
     */
    override fun <R> post(action: Observable<R>): Observable<R> = actionHost.post(action)

}