package com.glucose2.app.transaction

import com.glucose2.app.asResult
import io.reactivex.Observable
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.UnicastSubject
import java.util.*

/**
 * Reasoning about correctness:
 *  - All private variables and functions (even async callbacks) are synchronized with respect
 *  to the state, so no races should occur.
 *  - Upon start, there can't be any waiting actions, so no one needs to start them.
 *  - Upon action enqueue, an action is started if nothing is running.
 *  - Upon action termination, it is cleaned up and next action is started.
 *  - Upon unsubscribe from active action, it is cleaned up and next action is started.
 *
 *  Future work: Can we implement them without that nasty double replay? (Unicast seems nice,
 *  but does not provide all the API of ReplaySubject)
 */
internal class TransactionHostDelegate : TransactionHost {

    private val maxCapacity = 1000

    // Access to private state is always guarded by lock in this!

    private var destroyed = false

    // we use this weird function type because we loose generics information in the list
    private val pending = LinkedList<Pair<Observable<*>, UnicastSubject<*>>>()
    private var active: Pair<Disposable, UnicastSubject<*>>? = null

    override fun <T> submit(transaction: Observable<T>): Observable<T> {
        val proxy = UnicastSubject.create<T>()

        // We need the observeOn to break the call stack if it's to deep.
        // (f.e. when destroying with full queue)
        return proxy.observeOn(TransactionScheduler).doOnSubscribe {
            // Subject ensures this runs only once for each transaction.
            synchronized(this) {
                if (pending.size >= maxCapacity) {
                    // if we reached capacity, don't add anything, just make proxy an error
                    proxy.onError(CannotExecuteException("Transaction limit ($maxCapacity) reached."))
                } else {
                    pending.add(transaction.doOnEach(proxy).asResult() to proxy)
                    // The subject will buffer items emitted before the proxy is actually ready.
                    tryNextTransaction()
                }
            }
        }.doFinally {
            synchronized(this) {
                // Check if some bad shit didn't go down.
                // Active transaction can be different only if this transaction was refused!
                active?.takeIf {
                    it.second != proxy && proxy.throwable !is CannotExecuteException
                }?.let { active ->
                    throw IllegalStateException("GLUCOSE INTERNAL ERROR: Active transaction " +
                                                "is ${active.second}, but $proxy just finished!")
                }
                // If we got here, transaction a) is disposed b) is completed c) has error.
                // In either case, we just want to mark it as done and move to the next one.
                active = null
                tryNextTransaction()
            }
        }
    }

    private fun tryNextTransaction() {
        synchronized(this) {
            if (active == null && pending.isNotEmpty()) {
                val (transaction, proxy) = pending.remove()
                if (destroyed) {
                    proxy.onError(CannotExecuteException("TransactionHost has been destroyed."))
                } else {
                    active = transaction.subscribe() to proxy
                }
            }
        }
    }

    internal fun destroy() {
        synchronized(this) {
            if (destroyed) throw IllegalStateException("This host is already destroyed.")
            destroyed = true
            active?.second?.onError(
                    PrematureTerminationException("TransactionHost has been destroyed.")
            )
            // We don't have to deal with pending transactions, they will die in a chain
            // reaction of finally-tryNext calls.
        }
    }


}