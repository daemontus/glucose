package com.github.daemontus.glucose.blueprints

import android.view.View
import java.util.concurrent.atomic.AtomicInteger

val undefinedId = nextViewId<View>()

class ViewId<V: View>(
        val id: Int
)

//Provide a unique ID storage
private val nextViewId = AtomicInteger(1)

fun <V: View> nextViewId(): ViewId<V> {
    while (true) {  //we have to repeat until the atomic compare passes
        val result = nextViewId.get();
        // aapt-generated IDs have the high byte nonzero; clamp to the range under that.
        var newValue = result + 1;
        if (newValue > 0x00FFFFFF) newValue = 1; // Roll over to 1, not 0.
        if (nextViewId.compareAndSet(result, newValue)) {
            return ViewId(result)
        }
    }
}

fun <V: View> View.findViewById(id: ViewId<V>): V {
    @Suppress("UNCHECKED_CAST")
    return findViewById(id.id) as V
}
