package com.github.daemontus.glucose.demo.presentation

import android.os.Bundle
import android.util.Log
import android.view.ViewGroup
import com.github.daemontus.glucose.demo.R
import com.github.daemontus.glucose.demo.presentation.util.MultiStateButton
import com.glucose.app.PresenterContext
import com.glucose.app.PresenterGroup
import com.glucose.app.presenter.findView
import com.glucose.app.presenter.isFresh
import com.glucose.app.presenter.whileAlive
import rx.Observable
import rx.android.schedulers.AndroidSchedulers
import timber.log.Timber


class RootPresenter(context: PresenterContext, parent: ViewGroup?) : PresenterGroup(context, R.layout.presenter_root, parent) {

    val startButton = findView<MultiStateButton>(R.id.start_action_button)
    val endButton = findView<MultiStateButton>(R.id.end_action_button)

    init {
        Observable.merge(this.onChildAdd, this.onChildRemove)
                .observeOn(AndroidSchedulers.mainThread())
                .whileAlive(this) { subscribe {
                    startButton.state = if (presenters.size <= 1) null else startButton.backButtonState
                } }
    }

    override fun onAttach(arguments: Bundle) {
        super.onAttach(arguments)
        if (arguments.isFresh()) {
            attach(R.id.root_container, ShowListPresenter::class.java)
        }
    }


}