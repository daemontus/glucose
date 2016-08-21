package com.glucose.app

import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.support.annotation.IdRes
import android.util.SparseArray
import android.view.View
import rx.subjects.PublishSubject
import rx.Observable
import java.util.*

/**
 * WARNING: Currently, if the [PresenterLayout] has an ID, but the [Presenter] inside doesn't,
 * the presenter will be restored, but with no arguments! This might be a bit confusing (-.-)
 *
 * TODO: Polish those try-catch blocks.
 * TODO: Prevent presenter leaks in case of errors where possible.
 */
open class PresenterGroup : Presenter {

    companion object {
        val CHILDREN_ID_KEY = "glucose:presenter_children_ids"
        val CHILDREN_CLASS_KEY = "glucose:presenter_children_classes"
    }

    constructor(context: PresenterContext, view: View) : super(context, view)
    constructor(context: PresenterContext, layout: Int) : super(context, layout)

    private val children = ArrayList<Presenter>()

    // ============================ Children and Lifecycle =============================

    override fun onAttach(arguments: Bundle) {
        super.onAttach(arguments)
        val childIds = arguments.getIntArray(CHILDREN_ID_KEY)
        val childClasses = arguments.getStringArray(CHILDREN_CLASS_KEY)
        if (childIds != null && childClasses != null) {
            for ((id, className) in childIds.zip(childClasses)) {
                val layout = findOptionalView<PresenterLayout>(id)
                if (layout != null) {
                    add(layout, Class.forName(className) as Class<Presenter>)
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
        val parentViews = presenters.map { it.view.parent as View }
        val presenters = parentViews.filter { it.id != View.NO_ID }
        out.putIntArray(CHILDREN_ID_KEY, presenters.map { it.id }.toIntArray())
        out.putStringArray(CHILDREN_CLASS_KEY, presenters.map { it.javaClass.name }.toTypedArray())
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
        super.onActivityResult(requestCode, resultCode, data)
        children.forEach {
            it.performActivityResult(requestCode, resultCode, data)
        }
    }

    // ========================== Children manipulation ============================================

    private val childAdded: PublishSubject<Presenter> = PublishSubject.create()
    private val childRemoved: PublishSubject<Class<out Presenter>> = PublishSubject.create()

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
        try {
            parent.addView(presenter.view)
            children.add(presenter)
            if (isStarted) presenter.performStart()
            if (isResumed) presenter.performResume()
            transitionLog("Attached $presenter")
            childAdded.onNext(presenter)
            return presenter
        } catch (e: Exception) {
            if (isResumed) presenter.performPause()
            if (isStarted) presenter.performStop()
            parent.removeView(presenter.view)
            ctx.detach(presenter)
            throw e
        }
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
        transitionLog("Detached $presenter")
        childRemoved.onNext(presenter.javaClass)
        ctx.detach(presenter)
    }

    // ============================ Child retrieval ================================================

    val presenters: List<Presenter>
        get() = children.toList()

}