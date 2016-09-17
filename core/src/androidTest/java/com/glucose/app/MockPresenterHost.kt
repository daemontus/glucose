package com.glucose.app

import android.app.Activity
import android.os.Bundle
import android.support.test.rule.ActivityTestRule
import android.util.SparseArray
import android.view.ViewGroup

class MockPresenterHost<A: Activity>(
        private val activityRule: ActivityTestRule<A>
) : PresenterHost {

    override val activity: Activity
        get() = activityRule.activity

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