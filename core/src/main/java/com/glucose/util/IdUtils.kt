package com.glucose.util

import java.util.concurrent.atomic.AtomicInteger


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