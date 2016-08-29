package com.glucose.app

import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.support.annotation.IdRes
import android.util.SparseArray
import android.view.View
import com.glucose.Log
import com.glucose.app.presenter.*
import rx.subjects.PublishSubject
import rx.Observable
import java.util.*

/**
 * WARNING: Currently, if the [PresenterLayout] has an ID, but the [Presenter] inside doesn't,
 * the presenter will be restored even with it's state (but not recursively, unless similar situation occurs)
 *
 * Note: All children are restored synchronously when attaching. In case of a deep hierarchy,
 * this can cause frame drops. To avoid this, we have to introduce some mechanism to
 * "break the chain".
 *
 * TODO: Polish those try-catch blocks.
 * TODO: Prevent presenter leaks in case of errors where possible.
 */
open class PresenterGroup : Presenter {

    companion object {
        val CHILDREN_ID_KEY = "glucose:presenter_children_ids"
        val CHILDREN_CLASS_KEY = "glucose:presenter_children_classes"
        val CHILDREN_STATE_KEY = "glucose:presenter_children_state"
    }

    constructor(context: PresenterContext, view: View) : super(context, view)
    constructor(context: PresenterContext, layout: Int) : super(context, layout)

    private val children = ArrayList<Presenter>()

    // ============================ Children and Lifecycle =============================

    override fun onAttach(arguments: Bundle, isFresh: Boolean) {
        super.onAttach(arguments, isFresh)
        val childIds = arguments.getIntArray(CHILDREN_ID_KEY)
        val childClasses = arguments.getStringArray(CHILDREN_CLASS_KEY)
        val childStates = arguments.getParcelableArrayList<Bundle>(CHILDREN_STATE_KEY)
        if (childIds != null && childClasses != null) {
            for (i in childIds.indices) {
                val id = childIds[i]
                val className = childClasses[i]
                val state = childStates[i]
                val layout = findOptionalView<PresenterLayout>(id)
                if (layout != null) {
                    Log.d("Restored $className in $id")
                    add(layout, Class.forName(className) as Class<Presenter>, state)
                }
            }
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
        return children.fold(false, { wentBack, child -> wentBack || child.onBackPressed() }) || super.onBackPressed()
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
        //TODO: Action processing should probably stop here - but how?
        children.toList().forEach {
            remove(it)
        }
        children.clear()
        super.onDetach()
    }

    override val canChangeConfiguration: Boolean = true

    override fun onConfigurationChanged(newConfig: Configuration) {
        //child state is saved inside the global state array in the context
        val savedState = ArrayList<Pair<PresenterLayout, Class<Presenter>>>()
        for (presenter in children) {
            if (!presenter.canChangeConfiguration) {
                savedState.add(presenter.view.parent as PresenterLayout to presenter.javaClass)
                remove(presenter)
            }
        }
        children.forEach {
            it.onConfigurationChanged(newConfig)
        }
        //Now that the whole subtree is done, update super and restore the rest
        super.onConfigurationChanged(newConfig)
        for ((layout, clazz) in savedState) {
            add(layout, clazz)
        }
    }

    override fun saveHierarchyState(container: SparseArray<Bundle>) {
        super.saveHierarchyState(container)
        presenters.forEach {
            it.saveHierarchyState(container)
        }
    }

    override fun onSaveInstanceState(out: Bundle) {
        super.onSaveInstanceState(out)
        val presenters = presenters.filter { (it.view.parent as View).id != View.NO_ID }
        out.putIntArray(CHILDREN_ID_KEY, presenters.map { (it.view.parent as View).id }.toIntArray())
        out.putStringArray(CHILDREN_CLASS_KEY, presenters.map { it.javaClass.name }.toTypedArray())
        out.putParcelableArrayList(CHILDREN_STATE_KEY, ArrayList(presenters.map {
            val state = Bundle()
            it.onSaveInstanceState(state)
            state
        }))
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
        val presenter = ctx.attach(clazz, arguments, parent.id)
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