package com.glucose2.app.event

import android.os.HandlerThread
import rx.Scheduler
import rx.android.schedulers.AndroidSchedulers


/**
 * Special RxJava [Scheduler] which is used to handle all events and actions
 * passed in the component tree. Similar to the [AndroidSchedulers.mainThread], it uses
 * just one looper thread, so events can't be delivered in parallel.
 */
object EventScheduler : Scheduler() {

    // accessible for testing purposes
    internal var thread = HandlerThread("event-scheduler").apply { start() }

    private var looperScheduler = AndroidSchedulers.from(thread.looper)

    override fun createWorker(): Worker = looperScheduler.createWorker()

    override fun now(): Long = looperScheduler.now()

    // also for testing
    internal fun reset() {
        thread = HandlerThread("event-scheduler").apply { start() }
        looperScheduler = AndroidSchedulers.from(thread.looper)
    }

}