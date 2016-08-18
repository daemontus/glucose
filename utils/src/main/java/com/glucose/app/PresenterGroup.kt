package com.glucose.app

import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.support.annotation.IdRes
import android.view.View
import android.view.ViewGroup
import com.github.daemontus.egholm.functional.Result
import com.glucose.Log
import rx.Observable
import rx.Subscription
import rx.subjects.PublishSubject
import java.util.*

/**
 * Transition is created by calling one of the modification methods
 * on the group. This will return observable of transition results.
 *
 * The transition can be either executed immediately by subscribing to
 * said observable, or enqueued for execution using the commit method
 * (possibly after combining with other transitions/side effects).
 *
 * To set up a specific transition, use the observable returned by the
 * action method. To set up the presenter which is the result of such
 * transition, use the global transitionResults observable.
 * This observable will also return information about presenters that were
 * restored from state.
 *
 * TODO: Should we provide a transition id? Or some other form of identification?
 * TODO: It would be nice to get an error about transitions that were dropped due to being detached
 * TODO: Ensure that someone is not running transitions outside of the execution context.
 * TODO: Consider adding more info in transition result - so that we know if something was added, moved, etc.
 * TODO: Somehow we have to make sure that if transaction fails but presenter is obtained, it is recycled
 * TODO: Give option to execute transition immediately instead of waiting for a next slot? (So that we can render something without waiting)
 * TODO: We have to manually restore view state if the presenter is added after activity is shown.
 * TODO: This group can't handle configuration changes (it will recreate the whole tree) - either fix this or make a better subclass
 * TODO: Split this into an internal abstract group that handles basic stuff, stateless group that can do advanced actions, but can't remember shit and stateful that works like fragment manager.
 */
open class PresenterGroup<out Ctx: PresenterContext>(view: View, context: PresenterContext) : Presenter<Ctx>(view, context) {

    private val children = ArrayList<Presenter<*>>()

    private val transitionSubject = PublishSubject.create<Observable<Result<Any, Throwable>>>()

    private val transitionResultSubject = PublishSubject.create<Result<Any, Throwable>>()
    val transitionResults: Observable<Result<Any, Throwable>> = transitionResultSubject

    private var transitionSubscription: Subscription? = null

    // ============================ Children and Lifecycle =============================

    override fun onAttach(arguments: Bundle) {
        super.onAttach(arguments)
        //Start processing transactions
        transitionSubscription = Observable.concatDelayError(transitionSubject.onBackpressureDrop {
            Log.w("Transaction dropped due to back pressure")
            transitionResultSubject.onNext(IllegalStateException("Transaction dropped due to back pressure").asError())
        }).doOnEach(transitionResultSubject).subscribe({
            when (it) {
                is Result.Ok -> Log.d("Transaction finished successfully: ${it.ok}")
                is Result.Error -> Log.e("Transaction finished with an error: ${it.error}")
            }
        }, {
            Log.e("Something went horribly wrong in the transaction mechanism!", it)
        })
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
        transitionSubscription?.unsubscribe()
        transitionSubscription = null
        children.forEach {
            val parent = it.view.parent as ViewGroup
            parent.removeView(it.view)
            it.performDetach()
            ctx.recycle(it)
        }
        children.clear()
        super.onDetach()
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        children.forEach {
            it.performConfigurationChange(newConfig)
        }
    }

    override val canChangeConfiguration: Boolean = children.fold(true) { a, b -> a && b.canChangeConfiguration }

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
    fun <P: Presenter<*>> add(
            @IdRes id: Int, clazz: Class<P>, arguments: Bundle = Bundle()
    ): P = add(findView<ViewGroup>(id), clazz, arguments)

    /**
     * Obtain new presenter and attach it to a ViewGroup.
     */
    fun <P: Presenter<*>> add(
            parent: ViewGroup, clazz: Class<P>, arguments: Bundle = Bundle()
    ): P = attach(parent, ctx.obtain(clazz, parent), arguments)

    /**
     * Attach a presenter to a ViewGroup.
     *
     * Warning: It is recommended to use add/remove instead of attach/detach when possible
     * in order to avoid leaked Presenters.
     */
    fun <P: Presenter<*>> attach(
            parent: ViewGroup, presenter: P, arguments: Bundle = Bundle()
    ): P {
        if (!presenter.isAlive) throw IllegalStateException("Adding presenter that hasn't been created properly")
        if (presenter.isAttached) throw IllegalStateException("Adding presenter that is already attached")
        children.add(presenter)
        presenter.performAttach(arguments)
        parent.addView(presenter.view)
        if (isStarted) presenter.performStart()
        if (isResumed) presenter.performResume()
        transitionLog("Attached $presenter")
        return presenter
    }

    /**
     * Detach and return a presenter from a ViewGroup.
     *
     * Warning: It is recommended to use add/remove instead of attach/detach when possible
     * in order to avoid leaked Presenters.
     */
    fun <P: Presenter<*>> detach(presenter: P): P {
        if (!presenter.isAlive) throw IllegalStateException("Removing presenter that hasn't been created properly")
        if (!presenter.isAttached) throw IllegalStateException("Removing presenter that hasn't been attached properly")
        if (presenter !in children) throw IllegalStateException("Removing presenter that isn't attached to ${this@PresenterGroup}")
        if (isResumed) presenter.performPause()
        if (isStarted) presenter.performStop()
        val parent = presenter.view.parent as ViewGroup
        parent.removeView(presenter.view)
        presenter.performDetach()
        children.remove(presenter)
        transitionLog("Detached $presenter")
        return presenter
    }

    /**
     * Detach all presenters that satisfy a specific predicate.
     * TODO: This is not good, if would be much better if this could somehow be an observable
     */
    fun detachAll(predicate: (Presenter<*>) -> Boolean): List<Presenter<*>> {
        val victims = children.filter(predicate)
        victims.forEach { detach(it) }
        return victims
    }

    /**
     * Detach and recycle a presenter.
     */
    fun remove(presenter: Presenter<*>): Unit {
        return ctx.recycle(detach(presenter))
    }

    /**
     * Detach and recycle all presenters that satisfy a specific predicate.
     *
     * Transition result contains number of recycled presenters.
     */
    fun removeAll(predicate: (Presenter<*>) -> Boolean): Int {
        return detachAll(predicate).map { ctx.recycle(it) }.count()
    }

    /**
     * Detach and recycle all presenters from a specific ViewGroup.
     *
     * Transition result contains number of recycled presenters.
     */
    fun removeAll(parent: ViewGroup): Int {
        return removeAll { it.view.parent == parent }
    }

    /**
     * Detach and recycle all presenters with a specific Class.
     *
     * Transition result contains number of recycled presenters.
     */
    fun removeAll(clazz: Class<*>): Int {
        return removeAll { it.javaClass == clazz }
    }

    // ============================ Child retrieval ================================================

    val presenters: List<Presenter<*>>
        get() = children.toList()

    fun <P: Presenter<*>> findPresentersByClass(clazz: Class<P>): List<P> {
        return presenters.filter { it.javaClass == clazz }.map {
            @Suppress("UNCHECKED_CAST") //Safe due to filter
            (it as P)
        }
    }

    fun <P: Presenter<*>> findPresenterByClass(clazz: Class<P>): P {
        val r = findPresentersByClass(clazz)
        if (r.size == 0) throw IllegalStateException("So presenter for $clazz")
        if (r.size > 1) throw IllegalStateException("More then one presenter for $clazz")
        return r.first()
    }


}