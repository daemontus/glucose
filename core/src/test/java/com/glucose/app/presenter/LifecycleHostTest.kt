package com.glucose.app.presenter

import com.glucose.app.presenter.Lifecycle.Event.*
import com.glucose.app.presenter.Lifecycle.State.*
import org.junit.Test
import rx.Observable
import rx.subjects.PublishSubject
import java.util.*
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

//Note: In these tests, everything is running on one thread, so there shouldn't be
//any major differences between synchronous and asynchronous notifications.
class LifecycleHostTest {

    private val lifecycle = LifecycleDelegate()

    private fun doTransition(state: Lifecycle.State) {
        lifecycle.state = state
        assertEquals(state, lifecycle.state)
        assertTrue(lifecycle.isAlive == state >= ALIVE)
        assertTrue(lifecycle.isAttached == state >= ATTACHED)
        assertTrue(lifecycle.isStarted == state >= STARTED)
        assertTrue(lifecycle.isResumed == state >= RESUMED)
        assertTrue(lifecycle.isDestroyed == state <= DESTROYED)
    }

    private fun <T> Observable<T>.toListInternal(): List<T> = this.toBlocking().toIterable().toList()

    @Test
    fun lifecycleDelegate_validTransitions() {
        doTransition(ATTACHED)
        doTransition(STARTED)
        doTransition(RESUMED)
        doTransition(STARTED)
        doTransition(RESUMED)
        doTransition(STARTED)
        doTransition(ATTACHED)
        doTransition(STARTED)
        doTransition(ATTACHED)
        doTransition(ALIVE)
        doTransition(ATTACHED)
        doTransition(ALIVE)
        doTransition(DESTROYED)
    }

    @Test
    fun lifecycleDelegate_invalidTransitions() {
        //Alive -> Alive
        assertFailsWith(LifecycleException::class) {
            lifecycle.state = ALIVE
        }
        //Alive -> Started
        assertFailsWith(LifecycleException::class) {
            lifecycle.state = STARTED
        }
        //Alive -> Resumed
        assertFailsWith(LifecycleException::class) {
            lifecycle.state = RESUMED
        }
        //Alive -> Attached
        lifecycle.state = ATTACHED
        //Attached -> Attached
        assertFailsWith(LifecycleException::class) {
            lifecycle.state = ATTACHED
        }
        //Attached -> Resumed
        assertFailsWith(LifecycleException::class) {
            lifecycle.state = RESUMED
        }
        //Attached -> Destroyed
        assertFailsWith(LifecycleException::class) {
            lifecycle.state = DESTROYED
        }
        //Attached -> Started
        lifecycle.state = STARTED
        //Started -> Started
        assertFailsWith(LifecycleException::class) {
            lifecycle.state = STARTED
        }
        //Started -> Destroyed
        assertFailsWith(LifecycleException::class) {
            lifecycle.state = DESTROYED
        }
        //Started -> Alive
        assertFailsWith(LifecycleException::class) {
            lifecycle.state = ALIVE
        }
        //Started -> Resumed
        lifecycle.state = RESUMED
        //Resumed -> Resumed
        assertFailsWith(LifecycleException::class) {
            lifecycle.state = RESUMED
        }
        //Resumed -> Destroyed
        assertFailsWith(LifecycleException::class) {
            lifecycle.state = DESTROYED
        }
        //Resumed -> Alive
        assertFailsWith(LifecycleException::class) {
            lifecycle.state = ALIVE
        }
        //Resumed -> Attached
        assertFailsWith(LifecycleException::class) {
            lifecycle.state = ATTACHED
        }
        //Resumed -> Started
        lifecycle.state = STARTED
        //Started -> Attached
        lifecycle.state = ATTACHED
        //Attached -> Alive
        lifecycle.state = ALIVE
        //Alive -> Destroyed
        lifecycle.state = DESTROYED
        //Destroyed -> Alive
        assertFailsWith(LifecycleException::class) {
            lifecycle.state = ALIVE
        }
        //Destroyed -> Attached
        assertFailsWith(LifecycleException::class) {
            lifecycle.state = ATTACHED
        }
        //Destroyed -> Started
        assertFailsWith(LifecycleException::class) {
            lifecycle.state = STARTED
        }
        //Destroyed -> Resumed
        assertFailsWith(LifecycleException::class) {
            lifecycle.state = RESUMED
        }
    }

