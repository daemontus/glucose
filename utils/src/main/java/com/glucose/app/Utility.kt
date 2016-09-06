package com.glucose.app

import android.os.Looper
import com.github.daemontus.egholm.functional.Result
import com.github.daemontus.egholm.functional.error
import com.github.daemontus.egholm.functional.unwrap
import com.github.daemontus.glucose.utils.BuildConfig
import com.glucose.Log
import rx.Observable
import java.util.concurrent.atomic.AtomicInteger

fun <T, E> T.asOk(): Result<T, E> = Result.Ok<T, E>(this)
fun <T, E> E.asError(): Result<T, E> = Result.Error<T, E>(this)

fun <R> Observable<R>.asResult(): Observable<Result<R, Throwable>>
    = this.map { it.asOk<R, Throwable>() }.onErrorReturn { it.asError() }

fun <R> Observable<Result<R, Throwable>>.unwrap(): Observable<R>
    = this.map { when (it) {
        is Result.Ok -> it.ok
        is Result.Error -> throw it.error
    } }

fun actionLog(message: String) {
    if (BuildConfig.DEBUG) {
        Log.d("TRANSITION: $message")
    }
}

fun lifecycleLog(message: String) {
    if (BuildConfig.DEBUG) {
        Log.d("LIFECYCLE: $message")
    }
}

fun Presenter.lifecycleLog(message: String) {
    if (BuildConfig.DEBUG) {
        Log.d("LIFECYCLE[$this]: $message")
    }
}

fun mainThread() = Looper.myLooper() == Looper.getMainLooper()

//Provide a unique ID storage
private val nextViewId = AtomicInteger(1)

fun newSyntheticId(): Int {
    while (true) {  //we have to repeat until the atomic compare passes
        val result = nextViewId.get()
        // aapt-generated IDs have the high byte nonzero; clamp to the range under that.
        var newValue = result + 1
        if (newValue > 0x00FFFFFF) newValue = 1 // Roll over to 1, not 0.
        if (nextViewId.compareAndSet(result, newValue)) {
            return result
        }
    }
}