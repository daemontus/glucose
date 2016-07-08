package com.github.daemontus.glucose

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.github.daemontus.egholm.functional.Result
import com.glucose.Log
import com.glucose.app.*
import com.jakewharton.rxbinding.view.clicks

class PresenterActivity : RootActivity() {

    init {
        register(RootPresenter::class.java, ::RootPresenter)
        register(ContentPresenter::class.java, ::ContentPresenter)
        register(ControlsPresenter::class.java, ::ControlsPresenter)
    }

    override val rootPresenter: Class<out Presenter<*>> = RootPresenter::class.java

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }
}

class RootPresenter(context: PresenterContext, parent: ViewGroup?) : PresenterGroup<PresenterContext>(
        LayoutInflater.from(context.activity).inflate(R.layout.presenter_root, parent, false), context) {

    private val backStack = BackStack(BackStackEntry(ControlsPresenter::class.java), findView(R.id.content), this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        transitionResults.subscribe {
            Log.d("Transition result: $it")
            if (it is Result.Ok) {
                val p = it.ok
                if (p is ControlsPresenter) {
                    p.addContent.subscribe {
                        backStack.push(ContentPresenter::class.java)
                        /*newTransition()
                            .map { it.add(R.id.content, ContentPresenter::class.java) }
                            .enqueue()*/
                    }.until(p, LifecycleEvent.DETACH)
                    p.addControls.subscribe {
                        backStack.push(ControlsPresenter::class.java)
                        /*newTransition()
                                .map { it.add(R.id.content, ControlsPresenter::class.java) }
                                .enqueue()*/
                    }.until(p, LifecycleEvent.DETACH)
                    p.removeAllContent.subscribe {
                        newTransition()
                                .map { it.removeAll { it is ContentPresenter } }
                                .enqueue()
                    }.until(p, LifecycleEvent.DETACH)
                    p.removeLast.subscribe {
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
        }.bindToLifecycle()
    }

    override fun onBackPressed(): Boolean {
        return backStack.pop() || super.onBackPressed()
    }

    override fun onAttach(arguments: Bundle) {
        super.onAttach(arguments)
        /*Log.d("Attached - starting transition")
        newTransition()
            .map { it.add(R.id.content, ControlsPresenter::class.java) }
            .enqueue()*/
    }
}

class ContentPresenter(view: View, context: PresenterContext) : Presenter<PresenterContext>(view, context) {

    constructor(context: PresenterContext, parent: ViewGroup?) : this(
            LayoutInflater.from(context.activity).inflate(R.layout.presenter_1, parent, false), context)

}

class ControlsPresenter(view: View, context: PresenterContext) : Presenter<PresenterContext>(view, context) {

    constructor(context: PresenterContext, parent: ViewGroup?) : this(
            LayoutInflater.from(context.activity).inflate(R.layout.presenter_2, parent, false), context)

    val addContent = findView<View>(R.id.add_1).clicks()
    val addControls = findView<View>(R.id.add_2).clicks()
    val removeAllContent = findView<View>(R.id.remove_all_content).clicks()
    val removeLast = findView<View>(R.id.remove_last).clicks()

}