    @Test
    fun lifecycleDelegate_lifecycleCallbacks() {
        var attached = false
        var started = false
        var resumed = false
        var paused = false
        var stopped = false
        var detached = false
        var destroyed = false
        val attach = { assertFalse(attached); attached = true }
        val start = { assertFalse(started); started = true }
        val resume = { assertFalse(resumed); resumed = true }
        val pause = { assertFalse(paused); paused = true }
        val stop = { assertFalse(stopped); stopped = true }
        val detach = { assertFalse(detached); detached = true }
        val destroy = { assertFalse(destroyed); destroyed = true }
        lifecycle.addEventCallback(ATTACH, attach)
        lifecycle.addEventCallback(START, start)
        lifecycle.addEventCallback(RESUME, resume)
        lifecycle.addEventCallback(PAUSE, pause)
        lifecycle.addEventCallback(STOP, stop)
        lifecycle.addEventCallback(DETACH, detach)
        lifecycle.addEventCallback(DESTROY, destroy)
        lifecycle.state = ATTACHED
        assertTrue(attached)
        assertFalse(lifecycle.removeEventCallback(ATTACH, attach))
        lifecycle.state = STARTED
        assertTrue(started)
        assertFalse(lifecycle.removeEventCallback(START, start))
        lifecycle.state = RESUMED
        assertTrue(resumed)
        assertFalse(lifecycle.removeEventCallback(RESUME, resume))
        lifecycle.state = STARTED
        assertTrue(paused)
        assertFalse(lifecycle.removeEventCallback(PAUSE, pause))
        lifecycle.state = RESUMED
        lifecycle.state = STARTED
        lifecycle.state = ATTACHED
        assertTrue(stopped)
        assertFalse(lifecycle.removeEventCallback(STOP, stop))
        lifecycle.state = STARTED
        lifecycle.state = ATTACHED
        lifecycle.state = ALIVE
        assertTrue(detached)
        assertFalse(lifecycle.removeEventCallback(DETACH, detach))
        lifecycle.state = ATTACHED
        lifecycle.state = ALIVE
        lifecycle.state = DESTROYED
        assertTrue(destroyed)
        assertFalse(lifecycle.removeEventCallback(DESTROY, destroy))
    }

    @Test
    fun lifecycleDelegate_lifecycleNotifications() {
        val notificationLog = lifecycle.lifecycleEvents.replay()
        notificationLog.connect()
        lifecycle.state = ATTACHED
        lifecycle.state = STARTED
        lifecycle.state = RESUMED
        lifecycle.state = STARTED
        lifecycle.state = ATTACHED
        lifecycle.state = STARTED
        lifecycle.state = RESUMED
        lifecycle.state = STARTED
        lifecycle.state = ATTACHED
        lifecycle.state = ALIVE
        lifecycle.state = DESTROYED
        assertEquals(listOf(
            ATTACH, START, RESUME, PAUSE, STOP, START, RESUME, PAUSE, STOP, DETACH, DESTROY
        ), notificationLog.toListInternal())
    }

