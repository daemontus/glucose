package com.glucose.app

import android.app.Activity
import android.content.ComponentCallbacks2
import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.support.annotation.MainThread
import android.util.SparseArray
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import com.glucose.app.presenter.Lifecycle
import com.glucose.app.presenter.LifecycleException
import com.glucose.app.presenter.getId
import com.glucose.app.presenter.saveWholeState


/**
 * PresenterDelegate is an implementation of [PresenterHost] that relies on the activity
 * itself to call appropriate methods to ensure proper lifecycle of the components.
 *
 * @see PresenterHost
 * @see RootActivity
 * @see PresenterFactory
 */
@MainThread
class PresenterDelegate(
        override val activity: Activity,
        private val rootPresenter: Class<out Presenter>,
        private val rootArguments: Bundle = Bundle()
) : PresenterHost {

    companion object {
        val HIERARCHY_TREE_KEY = "glucose:presenter_hierarchy:tree"
        val HIERARCHY_MAP_KEY = "glucose:presenter_hierarchy:map"
    }

    override val factory = PresenterFactory(this)
    override var root: Presenter? = null
        private set

    //temporary storage for state map while the hierarchy is being restored
    private var presenterStates: SparseArray<Bundle>? = null

    /**
     * @see [PresenterHost.attach]
     */
    override fun <P: Presenter> attach(clazz: Class<P>, arguments: Bundle, parent: ViewGroup?): P {
        val instance = factory.obtain(clazz, parent)
        val id = arguments.getId()
        if (id != View.NO_ID) {
            presenterStates?.get(id)?.let { savedState ->
                arguments.putAll(savedState)
            }
        }
        instance.performAttach(arguments)
        return instance
    }

    /**
     * @see [PresenterHost.attachWithState]
     */
    override fun <P : Presenter> attachWithState(clazz: Class<P>, savedState: SparseArray<Bundle>, arguments: Bundle, parent: ViewGroup?): P {
        presenterStates = savedState
        val p = attach(clazz, arguments, parent)
        presenterStates = null
        return p
    }

    override fun detach(presenter: Presenter) {
        presenter.performDetach()
        factory.recycle(presenter)
    }

    // ====================================== Lifecycle ============================================

    private var parent: ViewGroup? = null

    /**
     * Initialize this delegate with a saved state. The caller should add the returned
     * view to the main view hierarchy before the state of the view hierarchy is restored
     * by the activity (or restore it manually).
     *
     * The caller doesn't have to remove this view at any time.
     */
    fun onCreate(savedInstanceState: Bundle?): View {
        val parent = FrameLayout(activity)
        this.parent = parent
        presenterStates = savedInstanceState?.getSparseParcelableArray(HIERARCHY_MAP_KEY)
        val stateTree = savedInstanceState?.getParcelable<Bundle>(HIERARCHY_TREE_KEY)
        val arguments = if (stateTree == null) rootArguments else Bundle().apply {
            this.putAll(rootArguments)
            this.putAll(stateTree)
        }
        val root = attach(rootPresenter, arguments, parent) //should recreate the whole tree
        this.root = root
        parent.addView(root.view)
        presenterStates = null  //forget about the state so that newly attached presenters don't suffer from it.
        return parent
    }

    fun onStart() {
        root?.performStart() ?: throw LifecycleException("Delegate is destroyed or not created.")
    }

    fun onResume() {
        root?.performResume() ?: throw LifecycleException("Delegate is destroyed or not created.")
    }

    fun onPause() {
        root?.performPause() ?: throw LifecycleException("Delegate is destroyed or not created.")
    }

    fun onStop() {
        root?.performStop() ?: throw LifecycleException("Delegate is destroyed or not created.")
    }

    /**
     * Before calling this, the view should be removed from the main hierarchy.
     */
    fun onDestroy() {
        parent?.removeAllViews()
        detach(root ?: throw LifecycleException("Delegate is destroyed or not created."))
        root = null
        factory.onDestroy()
    }

    fun onBackPressed(): Boolean = root?.onBackPressed() ?: throw LifecycleException("Delegate is destroyed or not created.")

    fun onConfigurationChanged(newConfig: Configuration) {
        root?.let { root ->
        parent!!.let { parent ->
            if (root.canChangeConfiguration) {
                factory.performConfigChange(newConfig)
                root.onConfigurationChanged(newConfig)
            } else {
                val savedState = root.saveWholeState()
                val state = root.state
                if (state >= Lifecycle.State.RESUMED) root.performPause()
                if (state >= Lifecycle.State.STARTED) root.performStop()
                parent.removeView(root.view)
                this.root = null
                detach(root)
                factory.performConfigChange(newConfig)
                presenterStates = savedState.map
                val newRoot = attach(rootPresenter, savedState.tree, parent)
                this.root = newRoot
                parent.addView(newRoot.view)
                presenterStates = null
                newRoot.view.restoreHierarchyState(savedState.viewState)
                if (state >= Lifecycle.State.STARTED) newRoot.performStart()
                if (state >= Lifecycle.State.RESUMED) newRoot.performResume()
            }
        } } ?: throw LifecycleException("Delegate is destroyed or not created.")
    }

    fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
        root?.onActivityResult(requestCode, resultCode, data)
                ?: throw LifecycleException("Delegate is destroyed or not created.")
    }

    fun onTrimMemory(level: Int) {
        if (level > ComponentCallbacks2.TRIM_MEMORY_RUNNING_LOW) factory.trimMemory()
        root?.onTrimMemory(level)
                ?: throw LifecycleException("Delegate is destroyed or not created.")
    }

    fun onSaveInstanceState(state: Bundle) {
        //Note: This will still consume 2x more space in the state bundle
        //thanks to the way parcelables work. Maybe we can do something better in the future.
        val map = SparseArray<Bundle>()
        val tree = root?.saveHierarchyState(map)
                ?: throw LifecycleException("Delegate is destroyed or not created.")
        state.putSparseParcelableArray(HIERARCHY_MAP_KEY, map)
        state.putParcelable(HIERARCHY_TREE_KEY, tree)
    }

    fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        root?.onRequestPermissionsResult(requestCode, permissions, grantResults)
                ?: throw LifecycleException("Delegate is destroyed or not created.")
    }

}