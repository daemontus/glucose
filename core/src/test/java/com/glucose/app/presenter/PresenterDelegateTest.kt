package com.glucose.app.presenter

import android.content.ComponentCallbacks2
import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.util.SparseArray
import com.github.daemontus.glucose.core.BuildConfig
import com.glucose.app.*
import com.glucose.util.newSyntheticId
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

@RunWith(RobolectricTestRunner::class)
@Config(constants = BuildConfig::class, sdk = intArrayOf(21))
class PresenterDelegateTest {

    private val activity = setupEmptyActivity()

    @Test
    fun presenterDelegate_invalidLifecycleCalls() {
        val delegate = PresenterDelegate(activity, SimplePresenter::class.java)
        //before on create
        assertFailsWith<LifecycleException> {
            delegate.onStart()
        }
        assertFailsWith<LifecycleException> {
            delegate.onResume()
        }
        assertFailsWith<LifecycleException> {
            delegate.onPause()
        }
        assertFailsWith<LifecycleException> {
            delegate.onStop()
        }
        assertFailsWith<LifecycleException> {
            delegate.onBackPressed()
        }
        assertFailsWith<LifecycleException> {
            delegate.onConfigurationChanged(Configuration())
        }
        assertFailsWith<LifecycleException> {
            delegate.onActivityResult(1, 1, Intent())
        }
        assertFailsWith<LifecycleException> {
            delegate.onTrimMemory(1)
        }
        assertFailsWith<LifecycleException> {
            delegate.onSaveInstanceState(Bundle())
        }
        assertFailsWith<LifecycleException> {
            delegate.onRequestPermissionsResult(1, arrayOf(), intArrayOf())
        }
        assertFailsWith<LifecycleException> {
            delegate.onDestroy()
        }
        delegate.onCreate(null)
        delegate.onDestroy()
        //after on destroy
        assertFailsWith<LifecycleException> {
            delegate.onStart()
        }
        assertFailsWith<LifecycleException> {
            delegate.onResume()
        }
        assertFailsWith<LifecycleException> {
            delegate.onPause()
        }
        assertFailsWith<LifecycleException> {
            delegate.onStop()
        }
        assertFailsWith<LifecycleException> {
            delegate.onBackPressed()
        }
        assertFailsWith<LifecycleException> {
            delegate.onConfigurationChanged(Configuration())
        }
        assertFailsWith<LifecycleException> {
            delegate.onActivityResult(1, 1, Intent())
        }
        assertFailsWith<LifecycleException> {
            delegate.onTrimMemory(1)
        }
        assertFailsWith<LifecycleException> {
            delegate.onSaveInstanceState(Bundle())
        }
        assertFailsWith<LifecycleException> {
            delegate.onRequestPermissionsResult(1, arrayOf(), intArrayOf())
        }
        assertFailsWith<LifecycleException> {
            delegate.onDestroy()
        }
    }

    @Test
    fun presenterDelegate_validLifecycle() {
        val host = PresenterDelegate(activity, LifecycleObservingPresenter::class.java)
        host.factory.register(LifecycleObservingPresenter::class.java) { host, parent -> LifecycleObservingPresenter(host) }
        host.onCreate(null)
        val root = host.root as LifecycleObservingPresenter
        root.assertAttached()
        host.onStart()
        root.assertStarted()
        host.onResume()
        root.assertResumed()
        host.onPause()
        root.assertPaused()
        host.onResume()
        root.assertResumed()

        host.onBackPressed()
        root.assertBack()
        host.onActivityResult(1, 1, Intent())
        root.assertActivityResult()
        host.onRequestPermissionsResult(1, arrayOf(), intArrayOf())
        root.assertPermissionResult()
        host.onTrimMemory(1)
        root.assertMemory()
        host.onConfigurationChanged(Configuration())
        root.assertConfig()

        host.onPause()
        root.assertPaused()
        host.onStop()
        root.assertStopped()
        host.onStart()
        root.assertStarted()
        host.onStop()
        root.assertStopped()
        host.onDestroy()
        root.assertDetached()
        root.assertDestroyed()
    }

    @Test
    fun presenterDelegate_trimMemory() {
        val host = PresenterDelegate(activity, SimplePresenter::class.java)
        host.onCreate(null)
        val a = host.factory.obtain(SimplePresenter::class.java, null)
        host.factory.recycle(a)
        val b = host.factory.obtain(SimplePresenter::class.java, null)
        assertEquals(a, b)
        host.factory.recycle(b)
        host.onTrimMemory(ComponentCallbacks2.TRIM_MEMORY_RUNNING_CRITICAL)
        val c = host.factory.obtain(SimplePresenter::class.java, null)
        assertNotEquals(a, c)
        host.factory.recycle(c)
        host.onDestroy()
    }

    @Test
    fun presenterDelegate_preserveState() {
        val host1 = PresenterDelegate(activity, PresenterWithState::class.java)
        host1.onCreate(null)
        (host1.root as PresenterWithState).data = -32
        val state = Bundle()
        host1.onSaveInstanceState(state)
        host1.onDestroy()
        val host2 = PresenterDelegate(activity, PresenterWithState::class.java)
        host2.onCreate(state)
        host2.onDestroy()
    }

    @Test
    fun presenterDelegate_restoreBasedOnId() {
        val host = PresenterDelegate(activity, SimplePresenter::class.java)
        host.onCreate(null)

        //check state is restored
        val artificialState = SparseArray<Bundle>()
        val id = newSyntheticId()
        artificialState.put(id, bundle(PresenterWithState::data.name with 456))
        val p = host.attachWithState(
                PresenterWithState::class.java,
                artificialState, bundle(Presenter::id.name with id), null
        )
        assertEquals(456, p.data)
        host.detach(p)

        //check state is not hanging around any more
        val p2 = host.attachWithState(
                PresenterWithState::class.java,
                artificialState, bundle(Presenter::id.name with newSyntheticId()), null
        )
        assertEquals(0, p2.data)
        host.detach(p2)
        val p3 = host.attach(
                PresenterWithState::class.java, bundle(Presenter::id.name with id), null
        )
        assertEquals(0, p3.data)

        host.detach(p3)
        host.onDestroy()
    }

    @Test
    fun presenterDelegate_configurationChange() {
        val host = PresenterDelegate(activity, CantChangeConfiguration::class.java)
        host.onCreate(null)
        val root1 = host.root as CantChangeConfiguration
        root1.data = 87
        host.onConfigurationChanged(Configuration())
        val root2 = host.root as CantChangeConfiguration
        assertNotEquals(root1, root2)
        assertEquals(87, root2.data)
        assertTrue(root1.isDestroyed)
        root2.data = 65
        host.onStart()
        host.onResume()
        host.onConfigurationChanged(Configuration())
        val root3 = host.root as CantChangeConfiguration
        assertNotEquals(root3, root2)
        assertEquals(65, root3.data)
        assertTrue(root3.isResumed)
        assertTrue(root2.isDestroyed)
        host.onPause()
        host.onStop()
        host.onDestroy()
    }

}