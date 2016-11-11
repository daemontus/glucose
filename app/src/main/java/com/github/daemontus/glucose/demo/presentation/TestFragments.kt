package com.github.daemontus.glucose.demo.presentation

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.github.daemontus.glucose.demo.R
import com.glucose.app.PresenterFragment

class SimpleFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val layout = inflater.inflate(R.layout.presenter_data, container, false)
        (layout.findViewById(R.id.data_title) as TextView).text = "A Fragment inside a Presenter here!"
        return layout
    }

}

class NestedPresenterTree : PresenterFragment() {

}