package com.glucose.app

import android.content.res.Configuration
import android.os.Bundle
import android.support.test.rule.ActivityTestRule
import android.support.test.runner.AndroidJUnit4
import android.util.SparseArray
import android.view.View
import android.widget.FrameLayout
import com.github.daemontus.glucose.core.test.R
import com.glucose.app.presenter.CannotExecuteException
import com.glucose.app.presenter.Lifecycle
import com.glucose.app.presenter.Lifecycle.Event.*
import com.glucose.app.presenter.LifecycleException
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import rx.Observable
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@RunWith(AndroidJUnit4::class)
class PresenterTest {


    @Rule @JvmField
    val activityRule: ActivityTestRule<EmptyActivity> = ActivityTestRule(EmptyActivity::class.java)

    private val host = MockPresenterHost(activityRule)

    @Test
    fun presenter_createWithArtificialView() {
        val view = View(activityRule.activity)
        val p = Presenter(host, view)
        assertEquals(view, p.view)
        assertEquals(host, p.host)
    }

    @Test
    fun presenter_createWithInflatedView() {
        val p = Presenter(host, R.layout.presenter_test, null)
        assertTrue(p.view is FrameLayout)
        assertEquals(R.id.inflated_view, p.view.id)
        assertEquals(host, p.host)
    }

    @Test
    fun presenter_validLifecycleTransitions() {
        val p = LifecycleTestingPresenter(host)
        val args1 = Bundle().apply { this.putInt("test", 1) }
        val args2 = Bundle().apply { this.putInt("test", 2) }

        p.performAttach(args1)
        assertTrue(p.onAttachCalled)
        assertEquals(args1, p.arguments)
        p.performStart()
        assertTrue(p.onStartCalled)
        p.performResume()
        assertTrue(p.onResumeCalled)
        p.performPause()
        assertTrue(p.onPauseCalled)
        p.performResume()
        p.performPause()
        p.performStop()
        assertTrue(p.onStopCalled)
        p.performStart()
        p.performStop()
        p.performDetach()
        assertTrue(p.onDetachCalled)
        p.performAttach(args2)
        assertEquals(args2, p.arguments)
        p.performDetach()
        p.performDestroy()
        assertTrue(p.onDestroyCalled)
    }

    @Test
    fun presenter_invalidLifecycleTransitions() {
        val p = Presenter(host, R.layout.presenter_test, null)
        assertFailsWith<LifecycleException> { p.performStart() }
        assertFailsWith<LifecycleException> { p.performResume() }
        assertFailsWith<LifecycleException> { p.performPause() }
        assertFailsWith<LifecycleException> { p.performStop() }
        assertFailsWith<LifecycleException> { p.performDetach() }
        p.performAttach(Bundle())

        assertFailsWith<LifecycleException> { p.performAttach(Bundle()) }
        assertFailsWith<LifecycleException> { p.performResume() }
        assertFailsWith<LifecycleException> { p.performPause() }
        assertFailsWith<LifecycleException> { p.performStop() }
        assertFailsWith<LifecycleException> { p.performDestroy() }
        p.performStart()

        assertFailsWith<LifecycleException> { p.performStart() }
        assertFailsWith<LifecycleException> { p.performAttach(Bundle()) }
        assertFailsWith<LifecycleException> { p.performPause() }
        assertFailsWith<LifecycleException> { p.performDetach() }
        assertFailsWith<LifecycleException> { p.performDestroy() }
        p.performResume()

        assertFailsWith<LifecycleException> { p.performAttach(Bundle()) }
        assertFailsWith<LifecycleException> { p.performStart() }
        assertFailsWith<LifecycleException> { p.performResume() }
        assertFailsWith<LifecycleException> { p.performStop() }
        assertFailsWith<LifecycleException> { p.performDestroy() }
        assertFailsWith<LifecycleException> { p.performDetach() }

        p.performPause()
        p.performStop()
        p.performDetach()
        p.performDestroy()
    }

    @Test
    fun presenter_invalidLifecycleOverride() {
        val attach = LifecycleTestingPresenter(host, superAttach = false)
        assertFailsWith<LifecycleException> { attach.performAttach(Bundle()) }

        val start = LifecycleTestingPresenter(host, superStart = false)
        start.performAttach(Bundle())
        assertFailsWith<LifecycleException> { start.performStart() }

        val resume = LifecycleTestingPresenter(host, superResume = false)
        resume.performAttach(Bundle())
        resume.performStart()
        assertFailsWith<LifecycleException> { resume.performResume() }

        val pause = LifecycleTestingPresenter(host, superPause = false)
        pause.performAttach(Bundle())
        pause.performStart()
        pause.performResume()
        assertFailsWith<LifecycleException> { pause.performPause() }

        val stop = LifecycleTestingPresenter(host, superStop = false)
        stop.performAttach(Bundle())
        stop.performStart()
        stop.performResume()
        stop.performPause()
        assertFailsWith<LifecycleException> { stop.performStop() }

        val detach = LifecycleTestingPresenter(host, superDetach = false)
        detach.performAttach(Bundle())
        detach.performStart()
        detach.performResume()
        detach.performPause()
        detach.performStop()
        assertFailsWith<LifecycleException> { detach.performDetach() }

        val destroy = LifecycleTestingPresenter(host, superDestroy = false)
        destroy.performAttach(Bundle())
        destroy.performStart()
        destroy.performResume()
        destroy.performPause()
        destroy.performStop()
        destroy.performDetach()
        assertFailsWith<LifecycleException> { destroy.performDestroy() }
    }

