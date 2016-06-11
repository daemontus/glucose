package com.github.daemontus.glucose.blueprints.view

/*
open class FrameGroupBlueprint<
        FinalView: FrameLayout,
        Parent: LayoutBlueprint,
        Component: UIComponent<*,*,*>
>(
        layout: Parent, constructor: (Context) -> FinalView
) : ViewGroupBlueprint<FinalView, Parent, FrameLayoutBlueprint, Component>(layout, constructor, ::FrameLayoutBlueprint) {
}

// DSL

fun <
        Child: LayoutBlueprint,
        Component: UIComponent<*,*,*>
        > ViewGroupBlueprint<*, *, Child, Component>.frameLayout(
        initializer: FrameGroupBlueprint<FrameLayout, Child, Component>.() -> Unit
): FrameGroupBlueprint<FrameLayout, Child, Component> {
    return this.child(initializer, FrameGroupBlueprint<FrameLayout, Child, Component>(this.createLayoutBlueprint(), ::FrameLayout))
}

fun <C: UIComponent<*,*,*>> frameLayout(
        layout: LayoutBlueprint = LayoutBlueprint(),
        initializer: FrameGroupBlueprint<FrameLayout, LayoutBlueprint, C>.() -> Unit
): FrameGroupBlueprint<FrameLayout, LayoutBlueprint, C> {
    val blueprint = FrameGroupBlueprint<FrameLayout, LayoutBlueprint, C>(layout, ::FrameLayout)
    blueprint.initializer()
    return blueprint
}

fun <C: UIComponent<*,*,*>> simpleFrameLayout(
        initializer: FrameGroupBlueprint<FrameLayout, LayoutBlueprint, C>.() -> Unit
): FrameGroupBlueprint<FrameLayout, LayoutBlueprint, C> {
    val blueprint = FrameGroupBlueprint<FrameLayout, LayoutBlueprint, C>(LayoutBlueprint(), ::FrameLayout)
    blueprint.initializer()
    return blueprint
}

//val f = ::simpleFrameLayout

fun <C: UIComponent<*,*,*>> safe(): (FrameGroupBlueprint<FrameLayout, LayoutBlueprint, C>.() -> Unit) -> FrameGroupBlueprint<FrameLayout, LayoutBlueprint, C> = { it -> simpleFrameLayout(it) }

fun <
        Activity: ComponentActivity<Activity>,
        FinalView: View,
        Component: UIComponent<FinalView, Activity, Component>
> ViewBlueprint<FinalView, LayoutBlueprint, Component>.asRenderable(constructor: (FinalView, Activity) -> Component): (Activity) -> Component {
    return {
        val view = this.create(it)
        val component = constructor(view, it)
        this.bind(view, component, it.asRenderContext())
        component
    }
}

fun <
        Root: ComponentActivity<Root>,
        FinalView: View,
        Component: UIComponent<FinalView, Root, Component>,
        Blueprint: ViewBlueprint<FinalView, LayoutBlueprint, Component>
> rootComponent(
        constructor: (FinalView, Root) -> Component,
        blueprint: (LayoutBlueprint) -> Blueprint,
        initializer: Blueprint.() -> Unit
): (Root) -> Component {
    return {
        val b = blueprint(LayoutBlueprint())
        b.initializer()
        val view = b.create(it)
        val component = constructor(view, it)
        b.bind(view, component, it.asRenderContext())
        component
    }
}

fun <
        Root: ComponentRoot,
        FinalView: View,
        Component: UIComponent<FinalView, Root, Component>,
        Parent: LayoutBlueprint,
        Blueprint: ViewBlueprint<FinalView, Parent, Component>
> ViewGroupBlueprint<*,*,Parent,*>.uiComponent(
        constructor: (FinalView, Root) -> Component,
        blueprint: (Parent) -> Blueprint,
        initializer: Blueprint.() -> Unit
): Unit {
    val b = blueprint(this.createLayoutBlueprint())
    addChild(blueprint(this.createLayoutBlueprint()))
    b.initializer()
    childComponents[b] = constructor as (View, ComponentRoot) -> UIComponent<*,*,*>
}
        */