package com.glucose2.app

import android.view.View
import com.github.daemontus.Result
import com.github.daemontus.asError
import com.github.daemontus.asOk
import com.glucose2.bundle.booleanBundler
import com.glucose2.bundle.intBundler
import com.glucose2.state.StateNative
import rx.Observable
import java.util.concurrent.atomic.AtomicInteger

// reduce the amount of object allocations per each component by reusing stateless delegates.
internal val ID_DELEGATE = StateNative(View.NO_ID, intBundler)
internal val TRUE_DELEGATE = StateNative(true, booleanBundler)

/**
 * Thrown whenever some code tries to perform an operation not allowed
 * by the lifecycle restrictions, such as accessing state of a component
 * that does not have any bound.
 */
class LifecycleException(message: String) : Exception(message)

internal fun lifecycleError(message: String): Nothing = throw LifecycleException(message)


//Provide a unique ID storage
private val nextViewId = AtomicInteger(1)

/**
 * Creates a new synthetic ID that should be unique compared to all previously generated IDs
 * (assuming an overflow does not occur) and compared to all ID resources.
 */
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

/**
 * Create an observable that will not throw an error, instead it will propagate it in form of
 * result object.
 */
fun <R> Observable<R>.asResult(): Observable<Result<R, Throwable>>
        = this.map { it.asOk<R, Throwable>() }.onErrorReturn { it.asError() }