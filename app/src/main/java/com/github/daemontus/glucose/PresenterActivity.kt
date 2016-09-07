package com.github.daemontus.glucose

import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import com.glucose.app.Presenter
import com.glucose.app.PresenterContext
import com.glucose.app.PresenterGroup
import com.glucose.app.RootCompatActivity
import com.glucose.app.presenter.Lifecycle
import com.glucose.app.presenter.findView
import com.glucose.app.presenter.isRestored
import com.glucose.app.presenter.whileIn
import com.jakewharton.rxbinding.view.clicks

class PresenterActivity : RootCompatActivity(RootPresenter::class.java)

class RootPresenter(context: PresenterContext, parent: ViewGroup?) : PresenterGroup(context, R.layout.presenter_root, parent) {

    init {
        onChildAdd.subscribe { p ->
            if (p is ControlsPresenter) {
                p.addContent.whileIn(p, Lifecycle.State.ATTACHED) {
                    subscribe {
                        this@RootPresenter.attach(R.id.content, ContentPresenter::class.java)
                    }
                }
                p.addControls.whileIn(p, Lifecycle.State.ATTACHED) {
                    subscribe {
                        this@RootPresenter.attach(R.id.content, ControlsPresenter::class.java)
                    }
                }
                p.removeAllContent.whileIn(p, Lifecycle.State.ATTACHED) {
                    subscribe {
                        for (item in presenters) {
                            this@RootPresenter.detach(item)
                        }
                    }
                }
                p.removeLast.whileIn(p, Lifecycle.State.ATTACHED) {
                    subscribe {
                        this@RootPresenter.detach(presenters.last())
                    }
                }
            }
        }
    }

    override fun onAttach(arguments: Bundle) {
        super.onAttach(arguments)
        if (!arguments.isRestored()) {
            Log.d("foo", "Adding controls!")
            attach(R.id.content, ControlsPresenter::class.java)
        }
    }
}

class ContentPresenter(context: PresenterContext, parent: ViewGroup?) : Presenter(context, R.layout.presenter_1, parent) {

}

class ControlsPresenter(context: PresenterContext, parent: ViewGroup?) : Presenter(context, R.layout.presenter_2, parent) {

    val addContent = findView<View>(R.id.add_1).clicks()
    val addControls = findView<View>(R.id.add_2).clicks()
    val removeAllContent = findView<View>(R.id.remove_all_content).clicks()
    val removeLast = findView<View>(R.id.remove_last).clicks()

    override val canChangeConfiguration: Boolean = false

}