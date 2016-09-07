package com.github.daemontus.glucose.demo.presentation.util

import android.view.ViewPropertyAnimator
import rx.Observable
import rx.subjects.UnicastSubject

object Duration {
    val ENTER_FAST = 115L
    val ENTER = 180L    //Note: Material design says 225, but that's incredibly slow!
    val LEAVE = 195L
    val LEAVE_FAST = 100L
    val SWAP = 200L
    val COMPLEX = 375L
}


fun <T> Observable<T>.finishAnimation(animator: ViewPropertyAnimator): Observable<T> {
    return this.delay {
        val proxy = UnicastSubject.create<Unit>()
        animator.withEndAction {
            proxy.onNext(Unit)
        }
        proxy
    }
}