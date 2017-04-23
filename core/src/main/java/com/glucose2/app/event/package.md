# Package com.glucose2.app.event

## Events and Actions

In order to facilitate communication between [Component]s, each component provides an **event bus**. However, contrary to other common event bus implementations, events in Glucose are delivered in a **structured** manner (following the component tree) and fall into two categories. Specifically, we talk about Events (flow upwards) and Actions (flow downwards).

 - [Event] is emitted when the component tree needs to be updated on a **higher** level than what is managed by the emitting component. Example: User clicked a menu item - the menu component emits an event notifying the navigation component that requested content should be displayed.
 - [Action] is emitted when the component tree needs to be updated on a **lower** level than what is managed by emitting component. Example: A new data is received from the server. The component that received the data sends an action to all of its children so that they can update their content if necessary.
    
Note that both events and actions can be emitted at any level in the tree, not just leaves or root. After emitting, each event is first observed by the emitter itself, then by emitters parent, moving on to the parents parent and so on, until it reaches the hierarchy root. Similarly, each action is first delivered to the emitting component, then to all of the components children and then to their children util it reaches tree leaves.

### Observing and Consuming

To ensure proper component encapsulation, both events and actions can be either **observed or consumed** by the component. Observed events are not affected in any way by the component and are immediately sent to the next receiving component (parent for Events, children for Actions).

However, a component can choose to consume certain kind of events. In such case, these are not passed any further up or down the tree. This behaviour gives you the ability to create component trees that use events(actions) internally, without affecting the rest of the tree, such as various forms of nested navigation.

Note that actions are always delivered to all children or not at all. If A emits action to B and C, C will receive the action regardless of whether B consumes it or not. However, if D and E are children of B, they won't receive the action, since B consumed it. For events, the situation is simpler, since there is always just one parent.
 
### Threading and Synchronization

Events (and actions) are always delivered on a special [EventScheduler] which does not use the main thread. Hence it is your responsibility to move the work to an appropriate scheduler if necessary.
   
While this ensures that events don't block the main thread (and generally reduces the problem of long event chains), it also means that there are **no hard guarantees on event delivery** when component tree changes rapidly. For example, if you emit an action and then quickly attach a new component, it might happen that this component also observes the action, because the EventScheduler was invoked only after the attach occurred (such situations should be very rare, however, they are technically possible). Similar problems can occur when Activity is abruptly recreated, since the event bus is not parcelable. This is intentional, since the point of event bus is to have a fast and simple service, even if it means sacrificing a little reliability. If you are looking for a more robust alternative to event bus, have a look at [Transactions]. 

This makes events (or actions) a good mechanism for delivery of user generated events or small UI updates (if a 
button click is lost because activity was killed, it wasn't probably that important to begin with), 
but unsuitable for operations that absolutely need to be handled correctly.

### Tree tunneling

As you might have noticed, two components that are not in each others subtrees (essentially siblings) cannot communicate using the event bus unless there is a common parent that will turn events from these components into actions and send them back down the tree. We call this tree tunneling, because components A and B use common parent C as a "tunnel" through which they communicate. This is the recommended way of sibling communication, because it ensures proper encapsulation of each component. Each component should be designed to work in a reasonable manner regardless of the state of the other components in the tree.