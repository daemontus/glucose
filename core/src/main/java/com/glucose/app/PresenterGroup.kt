package com.glucose.app

import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.support.annotation.IdRes
import android.support.annotation.LayoutRes
import android.util.SparseArray
import android.view.View
import android.view.ViewGroup
import com.glucose.app.presenter.*
import com.glucose.util.newSyntheticId
import rx.Observable
import rx.subjects.PublishSubject
import java.util.*

/**
 * [PresenterGroup] is an extension of [Presenter] that can
 */
open class PresenterGroup : Presenter {

    companion object {
        val CHILDREN_KEY = "glucose:presenter_children"
    }

    constructor(host: PresenterHost, view: View) : super(host, view)
    constructor(
            host: PresenterHost, @LayoutRes layout: Int, parent: ViewGroup?
    ) : super(host, layout, parent)

    private val children = ArrayList<Presenter>()

    // ============================ Children and Lifecycle =============================

    override fun onAttach(arguments: Bundle) {
        super.onAttach(arguments)
        if (arguments.isRestored()) {
            val savedChildren = arguments.getParcelableArrayList<PresenterParcel>(CHILDREN_KEY)
            savedChildren.forEach {
                val (clazz, state, parentId) = it
                findOptionalView<ViewGroup>(parentId)?.let { parent ->
                    //State probably has an outdated classloader.
                    state.classLoader = it.javaClass.classLoader
                    attach(parentId, Class.forName(clazz).asSubclass(Presenter::class.java), state)
                }
            }
            //this should ensure that children that were not restored will be garbage collected
            arguments.remove(CHILDREN_KEY)
        }
    }

    override fun onStart() {
        super.onStart()
        children.forEach { it.performStart() }
    }

    override fun onResume() {
        super.onResume()
        children.forEach { it.performResume() }
    }

    override fun onBackPressed(): Boolean {
        return children.fold(false, {
            wentBack, child -> wentBack || child.onBackPressed()
        }) || super.onBackPressed()
    }

    override fun onPause() {
        children.forEach { it.performPause() }
        super.onPause()
    }

    override fun onStop() {
        children.forEach { it.performStop() }
        super.onStop()
    }

    override fun onDetach() {
        children.toList().forEach {
            detach(it)
        }
        super.onDetach()
    }

    /**
     * Config change is a little complex thanks to Presenter caching.
     * First, as we go down the tree, we detach all presenters that can't handle
     * configuration change. Every time we hit a leaf, we have to clear the factory
     * to make sure that the detached presenters won't get reused (This is done by each Presenter
     * in super). Then as we go up, we restore the presenters using the state saved on the
     * way down.
     *
     * Furthermore, in order to preserve ordering of presenters if there are more in one
     * group and some need to be recreated, we create an extra replacement view that
     * takes it's place while other children change configuration.
     */
    override fun onConfigurationChanged(newConfig: Configuration) {
        val childStates = SparseArray<Pair<ViewGroup, ChildState>>()
        for (presenter in children.toList()) {  //make a copy
            if (!presenter.canChangeConfiguration) {
                val parent = presenter.view.parent as ViewGroup
                val replacementId = newSyntheticId()
                childStates.put(replacementId, Pair(parent, presenter.saveWholeState()))
                val index = parent.indexOfChild(presenter.view)
                detach(presenter)
                parent.addView(View(host.activity).apply { this.id = replacementId }, index)
            }
        }
        children.forEach {
            it.onConfigurationChanged(newConfig)
        }
        //Now that the whole subtree is done, clear factory and instantiate saved presenters
        super.onConfigurationChanged(newConfig)
        for (i in 0 until childStates.size()) {
            val id = childStates.keyAt(i)
            val replacement = findOptionalView<View>(id)
            if (replacement != null) {
                val (layout, state) = childStates.valueAt(i)
                val index = layout.indexOfChild(replacement)
                layout.removeView(replacement)
                val presenter = host.attachWithState(state.clazz, state.map, state.tree, layout)
                layout.addView(presenter.view, index)
                addChild(presenter)
                presenter.view.restoreHierarchyState(state.viewState)
            }
        }
    }

