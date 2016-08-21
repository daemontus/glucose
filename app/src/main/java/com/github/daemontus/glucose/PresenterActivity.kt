package com.github.daemontus.glucose

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.github.daemontus.egholm.functional.Result
import com.glucose.Log
import com.glucose.app.*
import com.jakewharton.rxbinding.view.clicks

class PresenterActivity : RootActivity(RootPresenter::class.java)

class RootPresenter(context: PresenterContext) : BackStack(context, R.layout.presenter_root, R.id.content, BackStackEntry(ControlsPresenter::class.java)) {

    init {
        onChildAdd.subscribe { p ->
            if (p is ControlsPresenter) {
                p.addContent.subscribe {
                    this.push(ContentPresenter::class.java)
                    /*newTransition()
                        .map { it.add(R.id.content, ContentPresenter::class.java) }
                        .enqueue()*/
                }.until(p, LifecycleEvent.DETACH)
                p.addControls.subscribe {
                    this.push(ControlsPresenter::class.java)
                    /*newTransition()
                            .map { it.add(R.id.content, ControlsPresenter::class.java) }
                            .enqueue()*/
                }.until(p, LifecycleEvent.DETACH)
                p.removeAllContent.subscribe {
                    /*newTransition()
                            .map { it.removeAll { it is ContentPresenter } }
                            .enqueue()*/
                }.until(p, LifecycleEvent.DETACH)
                p.removeLast.subscribe {
                    this.pop()
                    /*
                    //This is an example of a complex transition
                    //First transition has extended duration to accommodate for the animation.
                    //Both transition are executed in parallel, but any other transition
                    //will wait until both are finished.
                    val t1 = newTransition()
                            .map { it to presenters.last() }
                            .doOnSuccess {
                                it.second.view.animate().scaleX(0f).scaleY(0f).duration = 1000
                            }
                            .delay(1000, TimeUnit.MILLISECONDS)
                            .observeOn(AndroidSchedulers.mainThread())
                            .map {
                                it.second.view.scaleX = 1f
                                it.second.view.scaleY = 1f
                                it.first.detach(it.second)
                            }
                    val t2 = newTransition()
                            .map { it.add(R.id.content, ControlsPresenter::class.java) }
                    enqueueTransition(Observable.concatEager(t1.toObservable(), t2.toObservable()))*/
                }.until(p, LifecycleEvent.DETACH)
            }
        }
    }

    override fun onAttach(arguments: Bundle) {
        super.onAttach(arguments)
        /*Log.d("Attached - starting transition")
        newTransition()
            .map { it.add(R.id.content, ControlsPresenter::class.java) }
            .enqueue()*/
    }
}

class ContentPresenter(context: PresenterContext) : Presenter(context, R.layout.presenter_1) {

}

class ControlsPresenter(context: PresenterContext) : Presenter(context, R.layout.presenter_2) {

    val addContent = findView<View>(R.id.add_1).clicks()
    val addControls = findView<View>(R.id.add_2).clicks()
    val removeAllContent = findView<View>(R.id.remove_all_content).clicks()
    val removeLast = findView<View>(R.id.remove_last).clicks()

}