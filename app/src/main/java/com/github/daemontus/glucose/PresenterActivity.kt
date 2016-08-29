package com.github.daemontus.glucose

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.github.daemontus.egholm.functional.Result
import com.glucose.Log
import com.glucose.app.*
import com.glucose.app.presenter.*
import com.jakewharton.rxbinding.view.clicks

class PresenterActivity : RootActivity(RootPresenter::class.java)

class RootPresenter(context: PresenterContext) : PresenterGroup(context, R.layout.presenter_root) {

    init {
        onChildAdd.subscribe { p ->
            if (p is ControlsPresenter) {
                p.addContent.whileIn(p, Lifecycle.State.ATTACHED) {
                    subscribe {
                        this@RootPresenter.add(R.id.content, ContentPresenter::class.java)
                    }
                }
                p.addControls.whileIn(p, Lifecycle.State.ATTACHED) {
                    subscribe {
                        this@RootPresenter.add(R.id.content, ControlsPresenter::class.java)
                    }
                }
                p.removeAllContent.whileIn(p, Lifecycle.State.ATTACHED) {
                    subscribe {
                        for (p in presenters) {
                            this@RootPresenter.remove(p)
                        }
                    }
                }
                p.removeLast.whileIn(p, Lifecycle.State.ATTACHED) {
                    subscribe {
                        this@RootPresenter.remove(presenters.last())
                    }
                }
            }
        }
    }

    override fun onAttach(arguments: Bundle, isFresh: Boolean) {
        super.onAttach(arguments, isFresh)
        if (isFresh) {
            add(R.id.content, ControlsPresenter::class.java)
        }
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