    override fun saveHierarchyState(container: SparseArray<Bundle>): Bundle {
        val myState = super.saveHierarchyState(container)
        val childrenList = ArrayList<PresenterParcel>()
        children.forEach {
            //We have to call it on all children, because even if we drop this bundle, some
            //deeper child might save itself to the container
            val childState = PresenterParcel(
                    it.javaClass.name, it.saveHierarchyState(container)
            )
            if (it.canReattachAfterStateChange) {
                val parent = it.view.parent as View
                if (parent.id != View.NO_ID) {
                    childrenList.add(childState.copy(parentId = parent.id))
                }
            }
        }
        myState.putParcelableArrayList(CHILDREN_KEY, childrenList)
        return myState
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
        super.onActivityResult(requestCode, resultCode, data)
        children.forEach {
            it.onActivityResult(requestCode, resultCode, data)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        children.forEach {
            it.onRequestPermissionsResult(requestCode, permissions, grantResults)
        }
    }

    override fun onTrimMemory(level: Int) {
        super.onTrimMemory(level)
        children.forEach {
            it.onTrimMemory(level)
        }
    }

    override fun onDestroy() {
        //no children should be present any more
        childAdded.onCompleted()
        childRemoved.onCompleted()
        childAddedRecursive.onCompleted()
        childRemovedRecursive.onCompleted()
        super.onDestroy()
    }

    // ========================== Children manipulation ============================================

    private val childAdded: PublishSubject<Presenter> = PublishSubject.create()
    private val childRemoved: PublishSubject<Presenter> = PublishSubject.create()
    private val childAddedRecursive: PublishSubject<Presenter> = PublishSubject.create()
    private val childRemovedRecursive: PublishSubject<Presenter> = PublishSubject.create()

    init {
        childAdded.subscribe(childAddedRecursive).until(Lifecycle.Event.DESTROY)
        childRemoved.subscribe(childRemovedRecursive).until(Lifecycle.Event.DESTROY)
    }

    val onChildAdd: Observable<Presenter> = childAdded
    val onChildRemove: Observable<Presenter> = childRemoved
    val onChildAddRecursive: Observable<Presenter> = childAddedRecursive
    val onChildRemoveRecursive: Observable<Presenter> = childRemovedRecursive

    /**
     * Obtain new presenter and add it to this group.
     */
    fun <P: Presenter> attach(@IdRes parent: Int, presenter: Class<P>, arguments: Bundle = Bundle()): P
        = attach(findView<ViewGroup>(parent), presenter, arguments)

    /**
     * Obtain new presenter and add it to this group.
     *
     * WARNING: If the parent view doesn't have an ID, the presenter won't be restored from state.
     */
    fun <P: Presenter> attach(parentView: ViewGroup, presenter: Class<P>, arguments: Bundle = Bundle()): P {
        val instance = host.attach(presenter, arguments, parentView)
        add(parentView, instance)
        return instance
    }

    /**
     * Add a presenter that is already attached to the same context.
     *
     * Incorrect usage of this method may cause presenter leaks and lifecycle
     * problems. Prefer using [attach] when possible.
     *
     * Typical use case: Moving a presenter between groups without detaching.
     */
    fun add(@IdRes parent: Int, presenter: Presenter) = add(findView<ViewGroup>(parent), presenter)

    /**
     * Add a presenter that is already attached to the same context.
     *
     * Incorrect usage of this method may cause presenter leaks and lifecycle
     * problems. Prefer using [attach] when possible.
     *
     * Typical use case: Moving a presenter between groups without detaching.
     *
     * WARNING: If the parent does not have an ID, the presenter won't be restored from state.
     */
    fun add(parentView: ViewGroup, presenter: Presenter) {
        if (!presenter.isAttached) {
            throw LifecycleException("$presenter is not attached!")
        }
        if (presenter.view.parent != null) {
            throw LifecycleException("$presenter view is already added to ${presenter.view.parent}")
        }
        if (presenter.host != this.host) {
            throw LifecycleException("$presenter is attached to ${presenter.host} instead of ${this.host}")
        }
        parentView.addView(presenter.view)
        addChild(presenter)
    }

    private fun addChild(presenter: Presenter) {
        children.add(presenter)
        if (this.isStarted) presenter.performStart()
        if (this.isResumed) presenter.performResume()
        if (presenter is PresenterGroup) {
            presenter.onChildAddRecursive
                    .subscribe(childAddedRecursive).until(presenter, Lifecycle.Event.DETACH)
            presenter.onChildRemoveRecursive
                    .subscribe(childRemovedRecursive).until(presenter, Lifecycle.Event.DETACH)
        }
        childAdded.onNext(presenter)
    }

    /**
     * Remove presenter from view hierarchy, but keep it attached to the context.
     *
     * Incorrect usage of this method may cause presenter leaks and lifecycle
     * problems. Prefer using [detach] when possible.
     *
     * Typical use case: Moving a presenter between groups without detaching.
     */
    fun remove(presenter: Presenter): Presenter {
        if (presenter !in children) {
            throw LifecycleException("Removing presenter that isn't attached to ${this@PresenterGroup}")
        }
        if (isResumed) presenter.performPause()
        if (isStarted) presenter.performStop()
        children.remove(presenter)
        val parent = presenter.view.parent as ViewGroup
        parent.removeView(presenter.view)
        childRemoved.onNext(presenter)
        return presenter
    }

    /**
     * Detach and recycle a presenter.
     */
    fun detach(presenter: Presenter): Unit {
        host.detach(remove(presenter))
    }

    // ============================ Child retrieval ================================================

    val presenters: List<Presenter>
        get() = children.toList()

}