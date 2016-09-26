package com.glucose.app.presenter

import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.view.ViewGroup
import android.widget.FrameLayout
import com.github.daemontus.glucose.core.BuildConfig
import com.github.daemontus.glucose.core.R
import com.glucose.app.Presenter
import com.glucose.app.PresenterDelegate
import com.glucose.app.PresenterGroup
import com.glucose.app.PresenterHost
import com.glucose.util.newSyntheticId
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@RunWith(RobolectricTestRunner::class)
@Config(constants = BuildConfig::class, sdk = intArrayOf(21))
class PresenterGroupTest {

    val activity = setupEmptyActivity()

    @Test
    fun presenterGroup_simpleLifecycle() {
        val host = PresenterDelegate(activity, SimpleGroup::class.java)
        host.factory.register(LifecycleObservingPresenter::class.java) { host, parent -> LifecycleObservingPresenter(host) }
        host.onCreate(null)
        val group = host.root as PresenterGroup
        val child = group.attach(R.id.inflated_view, LifecycleObservingPresenter::class.java)
        child.assertAttached()
        host.onStart()
        child.assertStarted()
        host.onResume()
        child.assertResumed()

        host.onActivityResult(1,1, Intent())
        child.assertActivityResult()
        host.onRequestPermissionsResult(1, arrayOf(), intArrayOf())
        child.assertPermissionResult()
        host.onBackPressed()
        child.assertBack()
        host.onTrimMemory(1)
        child.assertMemory()
        host.onConfigurationChanged(Configuration())
        child.assertConfig()

        host.onPause()
        child.assertPaused()
        host.onResume()
        child.assertResumed()
        host.onPause()
        child.assertPaused()
        host.onStop()
        child.assertStopped()
        group.remove(child)
        //here child shouldn't detach, just leave the view
        assertTrue(child.isAttached)
        group.add(R.id.inflated_view, child)
        group.detach(child)
        child.assertDetached()
        host.onDestroy()
    }

    @Test
    fun presenterGroup_onBackPressed() {
        val host = PresenterDelegate(activity, SimpleGroup::class.java)
        host.onCreate(null)
        val group = host.root as PresenterGroup
        val child = group.attach(R.id.inflated_view, GoBackPresenter::class.java)
        assertEquals(3, child.goBack)
        assertTrue(host.onBackPressed())
        assertEquals(2, child.goBack)
        assertTrue(host.onBackPressed())
        assertEquals(1, child.goBack)
        assertFalse(host.onBackPressed())
        assertEquals(0, child.goBack)
    }

    @Test
    fun presenterGroup_cleanUpWhenDetaching() {
        val host = PresenterDelegate(activity, SimpleGroup::class.java)
        host.onCreate(null)
        val group = host.root as PresenterGroup
        val child = group.attach(R.id.inflated_view, SimplePresenter::class.java)
        assertTrue(child.isAttached)
        host.onDestroy()
        assertTrue(child.isDestroyed)
        assertTrue(group.isDestroyed)
    }

    @Test
    fun presenterGroup_stateSaveAndRestore() {
        val host = PresenterDelegate(activity, SimpleGroup::class.java)
        host.onCreate(null)
        val group = host.root as SimpleGroup
        group.attach(R.id.inflated_view, GoBackPresenter::class.java,
                bundle(GoBackPresenter::goBack.name with 14)
        )
        group.attach(R.id.inflated_view, GoBackPresenter::class.java,
                bundle(GoBackPresenter::goBack.name with 5)
        )
        val genericContainer = FrameLayout(activity)
        group.container.addView(genericContainer)
        group.attach(genericContainer, SimplePresenter::class.java,
                bundle(Presenter::id.name with newSyntheticId())
        )
        val state = Bundle().apply {
            host.onSaveInstanceState(this)
        }
        host.onDestroy()
        val host2 = PresenterDelegate(activity, SimpleGroup::class.java)
        host2.onCreate(state)
        val group2 = host2.root as SimpleGroup
        assertEquals(2, group2.container.childCount)
        assertEquals(2, group2.presenters.size)
        val back1b = group2.presenters[0] as GoBackPresenter
        val back2b = group2.presenters[1] as GoBackPresenter
        assertEquals(14, back1b.goBack)
        assertEquals(5, back2b.goBack)
        host2.onDestroy()
    }

