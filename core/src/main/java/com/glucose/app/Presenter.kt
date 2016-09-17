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
import rx.subjects.ReplaySubject
import rx.android.schedulers.AndroidSchedulers

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
        context: PresenterHost, val view: View
) : LifecycleHost, ActionHost {

    companion object {
        @JvmField val IS_RESTORED_KEY = "glucose:is_restored"
    }

    /**
     * Create a Presenter with view initialized from layout resource.
     */
    constructor(context: PresenterContext, @LayoutRes layout: Int, parent: ViewGroup?) : this(
            context, LayoutInflater.from(context.activity).inflate(layout, parent, false)
    )

    var arguments: Bundle = Bundle()
        private set
        get() {
            if (!this.isAttached)
                throw LifecycleException("Presenter is not attached. Cannot access arguments.")
            else return field
        }

    val ctx: PresenterHost = context
        get() {
            if (this.isDestroyed)
                throw LifecycleException("Accessing Context on a destroyed presenter.")
            return field
        }

    val id: Int by NativeArgument(intBundler, View.NO_ID)
    val canRecreateFromState: Boolean by NativeArgument(booleanBundler, true)

    /******************** Methods driving the lifecycle of this Presenter. ************************/

    private inline fun assertLifecycleChange(
            from: Lifecycle.State, to: Lifecycle.State, transition: () -> Unit
    ) {
        if (state != from) throw IllegalStateException("Something is wrong with the lifecycle!")
        transition()
        if (state != to) throw IllegalStateException("Something is wrong with the lifecycle! Maybe forgot to call super?")
    }

    // perform* methods are public so that other people can implement their own PresenterHosts

    fun performAttach(arguments: Bundle) = assertLifecycleChange(ALIVE, ATTACHED) {
        onAttach(arguments)
        actionHost.startProcessingActions()
    }

    fun performStart() = assertLifecycleChange(ATTACHED, STARTED) { onStart() }

    fun performResume() = assertLifecycleChange(STARTED, RESUMED) { onResume() }

    fun performPause() = assertLifecycleChange(RESUMED, STARTED) { onPause() }

    fun performStop() = assertLifecycleChange(STARTED, ATTACHED) { onStop() }

    fun performDetach() = assertLifecycleChange(ATTACHED, ALIVE) {
        actionHost.stopProcessingActions()
        onDetach()
    }

    fun performDestroy() = assertLifecycleChange(ALIVE, DESTROYED) { onDestroy() }

    protected open fun onAttach(arguments: Bundle) {
        lifecycleLog("onAttach")
        this.arguments = arguments
        lifecycleHost.mState = ATTACHED
    }

    /**
     * @see Activity.onStart
     */
    protected open fun onStart() {
        lifecycleLog("onStart")
        lifecycleHost.mState = STARTED
    }

    /**
     * @see Activity.onResume
     */
    protected open fun onResume() {
        lifecycleLog("onResume")
        lifecycleHost.mState = RESUMED
    }

    /**
     * @see Activity.onPause
     */
    protected open fun onPause() {
        lifecycleHost.mState = STARTED
        lifecycleLog("onPause")
    }

    /**
     * @see Activity.onStop
     */
    protected open fun onStop() {
        lifecycleHost.mState = ATTACHED
        lifecycleLog("onStop")
    }

    protected open fun onDetach() {
        lifecycleHost.mState = ALIVE
        arguments = Bundle()    //drop old arguments
        lifecycleLog("onDetach")
    }

    /**
     * @see Activity.onDestroy
     */
    protected open fun onDestroy() {
        lifecycleHost.mState = DESTROYED
        lifecycleLog("onDestroy")
    }

    /**
     * True if this presenter can handle a configuration change.
     */
    open val canChangeConfiguration = true

    /**
     * True if this presenter can be used again after being recycled.
     * If false, it is guaranteed to be attached only once.
     */
    open val canBeReused = true

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
        ctx.factory.prepareConfigChange()
        lifecycleLog("onConfigurationChanged")
    }

    /**
     * Notification to all attached presenters that activity has returned a result.
     *
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
     * Given container holds a flattened id <-> state "map" and returned
     * bundle holds a state tree. This means that child bundles can be
     * present twice - once if their parent view has an ID and second time
     * if they have an ID.
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

    protected open val actionHostCapacity = 5

    private val actionHost = ActionDelegate(AndroidSchedulers.mainThread(), actionHostCapacity)

    /**
     * @see [ActionHost.post]
     */
    override fun <R> post(action: Observable<R>): Observable<R> = actionHost.post(action)

}