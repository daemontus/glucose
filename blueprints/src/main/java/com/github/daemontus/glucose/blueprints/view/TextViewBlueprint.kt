package com.github.daemontus.glucose.blueprints.view

/*
open class TextViewBlueprint<
        FinalView: TextView,
        Parent: LayoutBlueprint,
        Component: UIComponent<*, *>
>(
        layout: Parent, constructor: (Context) -> FinalView
) : ViewBlueprint<FinalView, Parent, Component>(layout, constructor) {

    var gravity: ((RenderContext) -> Int)? = null
    var text: ((RenderContext) -> CharSequence)? = null
    var hint: ((RenderContext) -> CharSequence)? = null

    //override fun create(ctx: Context): View = TextView(ctx)

    override fun bind(v: FinalView, c: Component, ctx: RenderContext) {
        super.bind(v, c, ctx)
        gravity?.apply { v.gravity = this(ctx) }
        text?.apply { v.text = this(ctx) }
        hint?.apply { v.hint = this(ctx) }
    }
}

// DSL

fun <
        Group: ViewGroup,
        Child: LayoutBlueprint,
        Component: UIComponent<*,*, *>
        > ViewGroupBlueprint<Group, *, Child, Component>.textView(
        initializer: TextViewBlueprint<TextView, Child, Component>.() -> Unit
): TextViewBlueprint<TextView, Child, Component> {
    return this.child(initializer, TextViewBlueprint<TextView, Child, Component>(this.createLayoutBlueprint(), ::TextView))
}*/