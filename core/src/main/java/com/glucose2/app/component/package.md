# Package com.glucose2.app.component

## Components

[Component] is the most basic structural element of a Glucose application. Active components of a running app form a tree with a [ComponentHost] as the root. Each component in the tree is also an [EventHost] which ensures communication between components. Furthermore, each [ComponentHost] implements the [TransactionHost] through which you can perform safe long running operations on the component tree.
  
The lifecycle of a component is very straightforward. Components are [obtain]ed from and managed by the [ComponentHost]. Once the component is created, it can be attached to the active component tree using [attach]. If needed, it can be moved between components using the [reattach] method (this method is particularly useful in combination with the [LifecycleComponent], because it allows you to move the component safely without worrying about the rest of the lifecycle). Eventually, you will want to [detach] the component from the active tree. Finally, once the component is no longer needed, you must [recycle] it, so that is can be reused in the future.
    
As you can see, the [Component] interface is very general. This allows us to create a wide variety of components, ranging from extremely lightweight (ViewHolder) to very rich (Fragment/Activity), each designed with a specific use case in mind. We will discuss such component classes later in this article. However, it also allows us to extend the interface with more functionality. We call these interfaces component extensions. Extensions are the usually implemented using delegates and should be thus as independent as possible. 
  
  ### Component Extensions
  
  #### ComponentGroup
  
  The most basic component