    @Test
    fun presenterGroup_configChange() {
        val host = PresenterDelegate(activity, PresenterGroup::class.java)
        host.factory.register(PresenterGroup::class.java) { host, parent ->
            PresenterGroup(host, FrameLayout(host.activity))
        }
        host.onCreate(null)
        host.onStart()
        host.onResume()
        val group = host.root as PresenterGroup
        val parent = group.view as FrameLayout
        val child1 = group.attach(parent, PresenterWithState::class.java,
                bundle(PresenterWithState::data.name with 17)
        )
        group.attach(parent, CantChangeConfiguration::class.java,
                bundle(CantChangeConfiguration::data.name with 31)
        )
        val child3 = group.attach(parent, PresenterWithState::class.java,
                bundle(PresenterWithState::data.name with 53)
        )
        group.attach(parent, CantChangeConfiguration::class.java,
                bundle(CantChangeConfiguration::data.name with 47))
        host.onConfigurationChanged(Configuration())
        assertEquals(4, group.presenters.size)
        val newChild1 = group.presenters[0]
        val newChild2 = group.presenters[1]
        val newChild3 = group.presenters[2] as CantChangeConfiguration
        val newChild4 = group.presenters[3] as CantChangeConfiguration
        assertEquals(child1, newChild1)
        assertEquals(child3, newChild2)
        //preserve view order
        assertEquals(parent.getChildAt(0), newChild1.view)
        assertEquals(parent.getChildAt(1), newChild3.view)
        assertEquals(parent.getChildAt(2), newChild2.view)
        assertEquals(parent.getChildAt(3), newChild4.view)
        assertEquals(31, newChild3.data)
        assertEquals(47, newChild4.data)
        host.onPause()
        host.onStop()
        host.onDestroy()
    }

    @Test
    fun presenterGroup_invalidAdd() {
        val host = PresenterDelegate(activity, SimpleGroup::class.java)
        host.onCreate(null)
        val group = host.root as SimpleGroup
        val p = host.factory.obtain(SimplePresenter::class.java, null)
        assertFailsWith<LifecycleException> {
            group.add(R.id.inflated_view, p)
        }
        host.factory.recycle(p)
        val p2 = group.attach(R.id.inflated_view, SimplePresenter::class.java)
        assertFailsWith<LifecycleException> {
            group.add(R.id.inflated_view, p2)
        }
        val host2 = PresenterDelegate(activity, SimpleGroup::class.java)
        host2.onCreate(null)
        val p3 = host2.attach(SimplePresenter::class.java)
        assertFailsWith<LifecycleException> {
            group.add(R.id.inflated_view, p3)
        }
        host2.detach(p3)
        host.onDestroy()
        host2.onDestroy()
    }

    @Test
    fun presenterGroup_invalidRemove() {
        val host = PresenterDelegate(activity, SimpleGroup::class.java)
        host.onCreate(null)
        val group = host.root as SimpleGroup
        val p = host.attach(SimplePresenter::class.java)
        assertFailsWith<LifecycleException> {
            group.remove(p)
        }
        host.detach(p)
        host.onDestroy()
    }

    @Test
    fun presenterGroup_childObservables() {
        val host = PresenterDelegate(activity, SimpleGroup::class.java)
        host.onCreate(null)
        val group = host.root as SimpleGroup
        val childAdd = group.onChildAdd.replay()
        childAdd.connect()
        val childRemove = group.onChildRemove.replay()
        childRemove.connect()
        val p1 = group.attach(R.id.inflated_view, SimplePresenter::class.java)
        val p2 = group.attach(R.id.inflated_view, SimpleGroup::class.java)
        group.detach(p2)
        group.detach(p1)
        host.onDestroy()
        assertEquals(listOf(p1, p2), childAdd.toBlocking().toIterable().toList())
        assertEquals(listOf(p2, p1), childRemove.toBlocking().toIterable().toList())
    }

