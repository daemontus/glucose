package com.glucose2.rx

import io.reactivex.Observable

/**
 * Create an observable which will emit onComplete event as soon as any of the given
 * binders enters inactive state. If, at the time of subscription, any of the binders
 * is inactive, the onComplete should be invoked immediately.
 *
 * Note that the observable will still be subscribed.
 * If you need to also avoid subscription, see [subscribeWhile].
 */
fun <T> Observable<T>.observeWhile(vararg binders: ObservableBinder): Observable<T> {
    return binders.fold(this) { a, b -> b.addObservable(a) }
}

/**
 * Create a [DisposeProxy] which ensures that a subscription is created only
 * if all given binders are in the active state. Furthermore, as soon as any binder
 * enters inactive state, the subscription is unsubscribed.
 *
 * Note that this will not subscribe the observable, if at the time of subscription
 * any binder is inactive. If you wish to terminate the observable using the
 * onCompleted event instead, see [observeWhile].
 */
fun <T> Observable<T>.subscribeWhile(vararg binders: ObservableBinder): DisposeProxy<T> {
    return DisposeProxy(this, binders)
}