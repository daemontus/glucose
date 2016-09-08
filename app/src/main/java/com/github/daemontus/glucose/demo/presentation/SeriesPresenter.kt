package com.github.daemontus.glucose.demo.presentation

import android.view.ViewGroup
import com.github.daemontus.glucose.demo.R
import com.glucose.app.Presenter
import com.glucose.app.PresenterContext
import com.glucose.app.presenter.Argument
import com.glucose.app.presenter.longBundler

class SeriesPresenter(context: PresenterContext, parent: ViewGroup?) : Presenter(context, R.layout.presenter_series, parent) {

    val seriesId by Argument(longBundler)



}