package com.glucose.app.presenter

import android.app.Activity
import android.os.Bundle
import android.util.SparseArray
import android.view.ViewGroup
import com.glucose.app.Presenter
import com.glucose.app.PresenterFactory
import com.glucose.app.PresenterHost

class MockPresenterHost<out A: Activity>(
        override val activity: A
) : PresenterHost {

    override val factory: PresenterFactory = PresenterFactory(this)

    override val root: Presenter
        get() = throw UnsupportedOperationException()

    override fun <P : Presenter> attach(clazz: Class<P>, arguments: Bundle, parent: ViewGroup?): P {
        throw UnsupportedOperationException("not implemented")
    }

    override fun <P : Presenter> attachWithState(clazz: Class<P>, savedState: SparseArray<Bundle>, arguments: Bundle, parent: ViewGroup?): P {
        throw UnsupportedOperationException("not implemented")
    }

    override fun detach(presenter: Presenter) {
        throw UnsupportedOperationException("not implemented")
    }

}