package com.glucose2.app.event

import android.os.Looper
import rx.Scheduler
import rx.android.schedulers.AndroidSchedulers
import java.util.concurrent.atomic.AtomicReference


/**
 * Special RxJava [Scheduler] which is used to handle all events and actions
 * passed in the component tree. Similar to the [AndroidSchedulers.mainThread], it uses
 * just one looper thread, so events can't be delivered in parallel.
 */
object EventScheduler : Scheduler() {

    private val looperScheduler = run {
        val looper = AtomicReference<Looper?>(null)
        Thread {
            Looper.prepare()
            looper.set(Looper.myLooper())
            Looper.loop()
        }
        while (looper.get() == null) { /* do nothing */ }
        AndroidSchedulers.from(looper.get()!!)
    }

    override fun createWorker(): Worker = looperScheduler.createWorker()

    override fun now(): Long = looperScheduler.now()
}