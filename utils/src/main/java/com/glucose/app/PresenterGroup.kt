package com.glucose.app

import android.os.Bundle
import android.support.annotation.IdRes
import android.view.View
import android.view.ViewGroup
import com.github.daemontus.egholm.functional.Result
import com.glucose.Log
import rx.Observable
import rx.Single
import rx.Subscription
import rx.android.schedulers.AndroidSchedulers
import rx.subjects.PublishSubject
import java.util.*

fun <T, E> T.asOk(): Result<T, E> = Result.Ok<T, E>(this)
fun <T, E> E.asError(): Result<T, E> = Result.Error<T, E>(this)

/**
 * TODO: Should we provide a transition id? Or some other form of identification?
 * TODO: It would be nice to get an error about transitions that were dropped due to being detached
 * TODO: Ensure that someone is not running transitions outside of the execution context.
 */
open class PresenterGroup<out Ctx: PresenterContext>(view: View, context: PresenterContext) : Presenter<Ctx>(view, context) {

    private val children = ArrayList<Presenter<*>>()

    private val transitionSubject = PublishSubject.create<Observable<Result<Any, Throwable>>>()

    private val transitionResultSubject = PublishSubject.create<Result<Any, Throwable>>()
    val transitionResults: Observable<Result<Any, Throwable>> = transitionResultSubject

    private var transitionSubscription: Subscription? = null

    // ============================ Children and transaction Lifecycle =============================

    override fun onAttach(arguments: Bundle) {
        super.onAttach(arguments)
        //Start processing transactions
        transitionSubscription = Observable.concatDelayError(transitionSubject.onBackpressureDrop {
            Log.w("Transaction dropped due to back pressure")
            transitionResultSubject.onNext(IllegalStateException("Transaction dropped due to back pressure").asError())
        }).subscribe({
            when (it) {
                is Result.Ok -> Log.d("Transaction finished successfully: ${it.ok}")
                is Result.Error -> Log.w("Transaction finished with an error: ${it.error}")
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
        super.onPause()
        children.forEach { it.performPause() }
    }

    override fun onStop() {
        super.onStop()
        children.forEach { it.performStop() }
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

    /**
     * Create a new transition. The transition is not queued yet and doesn't have to be executed.
     * It just provides a way of exporting a context that can safely edit the insides of
     * the PresenterGroup.
     */
    fun newTransition(): Single<out BasicTransition> {
        transitionLog("New transition")
        return Single.just(BasicTransition()).doOnSuccess {
            transitionLog("Start transition")
        }.observeOn(AndroidSchedulers.mainThread())
    }

    /**
     * Enqueue a transition to be executed. The transition may be a composition of several
     * Singles composed into one Observable.
     *
     * Returns an observable that will emit results of all composed transitions in
     * order in which they finish (as if subscribed to the transition Observable).
     *
     * Note that depending on implementation, this emission might happen after or during
     * next transition. Therefore you can't make definitive assumptions about the
     * state of the world.
     */
    fun <R: Any> enqueueTransition(transition: Observable<TransitionResult<R>>): Observable<R> {
        transitionLog("Commit transition")
        val result = PublishSubject.create<R>()
        val t = transition
                .doOnSubscribe { transitionLog("End transition") }
                .map { it.result }
                .doOnEach(result)
                .map { it.asOk<Any, Throwable>() }
                .onErrorReturn { it.asError() }
        transitionSubject.onNext(t)
        //The subject is needed to ensure that subscription to the returned observable won't
        //execute the transaction itself.
        //Cache ensures that the user will get results even if the transaction executes before he subscribes.
        return result.cache()
    }

    /**
     * Enqueue a transition to be executed.
     */
    fun <R: Any> enqueueTransition(transition: Single<TransitionResult<R>>): Single<R> {
        return enqueueTransition(transition.toObservable()).toSingle()
    }

    /*
        Helper functions for working with transactions.
     */
    fun <R: Any> Observable<TransitionResult<R>>.enqueue(): Observable<R> = enqueueTransition(this)
    fun <R: Any> Single<TransitionResult<R>>.enqueue(): Single<R> = enqueueTransition(this)

    inner open class BasicTransition : Transition() {

        /**
         * Obtain new presenter and attach it to a ViewGroup with given id.
         */
        fun <P: Presenter<*>> add(
                @IdRes id: Int, clazz: Class<P>, arguments: Bundle = Bundle()
        ): TransitionResult<P> = add(findView<ViewGroup>(id), clazz, arguments)

        /**
         * Obtain new presenter and attach it to a ViewGroup.
         */
        fun <P: Presenter<*>> add(
                parent: ViewGroup, clazz: Class<P>, arguments: Bundle = Bundle()
        ): TransitionResult<P> = attach(parent, ctx.obtain(clazz, parent), arguments)

        /**
         * Attach a presenter to a ViewGroup.
         *
         * Warning: It is recommended to use add/remove instead of attach/detach when possible
         * in order to avoid leaked Presenters.
         */
        fun <P: Presenter<*>> attach(
                parent: ViewGroup, presenter: P, arguments: Bundle = Bundle()
        ): TransitionResult<P> {
            if (!presenter.isAlive) throw IllegalStateException("Adding presenter that hasn't been created properly")
            if (presenter.isAttached) throw IllegalStateException("Adding presenter that is already attached")
            children.add(presenter)
            presenter.performAttach(arguments)
            parent.addView(presenter.view)
            if (isStarted) presenter.performStart()
            if (isResumed) presenter.performResume()
            transitionLog("Attached $presenter")
            return presenter.asResult()
        }

        /**
         * Detach and return a presenter from a ViewGroup.
         *
         * Warning: It is recommended to use add/remove instead of attach/detach when possible
         * in order to avoid leaked Presenters.
         */
        fun <P: Presenter<*>> detach(presenter: P): TransitionResult<P> {
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
            return presenter.asResult()
        }

        /**
         * Detach all presenters that satisfy a specific predicate.
         */
        fun detachAll(predicate: (Presenter<*>) -> Boolean): TransitionResult<List<Presenter<*>>> {
            val victims = children.filter(predicate)
            victims.forEach { detach(it) }
            return victims.asResult()
        }

        /**
         * Detach and recycle a presenter.
         */
        fun remove(presenter: Presenter<*>): TransitionResult<Unit> {
            return ctx.recycle(detach(presenter).result).asResult()
        }

        /**
         * Detach and recycle all presenters that satisfy a specific predicate.
         *
         * Transition result contains number of recycled presenters.
         */
        fun removeAll(predicate: (Presenter<*>) -> Boolean): TransitionResult<Int> {
            return detachAll(predicate).result.map { ctx.recycle(it) }.count().asResult()
        }

        /**
         * Detach and recycle all presenters from a specific ViewGroup.
         *
         * Transition result contains number of recycled presenters.
         */
        fun removeAll(parent: ViewGroup): TransitionResult<Int> {
            return removeAll { it.view.parent == parent }
        }

        /**
         * Detach and recycle all presenters with a specific Class.
         *
         * Transition result contains number of recycled presenters.
         */
        fun removeAll(clazz: Class<*>): TransitionResult<Int> {
            return removeAll { it.javaClass == clazz }
        }

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