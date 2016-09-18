package com.glucose.app.presenter

/**
 * Thrown in case of lifecycle inconsistencies.
 */
open class LifecycleException : RuntimeException {
    constructor(message: String?) : super(message)
    constructor(message: String?, cause: Throwable?) : super(message, cause)
}