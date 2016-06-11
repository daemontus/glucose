package com.github.daemontus.glucose.blueprints.component

/*
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
        val constructor: (T) -> UIComponent<*, T, *>
) : ComponentActivity<T>() {
    override fun createRootComponent(): UIComponent<*, T, *> = constructor(this as T)
}*/