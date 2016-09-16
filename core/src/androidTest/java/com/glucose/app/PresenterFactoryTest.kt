package com.glucose.app

import android.app.Activity
import android.os.Bundle
import android.support.test.rule.ActivityTestRule
import android.support.test.runner.AndroidJUnit4
import android.util.SparseArray
import android.view.View
import android.view.ViewGroup
import com.glucose.app.presenter.LifecycleException
import com.glucose.app.presenter.getAlive
import com.glucose.app.presenter.isAlive
import com.glucose.app.presenter.post
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import rx.Observable
import kotlin.test.*

@RunWith(AndroidJUnit4::class)
class PresenterFactoryTest {


    @Rule @JvmField
    val activityRule: ActivityTestRule<EmptyActivity> = ActivityTestRule(EmptyActivity::class.java)

    private inner class MockPresenterHost : PresenterHost {
        override val activity: Activity
            get() = activityRule.activity
        override val factory: PresenterFactory
            get() = throw UnsupportedOperationException()
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

    private val host = MockPresenterHost()
    private val factory = PresenterFactory(host)

    @Test
    fun presenterFactory_doNothing() {
        factory.onDestroy()
    }

    @Test
    fun presenterFactory_registeredConstructor() {
        factory.register(SimplePresenter::class.java) { host, parent ->
            SimplePresenter(host, false)
        }
        val presenter = factory.obtain(SimplePresenter::class.java, null)
        assertFalse(presenter.flag)
        factory.recycle(presenter)
        factory.onDestroy()
    }

    @Test
    fun presenterFactory_reflectionConstructor() {
        val presenter = factory.obtain(SimplePresenter::class.java, null)
        assertTrue(presenter.flag)
        factory.recycle(presenter)
        factory.onDestroy()
    }

    @Test
    fun presenterFactory_afterDestroyed() {
        val p = factory.obtain(SimplePresenter::class.java, null)
        factory.onDestroy()
        assertFailsWith(LifecycleException::class) {
            factory.obtain(SimplePresenter::class.java, null)
        }
        assertFailsWith(LifecycleException::class) {
            factory.recycle(p)
        }
    }

    @Test
    fun presenterFactory_presenterCaching() {
        val p1 = factory.obtain(SimplePresenter::class.java, null)
        factory.recycle(p1)
        //assertTrue(p1.isAlive)
        val p2 = factory.obtain(SimplePresenter::class.java, null)
        //assertTrue(p2.isAlive)
        assertEquals(p1, p2)
        factory.recycle(p2)
        factory.onDestroy()
    }

    @Test
    fun presenterFactory_cachingFlag() {
        factory.register(SimplePresenter::class.java) { host, parent ->
            SimplePresenter(host, false)
        }
        val p1 = factory.obtain(SimplePresenter::class.java, null)
        //assertTrue(p1.isAlive)
        factory.recycle(p1)
        val p2 = factory.obtain(SimplePresenter::class.java, null)
        //assertTrue(p2.isAlive)
        assertNotEquals(p1, p2)
        factory.recycle(p2)
        factory.onDestroy()
    }

    @Test
    fun presenterFactory_createMultiple() {
        val p1 = factory.obtain(SimplePresenter::class.java, null)
        val p2 = factory.obtain(SimplePresenter::class.java, null)
        val p3 = factory.obtain(SimplePresenter::class.java, null)
        //assertTrue(p1.isAlive)
        //assertTrue(p2.isAlive)
        //assertTrue(p3.isAlive)
        assertNotEquals(p1, p2)
        assertNotEquals(p1, p3)
        assertNotEquals(p2, p3)
        factory.recycle(p1)
        factory.recycle(p2)
        factory.recycle(p3)
        factory.onDestroy()
    }

    @Test
    fun presenterFactory_missingConstructor() {
        assertFailsWith(LifecycleException::class) {
            factory.obtain(NoReflectionPresenter::class.java, null)
        }
        factory.onDestroy()
    }

    @Test
    fun presenterFactory_foreignPresenter() {
        val factory2 = PresenterFactory(MockPresenterHost())
        val p1 = factory.obtain(SimplePresenter::class.java, null)
        assertFailsWith(LifecycleException::class) {
            factory2.recycle(p1)
        }
        factory.recycle(p1)
        factory.onDestroy()
        factory2.onDestroy()
    }

    @Test
    fun presenterFactory_doubleRecycle() {
        val p = factory.obtain(SimplePresenter::class.java, null)
        factory.recycle(p)
        assertFailsWith(LifecycleException::class) {
            factory.recycle(p)
        }
        factory.onDestroy()
    }
/*
    @Test
    fun presenterFactory_recycleActive() {
        val p = factory.obtain(SimplePresenter::class.java, null)
        p.performAttach(Bundle())
        assertFailsWith(LifecycleException::class) {
            factory.recycle(p)
        }
        p.performDetach()
        factory.recycle(p)
        factory.onDestroy()
    }*/

    class SimplePresenter(host: PresenterHost, val flag: Boolean)
        : Presenter(host, View(host.activity)) {

        override val canBeReused: Boolean = flag

        //reflection constructor
        @Suppress("unused")
        constructor(host: PresenterHost, @Suppress("UNUSED_PARAMETER") parent: ViewGroup?) : this(host, true)

    }

    class NoReflectionPresenter(host: PresenterHost) : Presenter(host, View(host.activity))

}