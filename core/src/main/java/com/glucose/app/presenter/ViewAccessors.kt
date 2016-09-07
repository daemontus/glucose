package com.glucose.app.presenter

import android.support.annotation.IdRes
import android.view.View
import com.glucose.app.Presenter


/** Helper functions for accessing views associated with a [Presenter] **/

@Suppress("UNCHECKED_CAST")
fun <V: View> Presenter.findView(@IdRes viewId: Int): V = view.findViewById(viewId) as V

@Suppress("UNCHECKED_CAST")
fun <V: View> Presenter.findOptionalView(@IdRes viewId: Int): V? = view.findViewById(viewId) as V?