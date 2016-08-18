package com.glucose.app

import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.support.annotation.IdRes
import android.util.SparseArray
import android.view.View
import java.util.*

/**
 * TODO: Decide how to handle state and IDs - If the layout has an Id, but the presenter does not, should we save it? (Probably make laoyut and presenter have same id if possible)
 * TODO: Ensure that someone is not running transitions outside of the execution context.
 * TODO: Somehow we have to make sure that if transaction fails but presenter is obtained, it is recycled
 * TODO: Give option to execute transition immediately instead of waiting for a next slot? (So that we can render something without waiting)
 * TODO: This group can't handle configuration changes (it will recreate the whole tree) - either fix this or make a better subclass
 * TODO: Split this into an internal abstract group that handles basic stuff, stateless group that can do advanced actions, but can't remember shit and stateful that works like fragment manager.
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
        for ((id, className) in childIds.zip(childClasses)) {
            val layout = findOptionalView<PresenterLayout>(id)
            if (layout != null) {
                add(layout, Class.forName(className) as Class<Presenter>)
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
        children.forEach {
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
        val presenters = this.presenters.filter {
            (it.view.parent as View).id != View.NO_ID
        }
        out.putIntArray(CHILDREN_ID_KEY, presenters.map {
            (it.view.parent as View).id
        }.toIntArray())
        out.putStringArray(CHILDREN_CLASS_KEY, presenters.map { it.javaClass.name }.toTypedArray())
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
        super.onActivityResult(requestCode, resultCode, data)
        children.forEach {
            it.performActivityResult(requestCode, resultCode, data)
        }
    }

    // ========================== Children manipulation ============================================

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
        transitionLog("Attached $presenter")
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
        transitionLog("Detached $presenter")
        ctx.detach(presenter)
    }

    // ============================ Child retrieval ================================================

    val presenters: List<Presenter>
        get() = children.toList()

}