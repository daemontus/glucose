package com.glucose.app

import android.os.Bundle
import android.os.Parcel
import android.os.Parcelable
import android.support.annotation.IdRes
import android.support.annotation.LayoutRes
import android.view.View
import android.view.ViewGroup
import com.github.daemontus.egholm.functional.Result
import com.github.daemontus.egholm.functional.Result.Ok
import com.github.daemontus.egholm.functional.error
import com.glucose.Log
import rx.Observable
import java.util.*

//TODO: We need a notification about what is currently on top of the stack
//TODO: It would be best if we could gently move the presenters somewhere away (pause/stop them)
open class BackStack(
        context: PresenterContext,
        @LayoutRes layout: Int,
        @IdRes private val container: Int,
        private val root: BackStackEntry
) : PresenterGroup(context, layout) {

    private val backStack by InstanceState<ArrayList<BackStackEntry>>(
            { key ->
                this.getParcelableArrayList(key) ?: (ArrayList<BackStackEntry>().apply {
                    this@InstanceState.putParcelableArrayList(key, this)    //if we are creating it, we have to put it there!
                })
            }, Bundle::putParcelableArrayList)

    private val liveStack = ArrayList<Presenter>()

    override fun onAttach(arguments: Bundle) {
        Log.d("Attaching BackStack")
        super.onAttach(arguments)   //restore state
        if (backStack.isEmpty()) {
            Log.d("Stack is empty, add ")
            backStack.add(root)
        }
        Observable.from(backStack.map { entry ->
            Observable.fromCallable {
                Log.d("Adding: ${entry.clazz}")
                add(container, entry.clazz, entry.arguments)
            }
        }).onBackpressureBuffer(backStack.size+1L).concatMap { it.postAction(this) }.asResult()
                .subscribe {
                    if (it is Result.Ok) {
                        liveStack.add(it.ok)
                    }
                    Log.d("State restore: $it")
        }
    }

    override fun onBackPressed(): Boolean {
        return pop() || super.onBackPressed()
    }

    fun push(clazz: Class<out Presenter>, arguments: Bundle = Bundle()) {
        Observable.fromCallable {
            add(container, clazz, arguments)
        }.postAction(this).asResult().subscribe {
            Log.d("Push result: $it")
            when (it) {
                is Ok -> {
                    Log.d("Pushed: $it")
                    backStack.add(BackStackEntry(clazz, arguments))
                    liveStack.add(it.ok)
                }
                is Error -> Log.e("Problem pushing: $it")
            }
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
                val entry = backStack.last()
                backStack.remove(entry)
                remove(victim)
                Log.d("Popping ${victim.javaClass}")
                if (entry.clazz != victim.javaClass) {
                    throw IllegalStateException("Back stack corrupted: $backStack vs. $liveStack")
                }
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
