package com.glucose2.app.transaction

import com.github.daemontus.glucose.core.BuildConfig
import com.glucose2.app.asResult
import io.reactivex.Observable
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.util.concurrent.TimeUnit
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

@RunWith(RobolectricTestRunner::class)
@Config(constants = BuildConfig::class, sdk = intArrayOf(21))
class TransactionHostDelegateTest {

    @Test
    fun t01_dry_run() {
        val host = TransactionHostDelegate()

        host.destroy()
    }

    @Test
    fun t02_simple_transactions() {
        val host = TransactionHostDelegate()

        val result1 = host.submit(Observable.just(5)).blockingFirst()
        assertEquals(5, result1)

        val result2 = host.submit(Observable.just("foo")).blockingFirst()
        assertEquals("foo", result2)

        host.destroy()
    }

    @Test
    fun t03_interleaving() {
        val host = TransactionHostDelegate()

        var value = 0

        fun expect(e: Int): (Int) -> Int = {
            assertEquals(e, value)
            value = it
            it + 1
        }

        val t1 = Observable.just(1)
                .map(expect(0))
                .delay(50, TimeUnit.MILLISECONDS)
                .map(expect(1))

        val t2 = Observable.just(3)
                .map(expect(2))
                .delay(50, TimeUnit.MILLISECONDS)
                .map(expect(3))

        val r1 = host.submit(t1)
        val r2 = host.submit(t2)

        r1.blockingFirst()
        r2.blockingFirst()

        host.destroy()

        assertEquals(4, value)
    }

    @Test
    fun t04_transaction_limit() {
        val host = TransactionHostDelegate()

        repeat(1001) {
            // create congestion, ignoring errors...
            host.submit(Observable.timer(1, TimeUnit.DAYS)).asResult().subscribe()
        }

        assertFailsWith<CannotExecuteException> {
            host.submit(Observable.just(1)).blockingFirst()
        }

        host.destroy()
    }

}