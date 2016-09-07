package com.glucose.app.presenter

/**
 * Thrown in case of lifecycle inconsistencies.
 */
open class LifecycleException : RuntimeException {
    constructor() : super()
    constructor(message: String?) : super(message)
    constructor(message: String?, cause: Throwable?) : super(message, cause)
    constructor(cause: Throwable?) : super(cause)
}