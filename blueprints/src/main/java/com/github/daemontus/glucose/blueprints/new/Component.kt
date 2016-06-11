package com.github.daemontus.glucose.blueprints.new

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import com.github.daemontus.glucose.blueprints.RenderContext
import com.github.daemontus.glucose.blueprints.asRenderContext
import com.github.daemontus.glucose.blueprints.layout.FrameLayoutBlueprint
import com.github.daemontus.glucose.blueprints.layout.LayoutBlueprint
import java.util.*
import kotlin.properties.Delegates

/*
fun <
        RootView: View, Root: ComponentActivity<*>,
        Comp: UIComponent<RootView, Root>,
        Blueprint: ViewBlueprint<RootView, LayoutBlueprint, Comp>
> rootComponent(
        constructor: (Blueprint, Root) -> Comp,
        blueprint: (LayoutBlueprint) -> Blueprint,
        initializer: Blueprint.() -> Unit): (Root) -> UIComponent<*, Root> {
    return { root: Root ->
        val print = blueprint(LayoutBlueprint())
        print.initializer()
        constructor(print, root)
    }
}

fun <
        RootView: View, Root: ComponentActivity<*>, Parent : LayoutBlueprint,
        Comp: UIComponent<RootView, Root>,
        Blueprint: ViewBlueprint<RootView, Parent, Comp>
> ViewGroupBlueprint<*,*,Parent,*>.childComponent(
        constructor: (Blueprint, Root) -> Comp,
        blueprint: (Parent) -> Blueprint,
        initializer: Blueprint.() -> Unit): Unit {
    { root: Root ->
        val print = blueprint(this.createLayoutParams())
        print.initializer()
        constructor(print, root)
    }
}


class TestComponent(rootView: ViewBlueprint<FrameLayout, *, TestComponent>, root: TestActivity) : UIComponent<FrameLayout, TestActivity>(rootView, root)

class TestActivity : SimpleComponentActivity<TestActivity>(rootComponent(::TestComponent, testBlueprint<LayoutBlueprint, TestComponent>(), {
    childComponent(::TestComponent, testBlueprint()) {

    }
}))

fun <P: LayoutBlueprint, C: UIComponent<*,*>> testBlueprint(): (P) -> TestBlueprint<P, C> = { TestBlueprint(it) }

class TestBlueprint<P: LayoutBlueprint, C: UIComponent<*,*>>(parent: P) : ViewGroupBlueprint<FrameLayout, P, FrameLayoutBlueprint, C>(parent)

open class ViewBlueprint<FinalView: View, Parent: LayoutBlueprint, C: UIComponent<*, *>>(
        val layout: Parent
) {

    open fun create(ctx: Context): FinalView = throw NotImplementedError()

    open fun bind(v: FinalView, ctx: RenderContext) {

    }
}

abstract class ViewGroupBlueprint<
        FinalView: ViewGroup,
        Parent: LayoutBlueprint,
        Child: LayoutBlueprint,
        C: UIComponent<*, *>>(
        layout: Parent
) : ViewBlueprint<FinalView, Parent, C>(layout) {
    open fun createLayoutParams(): Child = throw NotImplementedError()
}


abstract class ComponentActivity<T: ComponentActivity<T>> : Activity(), ComponentRoot {

    private var rootComponent: UIComponent<*, T> by Delegates.notNull()

    private var savedState: Bundle? = null

    abstract fun createRootComponent(): UIComponent<*, T>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        rootComponent = createRootComponent()
        rootComponent.onAttach(savedInstanceState?.getBundle("rootComponent") ?: Bundle())
        setContentView(rootComponent.rootView)
    }

    override fun onStart() {
        super.onStart()
        rootComponent.onAttach(savedState ?: Bundle())
        savedState = null   //next time we start, the state won't be relevant any more
    }

    override fun onResume() {
        super.onResume()
        rootComponent.onShow()
    }

    override fun onPause() {
        super.onPause()
        rootComponent.onHide()
    }

    override fun onStop() {
        super.onStop()
        rootComponent.onDetach()
    }

    override fun onDestroy() {
        super.onDestroy()
        rootComponent.onDestroy()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBundle("rootComponent", rootComponent.saveState())
    }

}

open class SimpleComponentActivity<T: SimpleComponentActivity<T>>(
        val constructor: (T) -> UIComponent<*, T>
) : ComponentActivity<T>() {
    override fun createRootComponent(): UIComponent<*, T> = constructor(this as T)
}*/