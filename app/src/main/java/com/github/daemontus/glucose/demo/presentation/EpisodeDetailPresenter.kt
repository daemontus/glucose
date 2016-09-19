package com.github.daemontus.glucose.demo.presentation

import android.os.Bundle
import android.view.ViewGroup
import android.widget.TextView
import com.github.daemontus.glucose.demo.R
import com.glucose.app.Presenter
import com.glucose.app.PresenterDelegate
import com.glucose.app.PresenterGroup
import com.glucose.app.presenter.Argument
import com.glucose.app.presenter.findView
import com.glucose.app.presenter.isFresh
import com.glucose.app.presenter.stringBundler

class EpisodeDetailPresenter(context: PresenterDelegate, parent: ViewGroup?)
    : PresenterGroup(context, R.layout.presenter_episode_detail, parent) {

    val episodeName by Argument(stringBundler)

    override fun onAttach(arguments: Bundle) {
        super.onAttach(arguments)
        findView<TextView>(R.id.episode_title).text = episodeName
        if (arguments.isFresh()) {
            attach(R.id.episode_data, PersistentPresenter::class.java)
            attach(R.id.episode_data, RecreatedPresenter::class.java)
            attach(R.id.episode_data, NoRecyclePresenter::class.java)
        }
    }
}

class PersistentPresenter(context: PresenterDelegate, parent: ViewGroup?) : Presenter(context, R.layout.presenter_data, parent) {

    override val canChangeConfiguration: Boolean = true

    init {
        findView<TextView>(R.id.data_title).text = "canChangeConfiguration = true"
    }
}

class RecreatedPresenter(context: PresenterDelegate, parent: ViewGroup?) : Presenter(context, R.layout.presenter_data, parent) {

    override val canChangeConfiguration: Boolean = false

    init {
        findView<TextView>(R.id.data_title).text = "canChangeConfiguration = false"
    }
}

class NoRecyclePresenter(context: PresenterDelegate, parent: ViewGroup?) : Presenter(context, R.layout.presenter_data, parent) {

    override val canBeReused: Boolean = false

    init {
        findView<TextView>(R.id.data_title).text = "canBeReused = false"
    }
}