package com.glucose2.app.event

import com.github.daemontus.glucose.core.BuildConfig
import com.glucose2.app.LifecycleException
import io.reactivex.Observable
import io.reactivex.Observer
import io.reactivex.disposables.Disposable
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.util.concurrent.Semaphore
import java.util.concurrent.TimeUnit
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

@RunWith(RobolectricTestRunner::class)
@Config(constants = BuildConfig::class, sdk = intArrayOf(21))
class EventHostDelegateTest {

    data class A(val tag: String): Action
    data class E(val tag: String): Event
    data class AE(val tag: String): Action, Event

    @Test(timeout = 1000)
    fun t01_local_delivery() {
        val host = EventHostDelegate()

        val actual = HashSet<Any>()
        val barrier = Semaphore(-3)

        val observer = object : Observer<Any> {
            override fun onComplete() { barrier.release() }
            override fun onError(e: Throwable) { throw e }
            override fun onNext(it: Any) { synchronized(actual) { actual.add(it) } }
            override fun onSubscribe(d: Disposable) {}
        }

        host.observeAction<A>().subscribe(observer)
        host.consumeAction<AE>().subscribe(observer)
        host.observeEvent<E>().subscribe(observer)
        host.consumeEvent<AE>().subscribe(observer)

        host.emitAction(A("1"))
        host.emitAction(A("2"))
        host.emitEvent(AE("3"))
        host.emitEvent(E("4"))
        host.emitEvent(E("5"))
        host.emitAction(AE("6"))
        host.emitEvent(E("7"))
        host.emitAction(A("8"))

        Thread.sleep(100)

        host.destroy()

        barrier.acquire()

        val expected = hashSetOf(A("1"), A("2"), AE("3"), E("4"), E("5"), AE("6"), E("7"), A("8"))

        assertEquals(expected, actual)

    }

    @Test(timeout = 1000)
    fun t02_remote_action_delivery() {
        val parent = EventHostDelegate()
        val child = EventHostDelegate()

        child.attach(parent)

        val actual = HashSet<Any>()
        val barrier = Semaphore(-1)

        val observer = object : Observer<Any> {
            override fun onComplete() { barrier.release() }
            override fun onError(e: Throwable) { throw e }
            override fun onNext(it: Any) { synchronized(actual) { actual.add(it) } }
            override fun onSubscribe(d: Disposable) {}
        }

        child.observeAction<A>().subscribe(observer)
        child.observeAction<AE>().subscribe(observer)

        parent.emitAction(A("1"))
        parent.emitAction(A("2"))
        parent.emitAction(AE("3"))

        val c = parent.consumeAction<AE>().subscribe()

        parent.emitAction(A("4"))
        parent.emitEvent(E("5"))
        parent.emitAction(AE("6"))
        parent.emitAction(A("7"))

        Thread.sleep(100)

        c.dispose()

        child.emitAction(A("8"))
        parent.emitAction(AE("9"))

        Thread.sleep(100)

        child.detach()
        parent.destroy()
        child.destroy()

        barrier.acquire()

        val expected: Set<Any> = hashSetOf(A("1"), A("2"), AE("3"), A("4"), A("7"), A("8"), AE("9"))

        assertEquals(expected, actual)
    }

    @Test(timeout = 1000)
    fun t03_remote_event_delivery() {
        val parent = EventHostDelegate()
        val child = EventHostDelegate()

        child.attach(parent)

        val actual = HashSet<Any>()
        val barrier = Semaphore(-1)

        val observer = object : Observer<Any> {
            override fun onComplete() { barrier.release() }
            override fun onError(e: Throwable) { throw e }
            override fun onNext(it: Any) { synchronized(actual) { actual.add(it) } }
            override fun onSubscribe(d: Disposable) {}
        }

        parent.observeEvent<E>().subscribe(observer)
        parent.observeEvent<AE>().subscribe(observer)

        child.emitEvent(E("1"))
        child.emitEvent(E("2"))
        child.emitEvent(AE("3"))

        val c = child.consumeEvent<AE>().subscribe()

        child.emitEvent(E("4"))
        child.emitAction(A("5"))
        child.emitEvent(AE("6"))
        child.emitEvent(E("7"))

        Thread.sleep(100)

        c.dispose()

        parent.emitEvent(E("8"))
        child.emitEvent(AE("9"))

        Thread.sleep(100)

        child.detach()
        parent.destroy()
        child.destroy()

        barrier.acquire()

        val expected: Set<Any> = hashSetOf(E("1"), E("2"), AE("3"), E("4"), E("7"), E("8"), AE("9"))

        assertEquals(expected, actual)
    }

    @Test
    fun t04_reattaching() {
        val child = EventHostDelegate()
        val parent = EventHostDelegate()

        child.attach(parent)

        assertFailsWith<LifecycleException> {
            child.attach(parent)
        }

        child.destroy()
        parent.destroy()
    }

    @Test
    fun t05_empty_detach() {
        val host = EventHostDelegate()
        host.detach()
    }

    @Test
    fun t06_complex_test() {
        val child11 = EventHostDelegate()
        val child12 = EventHostDelegate()
        val child2 = EventHostDelegate()
        val parent = EventHostDelegate()

        child11.attach(parent)
        child12.attach(parent)
        child2.attach(child11)

        val actualParent = HashSet<Any>()
        val actual11 = HashSet<Any>()
        val actual12 = HashSet<Any>()
        val actual2 = HashSet<Any>()

        // tunnel through parent
        parent.consumeEvent<AE>().subscribe { parent.emitAction(it) }

        // tunnel through child11
        child11.consumeEvent<AE>().subscribe { child11.emitAction(it) }


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

        child2.emitEvent(E("1"))
        child11.emitEvent(E("2"))
        child12.emitEvent(E("3"))
        parent.emitEvent(E("4"))

        parent.emitAction(A("5"))
        child11.emitAction(A("6"))
        child12.emitAction(A("7"))
        child2.emitAction(A("8"))

        // try tunnelling
        child12.emitEvent(AE("9"))
        child2.emitEvent(AE("10"))

        Thread.sleep(200)

        child2.detach()
        child11.detach()
        child12.detach()

        child2.destroy()
        child11.destroy()
        child12.destroy()
        parent.destroy()

        assertEquals(setOf(E("1"), E("2"), E("3"), E("4"), A("5"), AE("9")), actualParent)
        assertEquals(setOf(E("1"), E("2"), A("5"), A("6"), AE("9"), AE("10")), actual11)
        assertEquals(setOf(E("3"), A("5"), A("7"), AE("9")), actual12)
        assertEquals(setOf(E("1"), A("5"), A("6"), A("8"), AE("9"), AE("10")), actual2)

    }

    @Test
    fun t07_scheduler() {
        var value = 0

        Observable.just(5)
                .observeOn(EventScheduler)
                .delay(10, TimeUnit.MILLISECONDS)
                .subscribe { value = it }

        Thread.sleep(100)

        assertEquals(5, value)
    }

}