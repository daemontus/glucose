package com.glucose2.app

import android.app.Activity
import android.os.Bundle
import android.util.SparseArray
import android.view.View
import rx.Observable
import rx.Observer

interface Event
interface Action

interface Holder {

    val view: View
    val parent: Presenter?

    fun onAttach(parent: Presenter)
    fun onBind(data: Bundle)

    fun onReset()
    fun onDetach()

}

interface Presenter : Holder {

    val host: PresenterHost

    fun onResume()
    fun onStart()

    fun onPause()
    fun onStop()
}

interface PresenterHost