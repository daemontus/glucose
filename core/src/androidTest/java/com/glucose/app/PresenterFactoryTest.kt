package com.glucose.app

import android.content.res.Configuration
import android.os.Bundle
import android.support.test.rule.ActivityTestRule
import android.support.test.runner.AndroidJUnit4
import com.glucose.app.presenter.LifecycleException
import com.glucose.app.presenter.isAlive
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import kotlin.test.*

@RunWith(AndroidJUnit4::class)
class PresenterFactoryTest {


    @Rule @JvmField
    val activityRule: ActivityTestRule<EmptyActivity> = ActivityTestRule(EmptyActivity::class.java)

    private val host = MockPresenterHost(activityRule)
    private val factory = host.factory

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
        assertTrue(p1.isAlive)
        val p2 = factory.obtain(SimplePresenter::class.java, null)
        assertTrue(p2.isAlive)
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
        assertTrue(p1.isAlive)
        factory.recycle(p1)
        val p2 = factory.obtain(SimplePresenter::class.java, null)
        assertTrue(p2.isAlive)
        assertNotEquals(p1, p2)
        factory.recycle(p2)
        factory.onDestroy()
    }

    @Test
    fun presenterFactory_createMultiple() {
        val p1 = factory.obtain(SimplePresenter::class.java, null)
        val p2 = factory.obtain(SimplePresenter::class.java, null)
        val p3 = factory.obtain(SimplePresenter::class.java, null)
        assertTrue(p1.isAlive)
        assertTrue(p2.isAlive)
        assertTrue(p3.isAlive)
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
        val factory2 = PresenterFactory(MockPresenterHost(activityRule))
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
    }

    @Test
    fun presenterFactory_leakedPresenter() {
        val p = factory.obtain(SimplePresenter::class.java, null)
        p.performAttach(Bundle())
        assertFailsWith<LifecycleException> {
            factory.onDestroy()
        }
    }

    @Test
    fun presenterFactory_memoryTrim() {
        val p = factory.obtain(SimplePresenter::class.java, null)
        factory.recycle(p)
        factory.trimMemory()
        val p2 = factory.obtain(SimplePresenter::class.java, null)
        assertNotEquals(p, p2)
        factory.onDestroy()
    }

    @Test
    fun presenterFactory_prepareConfigChange() {
        val forget = factory.obtain(CantChangeConfiguration::class.java, null)
        val keep = factory.obtain(CanChangeConfiguration::class.java, null)
        factory.recycle(forget)
        factory.recycle(keep)
        factory.prepareConfigChange()
        assertEquals(keep, factory.obtain(CanChangeConfiguration::class.java, null))
        assertNotEquals(forget, factory.obtain(CantChangeConfiguration::class.java, null))
        factory.onDestroy()
    }

    @Test
    fun presenterFactory_changeConfiguration() {
        val p = factory.obtain(CanChangeConfiguration::class.java, null)
        factory.recycle(p)
        val config = Configuration()
        factory.performConfigChange(config)
        val p2 = factory.obtain(CanChangeConfiguration::class.java, null)
        assertEquals(p, p2)
        assertEquals(config, p.config)
        factory.onDestroy()
    }

}