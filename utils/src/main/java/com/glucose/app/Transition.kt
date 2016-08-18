package com.glucose.app

/**
 * A base class from which a PresenterGroup transitions should inherit and add functionality to.
 */
open class Transition() {
    fun <R: Any> R.asResult() = TransitionResult(this)
}

/**
 * A result of a transition.
 */
open class TransitionResult<out R: Any>(
        val result: R
)