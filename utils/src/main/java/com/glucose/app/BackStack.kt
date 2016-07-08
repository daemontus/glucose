package com.glucose.app

import android.os.Bundle
import android.os.Parcel
import android.os.Parcelable
import android.view.ViewGroup
import com.glucose.Log
import rx.android.schedulers.AndroidSchedulers
import java.util.*
import rx.Observable

//TODO: We need a notification about what is currently on top of the stack
//TODO: It would be best if we could gently move the presenters somewhere away (pause/stop them)
open class BackStack(
        private val bottom: BackStackEntry, private val container: ViewGroup,
        private val group: PresenterGroup<*>
) : InstanceState(group) {

    private val backStack = ArrayList<BackStackEntry>()

    init {
        //PresenterGroup will recursively detach all children when detached.
        //Therefore we have to restore the backStack upon every attachment.
        /*val repopulateStack = {
            //It has to be a single transition because:
            //a) To guarantee ordering (what if user pushes some buttons?!)
            //b) Back pressure will drop parts of the back stack if it is too big
            group.enqueueTransition(Observable.from(
                    backStack.map { entry ->
                        group.newTransition()
                                .map { it.add(container, entry.clazz, entry.arguments) }
                    }
            ).onBackpressureBuffer(backStack.size+1L).concatMap { it.toObservable() })
        }
        group.lifecycleEvents.doOnSubscribe {
            //we are not getting a notification then...
            if (group.isAttached) repopulateStack()
        }.filter {
            it == LifecycleEvent.ATTACH
        }
                .subscribeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    repopulateStack()
                }*/
    }

    override fun onCreate(instanceState: Bundle?) {
        backStack.addAll(instanceState?.getParcelableArrayList<BackStackEntry>(key) ?: listOf(bottom))
        Log.d("Back stack after create: $backStack")
    }

    override fun onSaveInstanceState(output: Bundle) {
        output.putParcelableArrayList(key, backStack)
    }

    fun onAttach() {
        group.enqueueTransition(Observable.from(
                backStack.map { entry ->
                    group.newTransition()
                            .map { it.add(container, entry.clazz, entry.arguments) }
                }
        ).onBackpressureBuffer(backStack.size+1L).concatMap { it.toObservable() })
    }

    fun push(clazz: Class<out Presenter<*>>, arguments: Bundle = Bundle()) {
        group.enqueueTransition(
                group.newTransition()
                .map { it.add(container, clazz, arguments) }
        ).subscribe {
            Log.d("Pushing $clazz")
            backStack.add(BackStackEntry(clazz, arguments))
        }
    }

    fun canGoBack(): Boolean = backStack.size > 1

    fun pop(): Boolean {
        if (backStack.size < 2) return false
        group.enqueueTransition(group.newTransition()
                .toObservable()
                //ensure that at the time of the transaction, the stack is still not empty
                //this will "erase" the transaction, so that it won't be reported as an error
                .filter { backStack.size >= 2 }
                .map {
                    val victimEntry = backStack.last()
                    backStack.remove(victimEntry)
                    //if there is no such entry, somebody has been tempering with the back stack
                    //and the transition will fail
                    val victim = group.presenters.last {
                        it.javaClass == victimEntry.clazz && it.view.parent == container
                    }
                    Log.d("Popping ${victimEntry.clazz}")
                    it.remove(victim)
                }
        )
        return true
    }

}

class BackStackEntry(
        val clazz: Class<out Presenter<*>>,
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
                    Class.forName(p0.readString()) as Class<out Presenter<*>>, p0.readBundle()
            )
            override fun newArray(p0: Int): Array<out BackStackEntry?> = kotlin.arrayOfNulls(p0)
        }
    }
}
