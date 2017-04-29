package com.glucose2.app

import com.glucose2.app.transaction.TransactionHost

/**
 * Thrown whenever some code tries to perform an operation not allowed
 * by the lifecycle restrictions, such as accessing state of a component
 * that does not have any bound.
 */
class LifecycleException(message: String) : Exception(message)