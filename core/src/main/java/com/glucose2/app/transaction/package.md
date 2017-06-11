# Package com.glucose2.app.transaction

## Transactions

To perform long running atomic operations on UI and component hierarchy (such as animated transitions), Glucose provides Transactions.

Transactions behave similar to **critical sections** - only one transaction can run at a time and all transactions run by default on the same (main) thread. However, compared to normal critical sections, transactions are interruptible and can change threads. This is implemented by means of Observables. Each transaction is represented by an [Observable] and it is considered to be running when it is subscribed (the onComplete or onError has not been called yet).

This way, you can implement a transaction which starts by showing a progress bar (on the main thread), then proceeds to download some data (on the IO thread) and as soon as the data is loaded, proceeds to render it (again on the main thread). The transaction mechanism ensures that this transaction will **start only after all previous transactions are finished** and during the whole transaction, **no other transaction will be running**.
 
As you can see, the main thread is free during most of the transaction to handle other important tasks, such as lifecycle or animations and yet no other transaction can be executed concurrently. This means you can easily implement **mutually exclusive, long running chains of asynchronous actions**.
 
 ### Transaction lifecycle
 
To start a transaction, one has to submit it to a [TransactionHost]. Usually, only one transaction host is present in the component tree and it is implemented in the root (i.e. the [ComponentHost]). All child components then delegate their transactions to this one host. Creation of new transaction hosts (separate from a component tree) is currently not supported, but it can be implemented in the future.
 
User submits the observable representing the transaction directly to the [TransactionHost]. The transaction host will then return a proxy observable, which mimics the behaviour of the transaction observable and is used to start and monitor the transaction. Once the transaction is submitted, **it isn't queued for execution immediately**. This happens only when the proxy observable is **first subscribed**, which means that the order in which transactions are executed does not depend on the order in which they are submitted, but on the order in which their proxy observables are subscribed to. Furthermore, each proxy observable behaves like a **replay** observable, i.e. the transaction is executed only when first subscribed and all subsequent subscriptions receive a copy of the first transaction results.
 
If there is no other executing transaction at the time of proxy subscription, the transaction observable is subscribed immediately (and the transaction starts executing). However, if there is an active transaction and a potential queue of previous pending transactions, the new transaction observable is placed to the back of the queue. Once all previous transactions are finished, the transaction observable is subscribed and starts emitting items via the proxy observable. Once the onCompleted or onError is called, the transaction is considered finished and next transaction can start executing.

Finally, one can **cancel** any transaction merely by unsubscribing from the proxy observable. If the transaction has not been started yet, it is simply removed from the queue. If it is already running, the transaction observable is also unsubscribed and next transaction available is executed.

#### Premature termination and refused transactions
  
In some cases, the TransactionHost can decide to **refuse a transaction**. In such case [CannotExecuteException] is observed by the proxy observable once subscribed. This can happen either when the TransactionHost is not in a correct state (for example after onDestroy) or when the transaction queue is too big. In order to prevent congestion, TransactionHost will usually enforce an upper bound on the size of the transaction queue. 
 
Similarly, when the TransactionHost is being destroyed with a non empty transaction queue, all transactions which haven't started executing yet will be notified using a [CannotExecuteException]. Finally, the active transaction observable is also **unsubscribed** and a [PrematureTerminationException] is observed via the proxy observable.
   
Note that the [CannotExecuteException] usually does not require any further action, since the transaction haven't even started yet. However, the [PrematureTerminationException] can warrant some action, because the unfinished transaction can leave the system in an inconsistent state. Usually, the UI is not a problem, since the exception is thrown when the Activity itself is shutting down and the UI is therefore also destroyed. However, one might need to clean up other types of resources, such as database connections.

Note that you can and should prevent these types of errors by unsubscribing transactions on your own (either manually or by binding them to the lifecycle).