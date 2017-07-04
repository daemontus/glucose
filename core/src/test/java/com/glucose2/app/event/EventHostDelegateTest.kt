package com.glucose2.app.event

import com.github.daemontus.glucose.core.BuildConfig
import com.glucose2.app.LifecycleException
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import rx.Observable
import java.util.concurrent.TimeUnit
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

    private fun advanceEvents() {
        org.robolectric.Shadows.shadowOf(EventScheduler.thread.looper).runToEndOfTasks()
    }

    @Test
    fun t01_local_delivery() {

        EventScheduler.reset()

        val host = EventHostDelegate()

        val actual = ArrayList<Pair<Any, Any>>()

        host.observeAction<A1>().subscribe { actual.add(A1 to it) }
        host.consumeAction<A2>().subscribe { actual.add(A2 to it) }
        host.observeEvent<E1>().subscribe { actual.add(E1 to it) }
        host.consumeEvent<E2>().subscribe { actual.add(E2 to it) }

        advanceEvents()
        host.emitAction(A1)
        advanceEvents()
        host.emitAction(A2)
        advanceEvents()
        host.emitEvent(B2)
        advanceEvents()
        host.emitEvent(E2)
        advanceEvents()
        host.emitEvent(E1)
        advanceEvents()
        host.emitAction(B1)
        advanceEvents()
        host.emitEvent(E2)
        advanceEvents()
        host.emitAction(A1)
        advanceEvents()

        host.destroy()

        advanceEvents()

        // the order should be preserved because of the immediate scheduler
        // the pairs are there to check if we are not mixing receivers

        val expected = listOf(A1, A2, E2, E1, E2, A1).map { it to it }

        assertEquals(expected, actual)

    }

    @Test
    fun t02_remote_action_delivery() {

        EventScheduler.reset()

        val parent = EventHostDelegate()
        val child = EventHostDelegate()

        child.attach(parent)

        val actual = ArrayList<Pair<Any, Any>>()

        child.observeAction<A1>().subscribe { actual.add(A1 to it) }
        child.observeAction<A2>().subscribe { actual.add(A2 to it) }

        advanceEvents()

        parent.emitAction(A1)
        parent.emitAction(A1)
        parent.emitAction(A2)

        advanceEvents()

        parent.consumeAction<A1>().subscribe()

        advanceEvents()

        parent.emitAction(A1)
        parent.emitEvent(E1)
        parent.emitAction(A2)
        parent.emitAction(A1)

        advanceEvents()

        child.emitAction(A1)
        child.emitAction(A2)

        advanceEvents()

        child.detach()
        parent.destroy()
        child.destroy()

        advanceEvents()

        val expected = listOf(A1, A1, A2, A2, A1, A2).map { it to it }

        assertEquals<List<Pair<Any, Any>>>(expected, actual)
    }

    @Test
    fun t03_remote_event_delivery() {

        EventScheduler.reset()

        val parent = EventHostDelegate()
        val child = EventHostDelegate()

        child.attach(parent)

        val actual = ArrayList<Pair<Any, Any>>()

        parent.observeEvent<E1>().subscribe { actual.add(E1 to it) }
        parent.observeEvent<E2>().subscribe { actual.add(E2 to it) }

        child.emitEvent(E1)
        child.emitEvent(E1)
        child.emitEvent(E2)

        advanceEvents()

        child.consumeEvent<E1>().subscribe()

        advanceEvents()

        child.emitEvent(E1)
        child.emitAction(A1)
        child.emitEvent(E2)
        child.emitEvent(E1)

        advanceEvents()

        parent.emitEvent(E1)
        parent.emitEvent(E2)

        advanceEvents()

        val expected = listOf(E1, E1, E2, E2, E1, E2).map { it to it }
        assertEquals<List<Pair<Any, Any>>>(expected, actual)
    }

    @Test
    fun t04_reattaching() {

        EventScheduler.reset()

        val child = EventHostDelegate()
        val parent = EventHostDelegate()

        child.attach(parent)

        assertFailsWith<LifecycleException> {
            child.attach(parent)
        }


        child.destroy()
        parent.destroy()

        advanceEvents()
    }

    @Test
    fun t05_empty_detach() {

        EventScheduler.reset()

        val host = EventHostDelegate()

        host.detach()

        advanceEvents()

    }

    @Test
    fun t06_complex_test() {

        EventScheduler.reset()

        val child11 = EventHostDelegate()
        val child12 = EventHostDelegate()
        val child2 = EventHostDelegate()
        val parent = EventHostDelegate()

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

        advanceEvents()

        // basic action/event sending

        child2.emitEvent(E1); advanceEvents()
        child11.emitEvent(E1); advanceEvents()
        child12.emitEvent(E1); advanceEvents()
        parent.emitEvent(E1); advanceEvents()

        parent.emitAction(A1); advanceEvents()
        child11.emitAction(A1); advanceEvents()
        child12.emitAction(A1); advanceEvents()
        child2.emitAction(A1); advanceEvents()

        // try tunnelling

        child2.emitEvent(B1); advanceEvents()
        child2.emitEvent(B2); advanceEvents()

        child2.detach()
        child11.detach()
        child12.detach()

        advanceEvents()

        child2.destroy()
        child11.destroy()
        child12.destroy()
        parent.destroy()

        advanceEvents()

        assertEquals(listOf(E1, E1, E1, E1, A1, B1, B1), actualParent)
        assertEquals(listOf(E1, E1, A1, A1, B1, B1, B2, B2), actual11)
        assertEquals(listOf(E1, A1, A1, B1), actual12)
        assertEquals(listOf(E1, A1, A1, A1, B1, B1, B2, B2), actual2)

    }

    @Test
    fun t07_scheduler() {

        EventScheduler.reset()

        var value = 0

        Observable.just(5)
                .observeOn(EventScheduler)
                .delay(10, TimeUnit.MILLISECONDS)
                .subscribe { value = it }


        advanceEvents()

        Thread.sleep(100)

        advanceEvents()

        assertEquals(5, value)
    }

}