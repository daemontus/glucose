package com.glucose2.app

import android.app.Activity
import android.view.ViewGroup
import com.glucose2.app.event.EventHostDelegate
import com.glucose2.app.group.SingletonGroup
import com.glucose2.app.transaction.TransactionHost
import com.glucose2.app.transaction.TransactionHostDelegate

class ComponentHostDelegate internal constructor(
        rootComponent: Class<out Component>,
        rootView: ViewGroup,
        override val activity: Activity,
        override val factory: ComponentFactory,
        private val rootEventHostDelegate: EventHostDelegate
) : SingletonGroup(rootComponent, rootView,
        object : ComponentGroup.Parent {
            override val factory: ComponentFactory = factory
            override fun registerChild(child: Component) {
                child.getEventHostDelegate().attach(rootEventHostDelegate)
            }
            override fun unregisterChild(child: Component) {
                child.getEventHostDelegate().detach()
            }
        }
), ComponentHost, TransactionHost by TransactionHostDelegate() {

    init {
        factory.host = this
    }

    constructor(rootComponent: Class<out Component>, rootView: ViewGroup, activity: Activity)
            : this(rootComponent, rootView, activity, ComponentFactory(), EventHostDelegate())

    fun destroy() {
        factory.onDestroy()
    }

}