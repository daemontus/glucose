package com.glucose.app

import android.app.Activity
import android.content.ComponentCallbacks2
import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.os.Parcelable
import android.support.annotation.IdRes
import android.support.annotation.MainThread
import android.util.SparseArray
import android.view.View
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
        private val rootArguments: Bundle = Bundle(),
        private @IdRes val rootId: Int = View.NO_ID
) {

    companion object {
        val STATE_KEY = "glucose:presenter_hierarchy"
    }

    val factory = PresenterFactory(this)
    var root: Presenter by Delegates.notNull()

    //temporary storage for state array while the hierarchy is being restored
    private var presenterStates: SparseArray<Bundle>? = null

    /**
     * Obtain new presenter instance and attach it to this context by assigning it arguments.
     *
     * Arguments are based on the saved state and provided data.
     */
    fun <P: Presenter> attach(presenter: Class<P>, arguments: Bundle = Bundle(), @IdRes id: Int = View.NO_ID): P {
        val instance = factory.obtain(presenter)
        val finalId = if (id == View.NO_ID) instance.id else id
        val savedState = if (finalId == View.NO_ID) null else {
            presenterStates?.get(id)
        }
        if (savedState != null) {
            arguments.putAll(savedState)
        }
        arguments.putInt("id", finalId)
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

    /**
     * The caller should add returned view to the view hierarchy before the view state is restored.
     */
    fun onCreate(savedInstanceState: Bundle?): View {
        presenterStates = savedInstanceState?.getSparseParcelableArray<Bundle>(STATE_KEY)
        root = attach(rootPresenter, rootArguments, rootId) //should recreate the whole tree
        presenterStates = null  //forget about the state so that newly attached presenters don't suffer from it.
        return root.view
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

    /**
     * Config change is a little complex thanks to Presenter caching.
     * First, as we go down the tree, we detach all presenters that can't handle
     * configuration change. Every time we hit a leaf, we have to clear the factory
     * to make sure what the detached presenters won't get reused (This is done by each Presenter).
     * Then as we go up, we restore the presenters using the state saved on the way down.
     * Finally, when we reach the top, we restore the whole view hierarchy state, because
     * we don't know what happened there.
     */
    fun onConfigurationChanged(newConfig: Configuration) {
        val hierarchyState = SparseArray<Parcelable>()
        root.view.saveHierarchyState(hierarchyState)
        val container = SparseArray<Bundle>()
        presenterStates = container
        root.saveHierarchyState(container)
        if (root.canChangeConfiguration) {
            root.onConfigurationChanged(newConfig)
        } else {
            val resumed = root.isResumed
            if (resumed) root.performPause()
            val started = root.isStarted
            if (started) root.performStop()
            detach(root)
            factory.onConfigurationChange(newConfig)
            root = attach(rootPresenter, rootArguments, rootId)
            if (started) root.performStart()
            if (resumed) root.performResume()
        }
        presenterStates = null
        root.view.restoreHierarchyState(hierarchyState)
    }

    fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
        root.performActivityResult(requestCode, resultCode, data)
    }

    fun onTrimMemory(level: Int) {
        if (level > ComponentCallbacks2.TRIM_MEMORY_RUNNING_LOW) factory.trimMemory()
        root.onTrimMemory(level)
    }

    fun onSaveInstanceState(state: Bundle) {
        val container = SparseArray<Bundle>()
        root.saveHierarchyState(container)
        state.putSparseParcelableArray(STATE_KEY, container)
    }

}