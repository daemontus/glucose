package com.glucose.app.presenter

import android.support.annotation.IdRes
import android.view.View
import com.glucose.app.Presenter


/** Helper functions for accessing views associated with a [Presenter] **/

inline fun <reified V: View> Presenter.findView(@IdRes viewId: Int): V
        = view.findViewById(viewId) as V

inline fun <reified V: View> Presenter.findOptionalView(@IdRes viewId: Int): V?
        = view.findViewById(viewId) as V?