    @Test
    fun presenter_invalidHostAccess() {
        val p = Presenter(host, R.layout.presenter_test, null)
        assertEquals(host, p.host)
        p.performDestroy()
        assertFailsWith<LifecycleException> { p.host }
    }

    @Test
    fun presenter_invalidArgumentsAccess() {
        val p = Presenter(host, R.layout.presenter_test, null)
        assertFailsWith<LifecycleException> { p.arguments }
        val args = Bundle().apply { this.putInt("test", 1) }
        p.performAttach(args)
        assertEquals(args, p.arguments)
        p.performDetach()
        assertFailsWith<LifecycleException> { p.arguments }
    }

    @Test
    fun presenter_actionHost() {
        //Just a small test, most of the functionality is covered in the unit tests
        val p = Presenter(host, R.layout.presenter_test, null)
        assertFailsWith<CannotExecuteException> {
            p.post(Observable.just(1, 2, 3)).toBlocking().toIterable().toList()
        }
        p.performAttach(Bundle())
        assertEquals(listOf(1, 2, 3), p.post(Observable.just(1, 2, 3)).toBlocking().toIterable().toList())
        p.performDetach()
        assertFailsWith<CannotExecuteException> {
            p.post(Observable.just(1, 2, 3)).toBlocking().toIterable().toList()
        }
        p.performDestroy()
    }

    @Test
    fun presenter_lifecycleHost() {
        //Just a small test, most of the functionality is covered in the unit tests
        val p = Presenter(host, R.layout.presenter_test, null)
        val notificationLog = p.lifecycleEvents.replay()
        notificationLog.connect()
        var resumed = false
        val callback = { resumed = true }
        p.addEventCallback(Lifecycle.Event.RESUME, callback)
        p.performAttach(Bundle())
        p.performStart()
        p.performResume()
        assertTrue(resumed)
        assertFalse(p.removeEventCallback(Lifecycle.Event.RESUME, callback))
        p.performPause()
        p.performStop()
        p.performDetach()
        p.performDestroy()
        assertEquals(
                listOf(ATTACH, START, RESUME, PAUSE, STOP, DETACH, DESTROY),
                notificationLog.toBlocking().toIterable().toList()
        )
    }

    @Test
    fun presenter_saveState() {
        val p = Presenter(host, R.layout.presenter_test, null)
        p.performAttach(Bundle().apply {
            putInt(Presenter::id.name, 10)
            putBoolean(Presenter::canReattachAfterStateChange.name, false)
            putString("some random data", "data!")
        })
        val state = Bundle()
        p.onSaveInstanceState(state)
        assertEquals(10, state.getInt(Presenter::id.name))
        assertEquals(false, state.getBoolean(Presenter::canReattachAfterStateChange.name))
        assertEquals("data!", state.getString("some random data"))
        p.performDetach()
        p.performDestroy()
    }

    @Test
    fun presenter_saveHierarchy() {
        val p = Presenter(host, R.layout.presenter_test, null)
        p.performAttach(Bundle().apply {
            putString("data", "data")
        })
        val stateMap = SparseArray<Bundle>()
        val state = p.saveHierarchyState(stateMap)
        assertEquals("data", state.getString("data"))
        assertEquals(0, stateMap.size())
        p.performDetach()
        p.performAttach(Bundle().apply {
            putInt(Presenter::id.name, 25)
            putString("data", "data")
        })
        val state2 = p.saveHierarchyState(stateMap)
        assertEquals("data", state2.getString("data"))
        assertEquals(1, stateMap.size())
        val stateFromMap = stateMap.get(25)
        assertEquals("data", stateFromMap.getString("data"))
        p.performDetach()
        p.performDestroy()
    }

    @Test
    fun presenter_invalidConfigChange() {
        val p = PresenterFactoryTest.CantChangeConfiguration(host, null)
        assertFailsWith<LifecycleException> {
            p.onConfigurationChanged(Configuration())
        }
    }

    class LifecycleTestingPresenter(
            host: PresenterHost,
            private val superAttach: Boolean = true,
            private val superStart: Boolean = true,
            private val superResume: Boolean = true,
            private val superPause: Boolean = true,
            private val superStop: Boolean = true,
            private val superDetach: Boolean = true,
            private val superDestroy: Boolean = true
    ) : Presenter(host, View(host.activity)) {

        var onAttachCalled = false
        var onStartCalled = false
        var onResumeCalled = false
        var onPauseCalled = false
        var onStopCalled = false
        var onDetachCalled = false
        var onDestroyCalled = false

        override fun onAttach(arguments: Bundle) {
            if (superAttach) super.onAttach(arguments)
            onAttachCalled = true
        }

        override fun onStart() {
            if (superStart) super.onStart()
            onStartCalled = true
        }

        override fun onResume() {
            if (superResume) super.onResume()
            onResumeCalled = true
        }

        override fun onPause() {
            if (superPause) super.onPause()
            onPauseCalled = true
        }

        override fun onStop() {
            if (superStop) super.onStop()
            onStopCalled = true
        }

        override fun onDetach() {
            if (superDetach) super.onDetach()
            onDetachCalled = true
        }

        override fun onDestroy() {
            if (superDestroy) super.onDestroy()
            onDestroyCalled = true
        }
    }

}