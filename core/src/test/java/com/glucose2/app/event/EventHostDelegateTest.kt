package com.glucose2.app.event

import com.github.daemontus.glucose.core.BuildConfig
import com.glucose2.app.LifecycleException
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import rx.schedulers.Schedulers
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

@RunWith(RobolectricTestRunner::class)
@Config(constants = BuildConfig::class, sdk = intArrayOf(21))
class EventHostDelegateTest {

    object A1 : Action {
        override fun toString(): String = "A1"
    }
    object A2 : Action {
        override fun toString(): String = "A2"
    }
    object E1 : Event {
        override fun toString(): String = "E1"
    }
    object E2 : Event {
        override fun toString(): String = "E2"
    }

    object B1 : Action, Event {
        override fun toString(): String = "B1"
    }
    object B2 : Action, Event {
        override fun toString(): String = "B2"
    }

    @Test
    fun t01_local_delivery() {
        val host = EventHostDelegate(Schedulers.immediate())

        val actual = ArrayList<Pair<Any, Any>>()

        host.observeAction<A1>().subscribe { actual.add(A1 to it) }
        host.consumeAction<A2>().subscribe { actual.add(A2 to it) }
        host.observeEvent<E1>().subscribe { actual.add(E1 to it) }
        host.consumeEvent<E2>().subscribe { actual.add(E2 to it) }

        host.emitAction(A1)
        host.emitAction(A2)
        host.emitEvent(B2)
        host.emitEvent(E2)
        host.emitEvent(E1)
        host.emitAction(B1)
        host.emitEvent(E2)
        host.emitAction(A1)

        host.destroy()

        // the order should be preserved because of the immediate scheduler
        // the pairs are there to check if we are not mixing receivers

        val expected = listOf(A1, A2, E2, E1, E2, A1).map { it to it }

        assertEquals(expected, actual)

    }

    @Test
    fun t02_remote_action_delivery() {
        val parent = EventHostDelegate(Schedulers.immediate())
        val child = EventHostDelegate(Schedulers.immediate())

        child.attach(parent)

        val actual = ArrayList<Pair<Any, Any>>()

        child.observeAction<A1>().subscribe { actual.add(A1 to it) }
        child.observeAction<A2>().subscribe { actual.add(A2 to it) }

        parent.emitAction(A1)
        parent.emitAction(A1)
        parent.emitAction(A2)

        parent.consumeAction<A1>().subscribe()

        parent.emitAction(A1)
        parent.emitEvent(E1)
        parent.emitAction(A2)
        parent.emitAction(A1)

        child.emitAction(A1)
        child.emitAction(A2)

        child.detach()
        parent.destroy()
        child.destroy()

        val expected = listOf(A1, A1, A2, A2, A1, A2).map { it to it }

        assertEquals<List<Pair<Any, Any>>>(expected, actual)
    }

    @Test
    fun t03_remote_event_delivery() {
        val parent = EventHostDelegate(Schedulers.immediate())
        val child = EventHostDelegate(Schedulers.immediate())

        child.attach(parent)

        val actual = ArrayList<Pair<Any, Any>>()

        parent.observeEvent<E1>().subscribe { actual.add(E1 to it) }
        parent.observeEvent<E2>().subscribe { actual.add(E2 to it) }

        child.emitEvent(E1)
        child.emitEvent(E1)
        child.emitEvent(E2)

        child.consumeEvent<E1>().subscribe()

        child.emitEvent(E1)
        child.emitAction(A1)
        child.emitEvent(E2)
        child.emitEvent(E1)

        parent.emitEvent(E1)
        parent.emitEvent(E2)

        val expected = listOf(E1, E1, E2, E2, E1, E2).map { it to it }
        assertEquals<List<Pair<Any, Any>>>(expected, actual)
    }

    @Test
    fun t04_reattaching() {
        val child = EventHostDelegate(Schedulers.immediate())
        val parent = EventHostDelegate(Schedulers.immediate())

        child.attach(parent)

        assertFailsWith<LifecycleException> {
            child.attach(parent)
        }

        child.destroy()
        parent.destroy()
    }

    @Test
    fun t05_empty_detach() {
        val host = EventHostDelegate(Schedulers.immediate())

        host.detach()
    }

    @Test
    fun t06_complex_test() {

        val child11 = EventHostDelegate(Schedulers.immediate())
        val child12 = EventHostDelegate(Schedulers.immediate())
        val child2 = EventHostDelegate(Schedulers.immediate())
        val parent = EventHostDelegate(Schedulers.immediate())

        child11.attach(parent)
        child12.attach(parent)
        child2.attach(child11)

        val actualParent = ArrayList<Any>()
        val actual11 = ArrayList<Any>()
        val actual12 = ArrayList<Any>()
        val actual2 = ArrayList<Any>()

        // tunnel B1 through parent
        parent.consumeEvent<B1>().subscribe { parent.emitAction(B1) }

        // tunnel B2 through child11
        child11.consumeEvent<B2>().subscribe { child11.emitAction(B2) }

        // hook everything to the lists
        parent.observeEvent<Event>().subscribe { actualParent.add(it) }
        parent.observeAction<Action>().subscribe { actualParent.add(it) }
        child11.observeEvent<Event>().subscribe { actual11.add(it) }
        child11.observeAction<Action>().subscribe { actual11.add(it) }
        child12.observeEvent<Event>().subscribe { actual12.add(it) }
        child12.observeAction<Action>().subscribe { actual12.add(it) }
        child2.observeEvent<Event>().subscribe { actual2.add(it) }
        child2.observeAction<Action>().subscribe { actual2.add(it) }

        // basic action/event sending

        child2.emitEvent(E1)
        child11.emitEvent(E1)
        child12.emitEvent(E1)
        parent.emitEvent(E1)

        parent.emitAction(A1)
        child11.emitAction(A1)
        child12.emitAction(A1)
        child2.emitAction(A1)

        // try tunnelling

        child2.emitEvent(B1)
        child2.emitEvent(B2)

        child2.detach()
        child11.detach()
        child12.detach()

        child2.destroy()
        child11.destroy()
        child12.destroy()
        parent.destroy()

        assertEquals(listOf(E1, E1, E1, E1, A1, B1, B1), actualParent)
        assertEquals(listOf(E1, E1, A1, A1, B1, B1, B2, B2), actual11)
        assertEquals(listOf(E1, A1, A1, B1), actual12)
        assertEquals(listOf(E1, A1, A1, A1, B1, B1, B2, B2), actual2)

    }

}