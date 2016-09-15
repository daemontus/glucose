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
        lifecycle.mState = state
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
            lifecycle.mState = ALIVE
        }
        //Alive -> Started
        assertFailsWith(LifecycleException::class) {
            lifecycle.mState = STARTED
        }
        //Alive -> Resumed
        assertFailsWith(LifecycleException::class) {
            lifecycle.mState = RESUMED
        }
        //Alive -> Attached
        lifecycle.mState = ATTACHED
        //Attached -> Attached
        assertFailsWith(LifecycleException::class) {
            lifecycle.mState = ATTACHED
        }
        //Attached -> Resumed
        assertFailsWith(LifecycleException::class) {
            lifecycle.mState = RESUMED
        }
        //Attached -> Destroyed
        assertFailsWith(LifecycleException::class) {
            lifecycle.mState = DESTROYED
        }
        //Attached -> Started
        lifecycle.mState = STARTED
        //Started -> Started
        assertFailsWith(LifecycleException::class) {
            lifecycle.mState = STARTED
        }
        //Started -> Destroyed
        assertFailsWith(LifecycleException::class) {
            lifecycle.mState = DESTROYED
        }
        //Started -> Alive
        assertFailsWith(LifecycleException::class) {
            lifecycle.mState = ALIVE
        }
        //Started -> Resumed
        lifecycle.mState = RESUMED
        //Resumed -> Resumed
        assertFailsWith(LifecycleException::class) {
            lifecycle.mState = RESUMED
        }
        //Resumed -> Destroyed
        assertFailsWith(LifecycleException::class) {
            lifecycle.mState = DESTROYED
        }
        //Resumed -> Alive
        assertFailsWith(LifecycleException::class) {
            lifecycle.mState = ALIVE
        }
        //Resumed -> Attached
        assertFailsWith(LifecycleException::class) {
            lifecycle.mState = ATTACHED
        }
        //Resumed -> Started
        lifecycle.mState = STARTED
        //Started -> Attached
        lifecycle.mState = ATTACHED
        //Attached -> Alive
        lifecycle.mState = ALIVE
        //Alive -> Destroyed
        lifecycle.mState = DESTROYED
        //Destroyed -> Alive
        assertFailsWith(LifecycleException::class) {
            lifecycle.mState = ALIVE
        }
        //Destroyed -> Attached
        assertFailsWith(LifecycleException::class) {
            lifecycle.mState = ATTACHED
        }
        //Destroyed -> Started
        assertFailsWith(LifecycleException::class) {
            lifecycle.mState = STARTED
        }
        //Destroyed -> Resumed
        assertFailsWith(LifecycleException::class) {
            lifecycle.mState = RESUMED
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
        lifecycle.mState = ATTACHED
        assertTrue(attached)
        assertFalse(lifecycle.removeEventCallback(ATTACH, attach))
        lifecycle.mState = STARTED
        assertTrue(started)
        assertFalse(lifecycle.removeEventCallback(START, start))
        lifecycle.mState = RESUMED
        assertTrue(resumed)
        assertFalse(lifecycle.removeEventCallback(RESUME, resume))
        lifecycle.mState = STARTED
        assertTrue(paused)
        assertFalse(lifecycle.removeEventCallback(PAUSE, pause))
        lifecycle.mState = RESUMED
        lifecycle.mState = STARTED
        lifecycle.mState = ATTACHED
        assertTrue(stopped)
        assertFalse(lifecycle.removeEventCallback(STOP, stop))
        lifecycle.mState = STARTED
        lifecycle.mState = ATTACHED
        lifecycle.mState = ALIVE
        assertTrue(detached)
        assertFalse(lifecycle.removeEventCallback(DETACH, detach))
        lifecycle.mState = ATTACHED
        lifecycle.mState = ALIVE
        lifecycle.mState = DESTROYED
        assertTrue(destroyed)
        assertFalse(lifecycle.removeEventCallback(DESTROY, destroy))
    }

    @Test
    fun lifecycleDelegate_lifecycleNotifications() {
        val notificationLog = lifecycle.lifecycleEvents.replay()
        notificationLog.connect()
        lifecycle.mState = ATTACHED
        lifecycle.mState = STARTED
        lifecycle.mState = RESUMED
        lifecycle.mState = STARTED
        lifecycle.mState = ATTACHED
        lifecycle.mState = STARTED
        lifecycle.mState = RESUMED
        lifecycle.mState = STARTED
        lifecycle.mState = ATTACHED
        lifecycle.mState = ALIVE
        lifecycle.mState = DESTROYED
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
        lifecycle.mState = ATTACHED
        subject.onNext(2)
        assertEquals(listOf(1), untilAttach.toListInternal())
        lifecycle.mState = STARTED
        subject.onNext(3)
        assertEquals(listOf(1, 2), untilStart.toListInternal())
        lifecycle.mState = RESUMED
        subject.onNext(4)
        assertEquals(listOf(1, 2, 3), untilResume.toListInternal())
        lifecycle.mState = STARTED
        subject.onNext(5)
        assertEquals(listOf(1, 2, 3, 4), untilPause.toListInternal())
        lifecycle.mState = ATTACHED
        subject.onNext(6)
        assertEquals(listOf(1, 2, 3, 4, 5), untilStop.toListInternal())
        lifecycle.mState = ALIVE
        subject.onNext(7)
        assertEquals(listOf(1, 2, 3, 4, 5, 6), untilDetach.toListInternal())
        lifecycle.mState = DESTROYED
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
        lifecycle.mState = ATTACHED
        val attached = whileAttached.replay()
        attached.connect()
        subject.onNext(2)
        lifecycle.mState = STARTED
        val started = whileStarted.replay()
        started.connect()
        subject.onNext(3)
        lifecycle.mState = RESUMED
        val resumed = whileResumed.replay()
        resumed.connect()
        subject.onNext(4)
        lifecycle.mState = STARTED
        subject.onNext(5)
        assertEquals(listOf(4), resumed.toListInternal())
        lifecycle.mState = ATTACHED
        subject.onNext(6)
        assertEquals(listOf(3, 4, 5), started.toListInternal())
        lifecycle.mState = ALIVE
        subject.onNext(7)
        assertEquals(listOf(2, 3, 4, 5, 6), attached.toListInternal())
        lifecycle.mState = DESTROYED
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
        lifecycle.mState = ATTACHED
        subject.onNext(2)
        assertTrue(untilAttach.isUnsubscribed)
        assertEquals(listOf(1), attach)
        lifecycle.mState = STARTED
        subject.onNext(3)
        assertTrue(untilStart.isUnsubscribed)
        assertEquals(listOf(1, 2), start)
        lifecycle.mState = RESUMED
        subject.onNext(4)
        assertTrue(untilResume.isUnsubscribed)
        assertEquals(listOf(1, 2, 3), resume)
        lifecycle.mState = STARTED
        subject.onNext(5)
        assertTrue(untilPause.isUnsubscribed)
        assertEquals(listOf(1, 2, 3, 4), pause)
        lifecycle.mState = ATTACHED
        subject.onNext(6)
        assertTrue(untilStop.isUnsubscribed)
        assertEquals(listOf(1, 2, 3, 4, 5), stop)
        lifecycle.mState = ALIVE
        subject.onNext(7)
        assertTrue(untilDetach.isUnsubscribed)
        assertEquals(listOf(1, 2, 3, 4, 5, 6), detach)
        lifecycle.mState = DESTROYED
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
        lifecycle.mState = ATTACHED
        val whileAttached = subject.whileIn(lifecycle, ATTACHED).subscribe { attached.add(it) }
        subject.onNext(2)
        lifecycle.mState = STARTED
        val whileStarted = subject.whileIn(lifecycle, STARTED).subscribe { started.add(it) }
        subject.onNext(3)
        lifecycle.mState = RESUMED
        val whileResumed = subject.whileIn(lifecycle, RESUMED).subscribe { resumed.add(it) }
        subject.onNext(4)
        lifecycle.mState = STARTED
        subject.onNext(5)
        assertTrue(whileResumed!!.isUnsubscribed)
        assertEquals(listOf(4), resumed)
        lifecycle.mState = ATTACHED
        subject.onNext(6)
        assertTrue(whileStarted!!.isUnsubscribed)
        assertEquals(listOf(3, 4, 5), started)
        lifecycle.mState = ALIVE
        subject.onNext(7)
        assertTrue(whileAttached!!.isUnsubscribed)
        assertEquals(listOf(2, 3, 4, 5, 6), attached)
        lifecycle.mState = DESTROYED
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