    @Test
    fun lifecycleHost_takeUntil() {
        val subject = PublishSubject.create<Int>()
        val untilAttach = subject.takeUntil(lifecycle, ATTACH).replay()
        untilAttach.connect()
        val untilStart = subject.takeUntil(lifecycle, START).replay()
        untilStart.connect()
        val untilResume = subject.takeUntil(lifecycle, RESUME).replay()
        untilResume.connect()
        val untilPause = subject.takeUntil(lifecycle, PAUSE).replay()
        untilPause.connect()
        val untilStop = subject.takeUntil(lifecycle, STOP).replay()
        untilStop.connect()
        val untilDetach = subject.takeUntil(lifecycle, DETACH).replay()
        untilDetach.connect()
        val untilDestroy = subject.takeUntil(lifecycle, DESTROY).replay()
        untilDestroy.connect()
        subject.onNext(1)
        lifecycle.state = ATTACHED
        subject.onNext(2)
        assertEquals(listOf(1), untilAttach.toListInternal())
        lifecycle.state = STARTED
        subject.onNext(3)
        assertEquals(listOf(1, 2), untilStart.toListInternal())
        lifecycle.state = RESUMED
        subject.onNext(4)
        assertEquals(listOf(1, 2, 3), untilResume.toListInternal())
        lifecycle.state = STARTED
        subject.onNext(5)
        assertEquals(listOf(1, 2, 3, 4), untilPause.toListInternal())
        lifecycle.state = ATTACHED
        subject.onNext(6)
        assertEquals(listOf(1, 2, 3, 4, 5), untilStop.toListInternal())
        lifecycle.state = ALIVE
        subject.onNext(7)
        assertEquals(listOf(1, 2, 3, 4, 5, 6), untilDetach.toListInternal())
        lifecycle.state = DESTROYED
        subject.onNext(8)
        assertEquals(listOf(1, 2, 3, 4, 5, 6, 7), untilDestroy.toListInternal())
    }

    @Test
    fun lifecycleHost_takeWhileIn() {
        val subject = PublishSubject.create<Int>()
        val whileAlive = subject.takeWhileIn(lifecycle, ALIVE)
        val whileAttached = subject.takeWhileIn(lifecycle, ATTACHED)
        val whileStarted = subject.takeWhileIn(lifecycle, STARTED)
        val whileResumed = subject.takeWhileIn(lifecycle, RESUMED)
        val alive = whileAlive.replay()
        alive.connect()
        subject.onNext(1)
        lifecycle.state = ATTACHED
        val attached = whileAttached.replay()
        attached.connect()
        subject.onNext(2)
        lifecycle.state = STARTED
        val started = whileStarted.replay()
        started.connect()
        subject.onNext(3)
        lifecycle.state = RESUMED
        val resumed = whileResumed.replay()
        resumed.connect()
        subject.onNext(4)
        lifecycle.state = STARTED
        subject.onNext(5)
        assertEquals(listOf(4), resumed.toListInternal())
        lifecycle.state = ATTACHED
        subject.onNext(6)
        assertEquals(listOf(3, 4, 5), started.toListInternal())
        lifecycle.state = ALIVE
        subject.onNext(7)
        assertEquals(listOf(2, 3, 4, 5, 6), attached.toListInternal())
        lifecycle.state = DESTROYED
        subject.onNext(8)
        assertEquals(listOf(1, 2, 3, 4, 5, 6, 7), alive.toListInternal())

        //All of them should complete without emitting anything, because the state is wrong
        assertEquals(listOf(), whileAlive.toListInternal())
        assertEquals(listOf(), whileAttached.toListInternal())
        assertEquals(listOf(), whileStarted.toListInternal())
        assertEquals(listOf(), whileResumed.toListInternal())

        assertFailsWith(LifecycleException::class) {
            subject.takeWhileIn(lifecycle, DESTROYED).subscribe()
        }
    }