    @Test
    fun presenterGroup_childObservablesRecursive() {
        val host = PresenterDelegate(activity, SimpleGroup::class.java)
        host.onCreate(null)
        val group = host.root as SimpleGroup
        val childAdd = group.onChildAddRecursive.replay()
        childAdd.connect()
        val childRemove = group.onChildRemoveRecursive.replay()
        childRemove.connect()
        val g = group.attach(R.id.inflated_view, SimpleGroup::class.java)
        val p1 = g.attach(R.id.inflated_view, SimplePresenter::class.java)
        val p2 = g.attach(R.id.inflated_view, SimplePresenter::class.java)
        val p3 = group.attach(R.id.inflated_view, SimplePresenter::class.java)
        group.detach(g)
        group.detach(p3)
        host.onDestroy()
        assertEquals(listOf(g, p1, p2, p3), childAdd.toBlocking().toIterable().toList())
        //this is actually correct, since g is removed from the view tree first and then p1 and p2
        //are removed from g
        assertEquals(listOf(g, p1, p2, p3), childRemove.toBlocking().toIterable().toList())
    }

    @Test
    fun presenterGroup_findByParent() {
        val host = PresenterDelegate(activity, SimpleGroup::class.java)
        host.onCreate(null)
        val group = host.root as SimpleGroup
        val p1 = group.attach(R.id.inflated_view, GoBackPresenter::class.java,
                bundle(GoBackPresenter::goBack.name with 14)
        )
        val p2 = group.attach(R.id.inflated_view, GoBackPresenter::class.java,
                bundle(GoBackPresenter::goBack.name with 5)
        )
        val genericContainer = FrameLayout(activity)
        group.container.addView(genericContainer)
        val p3 = group.attach(genericContainer, SimplePresenter::class.java,
                bundle(Presenter::id.name with newSyntheticId())
        )
        assertEquals(listOf(p1, p2), group.findPresentersByParent(R.id.inflated_view))
        assertEquals(listOf(p3), group.findPresentersByParent(genericContainer))
        host.onDestroy()
    }

    @Test
    fun presenterGroup_findById() {
        val host = PresenterDelegate(activity, SimpleGroup::class.java)
        host.onCreate(null)
        val id1 = newSyntheticId()
        val id2 = newSyntheticId()
        val group = host.root as SimpleGroup
        val p1 = group.attach(R.id.inflated_view, GoBackPresenter::class.java,
                bundle(GoBackPresenter::goBack.name with 14) and
                        (Presenter::id.name with id1)
        )
        val p2 = group.attach(R.id.inflated_view, SimpleGroup::class.java,
                bundle(GoBackPresenter::goBack.name with 5)
        )
        val p3 = p2.attach(R.id.inflated_view, SimplePresenter::class.java,
                bundle(Presenter::id.name with newSyntheticId()) and
                        (Presenter::id.name with id2)
        )
        assertEquals(p1, group.findPresenter(id1, false))
        assertEquals(p1, group.findOptionalPresenter(id1, false))
        assertFailsWith<KotlinNullPointerException> {
            group.findPresenter(id2, false)
        }
        assertEquals(null, group.findOptionalPresenter(id2, false))

        assertEquals(p1, group.findPresenter(id1, true))
        assertEquals(p1, group.findOptionalPresenter(id1, true))
        assertEquals(p3, group.findPresenter(id2, true))
        assertEquals(p3, group.findOptionalPresenter(id2, true))
        host.onDestroy()
    }

    class SimpleGroup(host: PresenterHost, parent: ViewGroup?) : PresenterGroup(host, R.layout.presenter_test, parent) {
        val container = view.findViewById(R.id.inflated_view) as ViewGroup
    }
    class GoBackPresenter(host: PresenterHost, parent: ViewGroup?) : PresenterGroup(host, R.layout.presenter_test, parent) {

        var goBack by NativeState(3, intBundler)

        override fun onBackPressed(): Boolean {
            goBack -= 1
            return goBack > 0
        }
    }

}