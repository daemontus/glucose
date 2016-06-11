package com.github.daemontus.glucose.blueprints.view

import android.content.Context
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import com.github.daemontus.glucose.blueprints.RenderContext
import com.github.daemontus.glucose.blueprints.layout.LayoutBlueprint
import com.github.daemontus.glucose.blueprints.layout.LinearLayoutBlueprint
/*
open class LinearGroupBlueprint<
        G: LinearLayout,
        Parent: LayoutBlueprint
        >(layout: Parent) : ViewGroupBlueprint<G, Parent, LinearLayoutBlueprint>(layout) {

    override fun createLayoutBlueprint(): LinearLayoutBlueprint = LinearLayoutBlueprint()

    var gravity: (RenderContext) -> Int = { Gravity.NO_GRAVITY }
    var orientation: (RenderContext) -> Int = { LinearLayout.HORIZONTAL }

    override fun create(ctx: Context): View = LinearLayout(ctx)

    override fun apply(v: View, ctx: RenderContext) {
        super.apply(v, ctx)
        val l = v as LinearLayout
        l.setGravity(gravity(ctx))
        l.orientation = orientation(ctx)
    }
}

// DSL

fun <
        Group: ViewGroup,
        Parent: LayoutBlueprint,
        Child: LayoutBlueprint
        > ViewGroupBlueprint<Group, Parent, Child>.linearLayout(
        initializer: LinearGroupBlueprint<LinearLayout, Child>.() -> Unit
): LinearGroupBlueprint<LinearLayout, Child> {
    return this.child(initializer, LinearGroupBlueprint<LinearLayout, Child>(this.createLayoutBlueprint()))
}

fun linearLayout(
        layout: LayoutBlueprint = LayoutBlueprint(),
        initializer: LinearGroupBlueprint<LinearLayout, LayoutBlueprint>.() -> Unit
): LinearGroupBlueprint<LinearLayout, LayoutBlueprint> {
    val blueprint = LinearGroupBlueprint<LinearLayout, LayoutBlueprint>(layout)
    blueprint.initializer()
    return blueprint
}*/