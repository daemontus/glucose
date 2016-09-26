package com.glucose.app.presenter

import com.glucose.util.asResult
import org.junit.Ignore
import org.junit.Test
import rx.Observable
import rx.schedulers.Schedulers
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ActionHostTest {

    private val actions = ActionDelegate(Schedulers.from(Executors.newSingleThreadExecutor()), 4)

    @Test
    fun actionDelegate_emptyRun() {
        actions.startProcessingActions()
        actions.stopProcessingActions()
    }

    @Test
    fun actionDelegate_postOnInactive() {
        assertFailsWith(CannotExecuteException::class) {
            actions.post(Observable.empty<Unit>())
                    .toBlocking().first()
        }
        actions.startProcessingActions()
        actions.stopProcessingActions()
        assertFailsWith(CannotExecuteException::class) {
            actions.post(Observable.empty<Unit>())
                    .toBlocking().first()
        }
    }

    @Test
    fun actionDelegate_doubleActivate() {
        actions.startProcessingActions()
        assertFailsWith(IllegalStateException::class) {
            actions.startProcessingActions()
        }
        actions.stopProcessingActions()
    }

    @Test
    fun actionDelegate_prematureStop() {
        assertFailsWith(IllegalStateException::class) {
            actions.stopProcessingActions()
        }
    }

    @Test
    fun actionDelegate_postButNotExecute() {
        actions.startProcessingActions()
        actions.post(Observable.just(10))
        actions.stopProcessingActions()
    }

    @Test
    fun actionDelegate_prematureTermination() {
        kotlin.repeat(10) {
            actions.startProcessingActions()
            //Post
            val result = actions.post(Observable
                    .just(1)
                    .delay(1000, TimeUnit.MILLISECONDS)
            )
            val result2 = actions.post(Observable.just(2))
            //Enqueue
            result.asResult().subscribe()
            result2.asResult().subscribe()
            //Kill
            Thread.sleep(20)   //Haven't found a better way to make sure
            //both actions are in the queue by the time the host shuts down.
            actions.stopProcessingActions()
            //Check
            assertFailsWith(PrematureTerminationException::class) {
                result.toBlocking().first()
            }
            assertFailsWith(CannotExecuteException::class) {
                result2.toBlocking().first()
            }
        }
    }

    @Test
    fun actionDelegate_multipleActions() {
        kotlin.repeat(10) {
            actions.startProcessingActions()
            val r1 = actions.post(Observable.just(1).delay(10, TimeUnit.MILLISECONDS))
            val r2 = actions.post(Observable.just(2).delay(10, TimeUnit.MILLISECONDS))
            val r3 = actions.post(Observable.just(3).delay(10, TimeUnit.MILLISECONDS))
            val r4 = actions.post(Observable.just(4).delay(10, TimeUnit.MILLISECONDS))
            r1.asResult().subscribe()
            r2.asResult().subscribe()
            r3.asResult().subscribe()
            r4.asResult().subscribe()
            assertEquals(1, r1.toBlocking().first())
            assertEquals(2, r2.toBlocking().first())
            assertEquals(3, r3.toBlocking().first())
            assertEquals(4, r4.toBlocking().first())
            actions.stopProcessingActions()
        }
    }

    @Test
    fun actionDelegate_mutualExclusion() {
        kotlin.repeat(10) {
            var counter = 0
            actions.startProcessingActions()
            actions.post(Observable.just(Unit)
                    .doOnNext { synchronized(it) { counter += 1 } }
                    .delay(20, TimeUnit.MILLISECONDS)
                    .doOnNext { synchronized(it) { counter += 1 } }
                    .doOnTerminate { synchronized(Unit) { counter += 1 } }
            ).subscribe()
            actions.post(Observable.just(Unit).doOnNext {
                synchronized(it) { assertEquals(3, counter) }
            }).toBlocking().first()
            actions.stopProcessingActions()
        }
    }

    @Test
    fun actionDelegate_removeWhileActive() {
        kotlin.repeat(100) {
            actions.startProcessingActions()
            val sub = actions.post(Observable.just(Unit)
                    .delay(1000, TimeUnit.DAYS))
                    .subscribe()
            val r2 = actions.post(Observable.just(Unit))
            r2.subscribe()
            sub.unsubscribe()
            r2.toBlocking().last()  //last ensures the action completely finishes before shutdown
            actions.stopProcessingActions()
        }
    }


    @Ignore
    @Test
    fun actionDelegate_removeInQueue() {
        //TODO: This may fail with premature termination somehow
        kotlin.repeat(10) {
            actions.startProcessingActions()
            val a1 = actions.post(Observable.just(Unit)
                    .delay(20, TimeUnit.MILLISECONDS))
            val a2 = actions.post(Observable.just(Unit))
            a1.subscribe()
            val s2 = a2.subscribe()
            s2.unsubscribe()
            //We somehow have to make sure that the termination notification has been delivered
            //for a1 before the stopProcessingActions is called.
            assertFalse(a1.isEmpty.toBlocking().last())
            assertTrue(a2.isEmpty.toBlocking().last())
            actions.stopProcessingActions()
        }
    }

    @Test
    fun actionDelegate_capacityOverflow() {
        kotlin.repeat(100) {
            actions.startProcessingActions()
            val b = actions.post(Observable.just(Unit).delay(10, TimeUnit.DAYS)).subscribe()
            val a1 = actions.post(Observable.just(1)).apply { this.asResult().subscribe() }
            val a2 = actions.post(Observable.just(2)).apply { this.asResult().subscribe() }
            val a3 = actions.post(Observable.just(3)).apply { this.asResult().subscribe() }
            val a4 = actions.post(Observable.just(4)).apply { this.asResult().subscribe() }
            val a5 = actions.post(Observable.just(5)).apply { this.asResult().subscribe() }
            b.unsubscribe()
            a1.toBlocking().last()
            a2.toBlocking().last()
            a3.toBlocking().last()
            a4.toBlocking().last()
            assertFailsWith(CannotExecuteException::class) {
                a5.toBlocking().last()
            }
            actions.stopProcessingActions()
        }
    }

    @Test
    fun actionDelegate_subscriptionSharing() {
        kotlin.repeat(10) {
            actions.startProcessingActions()
            val action = actions.post(Observable.just(Unit).delay(20, TimeUnit.MILLISECONDS))
            val s1 = action.subscribe()
            val s2 = action.subscribe()
            action.subscribe()
            s1.unsubscribe()
            s2.unsubscribe()
            action.toBlocking().last()
            actions.stopProcessingActions()
        }
    }

    @Test
    fun actionHost_invertedCall() {
        actions.startProcessingActions()
        assertEquals(10, Observable.just(10).post(actions).toBlocking().last())
        actions.stopProcessingActions()
    }

    @Test
    fun actionHost_transformer() {
        actions.startProcessingActions()
        assertEquals(10, Observable.just(10).compose(actions.asTransformer()).toBlocking().last())
        actions.stopProcessingActions()
    }

}