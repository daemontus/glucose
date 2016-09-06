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
import com.glucose.app.presenter.getId
import com.glucose.app.presenter.isResumed
import com.glucose.app.presenter.isStarted
import com.glucose.app.presenter.saveWholeState
import kotlin.properties.Delegates


/**
 * PresenterContext is responsible for managing the root of the presenter hierarchy,
 * especially with regards to the state preservation and configuration changes.
 *
 * It relies on an [Activity] to provide it with appropriate lifecycle callbacks.
 *
 * @see RootActivity
 * @see PresenterFactory
 */
@MainThread
class PresenterContext(
        val activity: Activity,
        private val rootPresenter: Class<out Presenter>,
        private val rootArguments: Bundle = Bundle()
) {

    companion object {
        val HIERARCHY_TREE_KEY = "glucose:presenter_hierarchy:tree"
        val HIERARCHY_MAP_KEY = "glucose:presenter_hierarchy:map"
    }

    val factory = PresenterFactory(this)
    var root: Presenter by Delegates.notNull()
        private set

    //temporary storage for state array while the hierarchy is being restored
    internal var presenterStates: SparseArray<Bundle>? = null

    /**
     * Obtain new presenter instance and attach it to this context by assigning it arguments.
     *
     * Arguments are based on the saved state and provided data.
     */
    fun <P: Presenter> attach(presenter: Class<P>, arguments: Bundle = Bundle(), parent: ViewGroup? = null): P {
        val instance = factory.obtain(presenter, parent)
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
     * Detach and recycle given presenter.
     */
    fun detach(presenter: Presenter) {
        presenter.performDetach()
        factory.recycle(presenter)
    }

    // ====================================== Lifecycle ============================================

    private var parent: ViewGroup by Delegates.notNull()

    /**
     * The caller should add returned view to the view hierarchy before the view state is restored.
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
        root = attach(rootPresenter, arguments, parent) //should recreate the whole tree
        presenterStates = null  //forget about the state so that newly attached presenters don't suffer from it.
        return parent
    }

    fun onStart() {
        root.performStart()
    }

    fun onResume() {
        root.performResume()
    }

    fun onPause() {
        root.performPause()
    }

    fun onStop() {
        root.performStop()
    }

    /**
     * Before calling this, the view should be removed from the main hierarchy.
     */
    fun onDestroy() {
        detach(root)
        factory.onDestroy()
    }

    fun onBackPressed(): Boolean = root.onBackPressed()

    fun onConfigurationChanged(newConfig: Configuration) {
        if (root.canChangeConfiguration) {
            root.onConfigurationChanged(newConfig)
        } else {
            val state = root.saveWholeState()
            val resumed = root.isResumed
            if (resumed) root.performPause()
            val started = root.isStarted
            if (started) root.performStop()
            parent.removeView(root.view)
            detach(root)
            factory.onConfigurationChange(newConfig)
            presenterStates = state.map
            root = attach(rootPresenter, state.tree, parent)
            parent.addView(root.view)
            presenterStates = null
            root.view.restoreHierarchyState(state.viewState)
            if (started) root.performStart()
            if (resumed) root.performResume()
        }
    }

    fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
        root.onActivityResult(requestCode, resultCode, data)
    }

    fun onTrimMemory(level: Int) {
        if (level > ComponentCallbacks2.TRIM_MEMORY_RUNNING_LOW) factory.trimMemory()
        root.onTrimMemory(level)
    }

    fun onSaveInstanceState(state: Bundle) {
        //Note: This will still consume 2x more space in the state bundle
        //thanks to the way parcelables work. Maybe we can do something better in the future.
        val map = SparseArray<Bundle>()
        val tree = root.saveHierarchyState(map)
        state.putSparseParcelableArray(HIERARCHY_MAP_KEY, map)
        state.putParcelable(HIERARCHY_TREE_KEY, tree)
    }

    fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        root.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

}