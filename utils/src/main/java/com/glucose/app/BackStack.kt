package com.glucose.app

import android.os.Bundle
import android.support.annotation.IdRes
import android.support.annotation.LayoutRes
import android.view.View
import com.glucose.Log
import rx.Observable
import java.util.*

/**
 * BackStack assumes that no one else is changing the contents of
 * [container]. Otherwise, the contract is broken and the behavior is undefined.
 * (It shouldn't crash, but it can get stuck, etc.)
 *
 * TODO: Make a version of stack that can stop/detach presenters while they are not visible, saving resources.
 */
open class BackStack(
        context: PresenterContext,
        @LayoutRes layout: Int,
        @IdRes private val container: Int,
        private val bottom: Class<out Presenter>,
        private val bottomArguments: Bundle = Bundle()
) : PresenterGroup(context, layout) {

    //Note: Live stack will be repopulated from state info. Hurray!

    private val liveStack = ArrayList<Presenter>()

    /**
     * An unmodifiable view of the Presenter stack managed by this group.
     */
    val stack: List<Presenter> = Collections.unmodifiableList(liveStack)

    init {
        onChildAdd.subscribe {
            if ((it.view.parent as View).id == container) {
                Log.d("Add to live stack: $it")
                liveStack.add(it)
            }
        }
    }

    override fun onAttach(arguments: Bundle) {
        super.onAttach(arguments)
        if (liveStack.isEmpty()) {  //add bottom element if empty
            push(bottom, bottomArguments)
        }
    }

    override fun onBackPressed(): Boolean {
        return pop() || super.onBackPressed()
    }

    fun <R: Presenter> push(clazz: Class<R>, arguments: Bundle = Bundle()): Observable<R> {
        return Observable.fromCallable {
            Log.d("Pushing: $clazz")
            add(container, clazz, arguments)
        }.postActionImmediate(this)
    }

    fun pop(): Boolean {
        if (liveStack.size < 2) return false
        Observable.fromCallable {
            if (liveStack.size > 1) {
                //if there is no such entry, somebody has been tempering with the back stack
                //and the transition will fail
                val victim = liveStack.last()
                Log.d("Popping ${victim.javaClass}")
                liveStack.removeAt(liveStack.size - 1)
                remove(victim)
            }
        }.postActionImmediate(this)
        return true
    }

}