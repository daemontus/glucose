package com.glucose.app

import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.support.annotation.IdRes
import android.util.SparseArray
import android.view.View
import android.view.ViewGroup
import com.glucose.Log
import com.glucose.app.presenter.*
import rx.subjects.PublishSubject
import rx.Observable
import java.util.*

/**
 * Note: All children are restored synchronously when attaching. In case of a deep hierarchy,
 * this can cause frame drops. To avoid this, we have to introduce some mechanism to
 * "break the chain".
 */
open class PresenterGroup : Presenter {

    companion object {
        val CHILDREN_KEY = "glucose:presenter_children"
    }

    constructor(context: PresenterContext, view: View) : super(context, view)
    constructor(context: PresenterContext, layout: Int) : super(context, layout)

    private val children = ArrayList<Presenter>()

    // ============================ Children and Lifecycle =============================

    override fun onAttach(arguments: Bundle) {
        super.onAttach(arguments)
        if (arguments.isRestored()) {
            val savedChildren = arguments.getSparseParcelableArray<PresenterParcel>(CHILDREN_KEY)
            for (i in 0 until savedChildren.size()) {
                val parentId = savedChildren.keyAt(i)
                val child = savedChildren.valueAt(i)
                findOptionalView<PresenterLayout>(parentId)?.let { parent ->
                    add(parentId, Class.forName(child.clazz) as Class<Presenter>, child.state)
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
            remove(it)
        }
        super.onDetach()
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        //TODO: This mechanism might change order of views in containers if config change occurs
        class ChildState(
                val clazz: Class<out Presenter>,
                val tree: Bundle,
                val map: SparseArray<Bundle>
        )
        val childStates = ArrayList<Pair<PresenterLayout, ChildState>>()
        for (presenter in children) {
            if (!presenter.canChangeConfiguration) {
                val map = SparseArray<Bundle>()
                val tree = presenter.saveHierarchyState(map)
                childStates.add(Pair(
                        presenter.view.parent as PresenterLayout,
                        ChildState(presenter.javaClass, tree, map)
                ))
                remove(presenter)
            }
        }
        children.forEach {
            it.onConfigurationChanged(newConfig)
        }
        //Now that the whole subtree is done, clear factory and instantiate saved presenters
        super.onConfigurationChanged(newConfig)
        for ((layout, state) in childStates) {
            ctx.presenterStates = state.map
            add(layout, state.clazz, state.tree)
            ctx.presenterStates = null
        }
    }

    override fun saveHierarchyState(container: SparseArray<Bundle>): Bundle {
        val myState = super.saveHierarchyState(container)
        val childrenMap = SparseArray<PresenterParcel>()
        children.forEach {
            //We have to call it on all children, because even if we drop this bundle, some
            //deeper child might save itself to the container
            val childState = PresenterParcel(
                    it.javaClass.name, it.saveHierarchyState(container)
            )
            if (it.view.parent is View) {
                val parent = it.view.parent as View
                if (parent.id != View.NO_ID) {
                    childrenMap.put(parent.id, childState)
                }
            }
        }
        myState.putSparseParcelableArray(CHILDREN_KEY, childrenMap)
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

    // ========================== Children manipulation ============================================

    private val childAdded: PublishSubject<Presenter> = PublishSubject.create()
    private val childRemoved: PublishSubject<Class<out Presenter>> = PublishSubject.create()

    //TODO: Can we somehow make sure this does not execute after detach?
    val onChildAdd: Observable<Presenter> = childAdded
    val onChildRemoved: Observable<Class<out Presenter>> = childRemoved

    /**
     * Obtain new presenter and attach it to a ViewGroup with given id.
     */
    fun <P: Presenter> add(
            @IdRes id: Int, clazz: Class<P>, arguments: Bundle = Bundle()
    ): P = add(findView<PresenterLayout>(id), clazz, arguments)

    /**
     * Obtain new presenter and attach it to a ViewGroup.
     */
    fun <P: Presenter> add(
            parent: PresenterLayout, clazz: Class<P>, arguments: Bundle = Bundle()
    ): P {
        val presenter = ctx.attach(clazz, arguments)
        parent.addView(presenter.view)
        children.add(presenter)
        if (isStarted) presenter.performStart()
        if (isResumed) presenter.performResume()
        actionLog("Attached $presenter")
        childAdded.onNext(presenter)
        return presenter
    }

    /**
     * Detach and recycle a presenter.
     */
    fun remove(presenter: Presenter): Unit {
        if (!presenter.isAlive) throw IllegalStateException("Removing presenter that hasn't been created properly")
        if (!presenter.isAttached) throw IllegalStateException("Removing presenter that hasn't been attached properly")
        if (presenter !in children) throw IllegalStateException("Removing presenter that isn't attached to ${this@PresenterGroup}")
        if (isResumed) presenter.performPause()
        if (isStarted) presenter.performStop()
        val parent = presenter.view.parent as PresenterLayout
        parent.removeView(presenter.view)
        children.remove(presenter)
        actionLog("Detached $presenter")
        childRemoved.onNext(presenter.javaClass)
        ctx.detach(presenter)
    }

    // ============================ Child retrieval ================================================

    val presenters: List<Presenter>
        get() = children.toList()

}