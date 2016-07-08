package com.github.daemontus.glucose

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.github.daemontus.egholm.functional.Result
import com.glucose.Log
import com.glucose.app.*
import com.jakewharton.rxbinding.view.clicks
import rx.android.schedulers.AndroidSchedulers
import java.util.concurrent.TimeUnit

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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        transitionResults.subscribe {
            Log.d("Transition result: $it")
            if (it is Result.Ok) {
                val p = it.ok
                if (p is ControlsPresenter) {
                    p.addContent.subscribe {
                        newTransition()
                            .map { it.add(R.id.content, ContentPresenter::class.java) }
                            .enqueue()
                    }.until(p, LifecycleEvent.DETACH)
                    p.addControls.subscribe {
                        newTransition()
                                .map { it.add(R.id.content, ControlsPresenter::class.java) }
                                .enqueue()
                    }.until(p, LifecycleEvent.DETACH)
                    p.removeAllContent.subscribe {
                        newTransition()
                                .map { it.removeAll { it is ContentPresenter } }
                                .enqueue()
                    }.until(p, LifecycleEvent.DETACH)
                    p.removeLast.subscribe {
                        newTransition()
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
                                .enqueue()
                    }.until(p, LifecycleEvent.DETACH)
                }
            }
        }.bindToLifecycle()
    }

    override fun onAttach(arguments: Bundle) {
        super.onAttach(arguments)
        Log.d("Attached - starting transition")
        newTransition()
            .map { it.add(R.id.content, ControlsPresenter::class.java) }
            .enqueue()
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