package com.github.daemontus.glucose

import android.view.View
import com.glucose.Log
import com.glucose.app.Presenter
import com.glucose.app.PresenterContext
import com.glucose.app.RootActivity
import com.glucose.app.postAction
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.shadows.ShadowLog
import rx.Observable
import java.util.concurrent.TimeUnit

@RunWith(RobolectricTestRunner::class)
class ActionTest {

    @Before
    fun setUp() {
        ShadowLog.stream = System.out
    }

    @Test
    fun basicTest() {
        Robolectric.setupActivity(ActionActivity::class.java)
    }

}

class ActionActivity : RootActivity() {

    init {
        register(ActionPresenter::class.java, { a, b -> ActionPresenter(View(this), a) })
    }

    override val rootPresenter: Class<out Presenter<*>> = ActionPresenter::class.java

}

class ActionPresenter(view: View, context: PresenterContext) : Presenter<ActionActivity>(view, context) {

    override fun onStart() {
        super.onStart()
        val result = Observable.just(10)
                .doOnSubscribe { Log.d("Subscribed") }
                .doOnEach { Log.d("Before delay $it") }
                .delay(1000, TimeUnit.MILLISECONDS)
                .doOnEach { Log.d("After delay $it") }
                .postAction(this)

        result.subscribe({ Log.d("Action result: $it") }, { Log.d("Action error: $it") })
        result.subscribe { Log.d("Another result: $it") }

        Observable.just(10)
                .doOnSubscribe { Log.d("Subscribed 2") }
                .doOnEach { Log.d("Before delay 2 $it") }
                .delay(100, TimeUnit.MILLISECONDS)
                .doOnEach { Log.d("After delay 2 $it") }
                .postAction(this)
                .subscribe { Log.d("Result 2 $it") }


    }
}
