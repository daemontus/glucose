package com.github.daemontus.glucose.blueprints.component


/*

open class UIComponent<RootView: View, Root: ComponentRoot>(
        blueprint: ViewBlueprint<RootView, *, out UIComponent<RootView, Root>>,
        root: Root
) : Component<Root>(root) {

    private val whileVisible = ArrayList<() -> Subscription>()
    private val active = ArrayList<Subscription>()

    private val childComponents: MutableMap<String, UIComponent<*, out Root>> = HashMap()

    var visible: Boolean = false
        private set

    val rootView = blueprint.create(root.activity)

    init {
        blueprint.bind(rootView, root.activity.asRenderContext())
    }

    open fun onShow() {
        visible = true
        childComponents.values.forEach {
            it.onShow()
        }
        whileVisible.forEach {
            active.add(it())
        }
    }

    open fun onHide() {
        active.forEach {
            it.unsubscribe()
        }
        active.clear()
        childComponents.values.forEach {
            it.onHide()
        }
        visible = false
    }

    protected fun whileVisible(subscription: () -> Subscription) {
        whileVisible.add(subscription)
        if (visible) active.add(subscription())
    }

    override fun addChild(key: String, component: Component<out Root>) {
        super.addChild(key, component)
        if (component is UIComponent<*,*>) {
            childComponents.put(key, component as UIComponent<*, out Root>)
            if (visible) {
                component.onShow()
            }
        }
    }

}*/