    @Test
    fun lifecycleHost_until() {
        val subject = PublishSubject.create<Int>()
        val attach = ArrayList<Int>()
        val untilAttach = subject.subscribe { attach.add(it) }.until(lifecycle, ATTACH)
        val start = ArrayList<Int>()
        val untilStart = subject.subscribe { start.add(it) }.until(lifecycle, START)
        val resume = ArrayList<Int>()
        val untilResume = subject.subscribe { resume.add(it) }.until(lifecycle, RESUME)
        val pause = ArrayList<Int>()
        val untilPause = subject.subscribe { pause.add(it) }.until(lifecycle, PAUSE)
        val stop = ArrayList<Int>()
        val untilStop = subject.subscribe { stop.add(it) }.until(lifecycle, STOP)
        val detach = ArrayList<Int>()
        val untilDetach = subject.subscribe { detach.add(it) }.until(lifecycle, DETACH)
        val destroy = ArrayList<Int>()
        val untilDestroy = subject.subscribe { destroy.add(it) }.until(lifecycle, DESTROY)
        subject.onNext(1)
        lifecycle.state = ATTACHED
        subject.onNext(2)
        assertTrue(untilAttach.isUnsubscribed)
        assertEquals(listOf(1), attach)
        lifecycle.state = STARTED
        subject.onNext(3)
        assertTrue(untilStart.isUnsubscribed)
        assertEquals(listOf(1, 2), start)
        lifecycle.state = RESUMED
        subject.onNext(4)
        assertTrue(untilResume.isUnsubscribed)
        assertEquals(listOf(1, 2, 3), resume)
        lifecycle.state = STARTED
        subject.onNext(5)
        assertTrue(untilPause.isUnsubscribed)
        assertEquals(listOf(1, 2, 3, 4), pause)
        lifecycle.state = ATTACHED
        subject.onNext(6)
        assertTrue(untilStop.isUnsubscribed)
        assertEquals(listOf(1, 2, 3, 4, 5), stop)
        lifecycle.state = ALIVE
        subject.onNext(7)
        assertTrue(untilDetach.isUnsubscribed)
        assertEquals(listOf(1, 2, 3, 4, 5, 6), detach)
        lifecycle.state = DESTROYED
        subject.onNext(8)
        assertTrue(untilDestroy.isUnsubscribed)
        assertEquals(listOf(1, 2, 3, 4, 5, 6, 7), destroy)
    }

    @Test
    fun lifecycleHost_whileIn() {
        val subject = PublishSubject.create<Int>()
        val alive = ArrayList<Int>()
        val attached = ArrayList<Int>()
        val started = ArrayList<Int>()
        val resumed = ArrayList<Int>()
        val whileAlive = subject.whileIn(lifecycle, ALIVE).subscribe { alive.add(it) }
        subject.onNext(1)
        lifecycle.state = ATTACHED
        val whileAttached = subject.whileIn(lifecycle, ATTACHED).subscribe { attached.add(it) }
        subject.onNext(2)
        lifecycle.state = STARTED
        val whileStarted = subject.whileIn(lifecycle, STARTED).subscribe { started.add(it) }
        subject.onNext(3)
        lifecycle.state = RESUMED
        val whileResumed = subject.whileIn(lifecycle, RESUMED).subscribe { resumed.add(it) }
        subject.onNext(4)
        lifecycle.state = STARTED
        subject.onNext(5)
        assertTrue(whileResumed!!.isUnsubscribed)
        assertEquals(listOf(4), resumed)
        lifecycle.state = ATTACHED
        subject.onNext(6)
        assertTrue(whileStarted!!.isUnsubscribed)
        assertEquals(listOf(3, 4, 5), started)
        lifecycle.state = ALIVE
        subject.onNext(7)
        assertTrue(whileAttached!!.isUnsubscribed)
        assertEquals(listOf(2, 3, 4, 5, 6), attached)
        lifecycle.state = DESTROYED
        subject.onNext(8)
        assertTrue(whileAlive!!.isUnsubscribed)
        assertEquals(listOf(1, 2, 3, 4, 5, 6, 7), alive)

        //All of them should complete without emitting anything, because the state is wrong
        assertEquals(null, subject.whileIn(lifecycle, ALIVE).subscribe())
        assertEquals(null, subject.whileIn(lifecycle, ATTACHED).subscribe())
        assertEquals(null, subject.whileIn(lifecycle, STARTED).subscribe())
        assertEquals(null, subject.whileIn(lifecycle, RESUMED).subscribe())

        assertFailsWith(LifecycleException::class) {
            subject.whileIn(lifecycle, DESTROYED).subscribe()
        }
    }
}