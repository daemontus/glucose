package com.github.daemontus.glucose.blueprints

open class Component

/*open class UIComponent() : Component() {

    val view: View by ContextWriteProperty<UIComponent, View>()
}*/
/*
class ContextWriteProperty<K, T> : ReadWriteProperty<K, T> {

    override fun setValue(thisRef: K, property: KProperty<*>, value: T) {
        this.value = value
    }

    internal var value: T? = null

    override fun getValue(thisRef: K, property: KProperty<*>): T {
        return value!!
    }

}

//we have to accept mutable property, because immutable has a variance that will screw up the type safety
fun <V: View, L: LayoutBlueprint, C: UIComponent<V, *, *>> ViewBlueprint<V, L, C>.attach(prop: KMutableProperty1<C, in V>) {
    bindings.add { v, c ->
        prop.set(c, v)
    }
}

fun <V: View, VV: V, L: LayoutBlueprint, C: UIComponent<V, *, *>> ViewBlueprint<VV, L, C>.unsafeAttach(prop: KProperty1<C, V>) {
    bindings.add { v, c ->
        c.getProperty<C, V>(prop).value = v
    }
}

// Right now there is not type safe way to do this in kotlin :(

private fun <K, T> Any.getProperty(prop: KProperty<T>): ContextWriteProperty<K, T> {
    return try {
        //TODO: This should be a while loop!
        getDelegate(this, this.javaClass, prop) as ContextWriteProperty<K, T>
    } catch(e: ClassCastException) {
        throw e//InvalidTypeException("${prop.name} is of type ObservableProperty")
    } catch(e: NoSuchFieldException) {
        val s = javaClass.superclass
        if (s != null) {
            getDelegate(this, s!!, prop) as ContextWriteProperty<K, T>
        } else {
            throw e//InvalidTypeException("${prop.name} is of type ObservableProperty")
        }
    }
}

private fun <T> getDelegate(o : Any, c: Class<in Any>, prop: KProperty<T>): Any {
    println("Class: ${c.name}")
    println("Filds: ${c.fields.map { it.name }}")
    println("Filds: ${c.declaredFields.map { it.name }}")
    return c.getDeclaredField("${prop.name}\$delegate").let {
        it.isAccessible = true
        it.get(o)
    }
}*/