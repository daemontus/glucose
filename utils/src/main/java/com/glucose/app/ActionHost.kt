package com.glucose.app

import android.support.annotation.AnyThread
import rx.Observable
import rx.exceptions.MissingBackpressureException


/**
 * Actions provide a way to serialize any long running, asynchronous execution
 * related to a specific [ActionHost].
 *
 * Using actions, one can prevent interleaving of [Presenter] tree transformations
 * or other complex UI operations which might otherwise result in unexpected or
 * undefined behavior.
 *
 * Action is represented by any generic Action [Observable]. Instead of directly subscribing
 * to the Action, it is posted to an [ActionHost] which returns a Proxy [Observable]
 * mimicking the behavior of the original Action.
 *
 * When the Proxy [Observable] is subscribed to, the Action [Observable] becomes queued.
 * If there are no active (not terminated) actions, [ActionHost] immediately subscribes
 * to this Action. Otherwise it will wait for the previously queued actions to
 * terminate and then subscribes to this Action.
 *
 * The Proxy [Observable] should then exactly mirror the behavior of the Action [Observable]
 * with respect to the delay caused by the unfinished actions. That is including errors
 * and unsubscribe events.
 *
 * It is left to the
 * implementation as to how multiple subscriptions to the Proxy [Observable] are handled.
 * However, each [ActionHost] implementation should make sure only one action is active
 * at a time. (So either the results need to be cached, or the action needs to be queued
 * again upon each subscribe)
 *
 * Note that [ActionHost] can choose to refuse any action or terminate it prematurely. In such
 * cases, the proxy observable will be notified with an appropriate exception.
 * This will usually happen when:
 *  - The [ActionHost] has to shut down due to external reasons (such as application shutting down).
 * This will should result in [PrematureTerminationException] if action has been already running or
 * [CannotExecuteException] if the action hasn't been subscribed to yet.
 *  - The number of queued actions exceeds maximal supported amount. This should result in the
 *  [CannotExecuteException].
 * (This is not a complete list, take it more as an example. The actual conditions and errors
 * depend on implementation)
 *
 * Hint: If you need to post a big amount of actions at once, concatenate them and post
 * them as one action to prevent backpressure problems.
 *
 * Scheduling: The interface contract does not enforce any particular scheduler for
 * action execution, however the implementations can modify this behavior.
 *
 * TODO: Can we somehow detect deadlocks? (Posting proxy as an action sounds like an easy mistake)
 * TODO: Add picture.
 */
interface ActionHost {

    /**
     * Return a Proxy [Observable] that will upon subscription ensure that action
     * is properly queued and executed if possible.
     */
    @AnyThread
    fun <R> post(action: Observable<R>): Observable<R>

}

/**
 * Thrown by [ActionHost] if an action has to be terminated prematurely due to external reasons.
 */
class PrematureTerminationException : RuntimeException {
    constructor(message: String) : super(message)
    constructor(message: String, cause: Throwable) : super(message, cause)
}

/**
 * Thrown by [ActionHost] if an action cannot be executed for whatever reason.
 */
class CannotExecuteException : RuntimeException {
    constructor(message: String) : super(message)
    constructor(message: String, cause: Throwable) : super(message, cause)
}

// Some helper functions to simplify work with ActionHosts

/**
 * Post this observable as an action to the [ActionHost].
 *
 * @see [ActionHost.post]
 */
fun <R> Observable<R>.post(host: ActionHost): Observable<R> = host.post(this)

/**
 * Use [ActionHost] as an [Observable.Transformer] (useful for Java interop).
 */
fun <R> ActionHost.asTransformer(): Observable.Transformer<R, R>
        = Observable.Transformer<R, R> { t -> this@asTransformer.post(t) }