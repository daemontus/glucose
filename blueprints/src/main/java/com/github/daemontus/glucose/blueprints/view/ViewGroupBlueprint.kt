package com.github.daemontus.glucose.blueprints.view

/*
abstract class ViewGroupBlueprint<
        FinalView: ViewGroup,
        Parent: LayoutBlueprint,
        Child: LayoutBlueprint,
        Component: UIComponent<View,ComponentRoot>
>(
        layout: Parent,
        viewConstructor: (Context) -> FinalView,
        private val layoutConstructor: () -> Child
) : ViewBlueprint<FinalView, Parent, Component>(layout, viewConstructor) {

    protected val children = ArrayList<ViewBlueprint<in View, Child, UIComponent<out View, out ComponentRoot>>>()

    fun createLayoutBlueprint() = layoutConstructor()

    override fun bind(v: FinalView, c: Component, ctx: RenderContext) {
        super.bind(v, c, ctx)
        val group = v
        children.forEach {
            val child = it.create(ctx.ctx)
            if (it in childComponents) {    //this child has a different component
                val constructor = childComponents[it]!!
                val component = constructor.second(it, c.root)
                it.bind(child, component, ctx)
                c.addChild(constructor.first, component)
            } else {
                it.bind(child, c, ctx)
            }
            group.addView(child, it.layout.toLayoutParams(ctx))
        }
    }

    fun addChild(child: ViewBlueprint<out View, Child, UIComponent<out View, out ComponentRoot>>) {
        //we have to forget the real type because it's going into the collection :(
        children.add(child)
    }

    fun removeChild(child: ViewBlueprint<out View, Child, out UIComponent<View,*>>) {
        //we have to forget the real type because it's going into the collection :(
        children.remove(child)
    }

    internal val childComponents = HashMap<
            ViewBlueprint<*,*,*>,
            Pair<String, (ViewBlueprint<*,*,*>, ComponentRoot) -> UIComponent<out View, out ComponentRoot>>
    >()


    protected fun
            <ChildView: View, Blueprint : ViewBlueprint<ChildView, Child, Component>>
    childView(): ReadWriteProperty<ViewGroupBlueprint<FinalView, Parent, Child, Component>, Blueprint>
            = object : ReadWriteProperty<ViewGroupBlueprint<FinalView, Parent, Child, Component>, Blueprint> {

        private var blueprint: Blueprint? = null

        override fun getValue(
                thisRef: ViewGroupBlueprint<FinalView, Parent, Child, Component>, property: KProperty<*>
        ): Blueprint = blueprint ?: throw IllegalStateException("Blueprint for ${property.name} not bound!")

        override fun setValue(
                thisRef: ViewGroupBlueprint<FinalView, Parent, Child, Component>, property: KProperty<*>, value: Blueprint) {
            if (blueprint == value) return
            this.blueprint?.apply {
                removeChild(value)
                blueprint = null
            }
            addChild(value)
            blueprint = value
        }


    }

}


fun <
        Group: ViewGroup,
        Child: LayoutBlueprint,
        FinalView: View,
        Component: UIComponent<*,*,*>,
        Blueprint: ViewBlueprint<FinalView, Child, Component>
        >
ViewGroupBlueprint<Group, *, Child, Component>.child(
        initializer: Blueprint.() -> Unit,
        blueprint: Blueprint
): Blueprint {
    addChild(blueprint)
    blueprint.initializer()
    return blueprint
}


fun <
        Group: ViewGroup,
        Child: LayoutBlueprint,
        ChildView : View,
        Root: ComponentRoot,
        Parent: UIComponent<*,*,*>,
        Component : UIComponent<ChildView,Root,Component>
> ViewGroupBlueprint<Group, *, Child, Parent>.nestedUIComponent(
            key: String,
            constructor: (ChildView, Root) -> Component,
            initializer: () -> ViewBlueprint<ChildView, Child, Component>) {
    val child = initializer()
    addChild(child)
    this.childComponents[child] = constructor as (View, ComponentRoot) -> UIComponent<*,*,*>
}*/