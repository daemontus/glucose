package com.glucose.app

import android.os.Bundle
import android.os.Parcel
import android.os.Parcelable
import android.support.annotation.IdRes
import android.support.annotation.LayoutRes
import android.support.v7.appcompat.R
import android.view.View
import android.view.ViewGroup
import com.github.daemontus.egholm.functional.Result
import com.github.daemontus.egholm.functional.Result.Ok
import com.github.daemontus.egholm.functional.error
import com.glucose.Log
import rx.Observable
import java.util.*

//TODO: More strict BackStack contract - What about other layouts? What happens if I do my own modifications? Etc. Etc.
//TODO: We need a notification about what is currently on top of the stack
//TODO: It would be best if we could gently move the presenters somewhere away (pause/stop them)
open class BackStack(
        context: PresenterContext,
        @LayoutRes layout: Int,
        @IdRes private val container: Int,
        private val root: BackStackEntry
) : PresenterGroup(context, layout) {

    private val liveStack = ArrayList<Presenter>()

    //Note: Live stack will be repopulated from state info. Hurray!

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
        if (liveStack.isEmpty()) {
            push(root.clazz, root.arguments)
        }
    }

    override fun onDetach() {
        super.onDetach()
        Log.d("Detaching back stack")
    }

    override fun onSaveInstanceState(out: Bundle) {
        super.onSaveInstanceState(out)
        Log.d("Saving state! ${out.keySet().toSet()}")
    }

    override fun onBackPressed(): Boolean {
        return pop() || super.onBackPressed()
    }

    fun push(clazz: Class<out Presenter>, arguments: Bundle = Bundle()) {
        Observable.fromCallable {
            add(container, clazz, arguments)
        }.postAction(this).asResult().subscribe {
            Log.d("Push result: $it")
        }
    }

    fun pop(): Boolean {
        if (liveStack.size < 2) return false
        Observable.fromCallable {
            if (liveStack.size > 1) {
                //if there is no such entry, somebody has been tempering with the back stack
                //and the transition will fail
                val victim = liveStack.last()
                liveStack.remove(victim)
                remove(victim)
                Log.d("Popping ${victim.javaClass}")
            }
        }.postAction(this).asResult().subscribe { Log.d("Popped: $it") }
        return true
    }

}

class BackStackEntry(
        val clazz: Class<out Presenter>,
        val arguments: Bundle = Bundle()
) : Parcelable {
    override fun writeToParcel(p0: Parcel, p1: Int) {
        p0.writeString(clazz.name)
        p0.writeBundle(arguments)
    }

    override fun describeContents(): Int = 0

    companion object {
        @JvmField val CREATOR = object : Parcelable.Creator<BackStackEntry> {
            override fun createFromParcel(p0: Parcel): BackStackEntry = BackStackEntry(
                    Class.forName(p0.readString()) as Class<out Presenter>, p0.readBundle()
            )
            override fun newArray(p0: Int): Array<out BackStackEntry?> = kotlin.arrayOfNulls(p0)
        }
    }
}
