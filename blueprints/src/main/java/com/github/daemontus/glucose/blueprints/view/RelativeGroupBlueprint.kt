package com.github.daemontus.glucose.blueprints.view

import android.content.Context
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.RelativeLayout
import com.github.daemontus.glucose.blueprints.RenderContext
import com.github.daemontus.glucose.blueprints.layout.LayoutBlueprint
import com.github.daemontus.glucose.blueprints.layout.RelativeLayoutBlueprint

/*
open class RelativeGroupBlueprint<
        G: RelativeLayout,
        Parent: LayoutBlueprint,
        C: UIComponent
        >(layout: Parent) : ViewGroupBlueprint<G, Parent, RelativeLayoutBlueprint, C>(layout) {

    override fun createLayoutBlueprint(): RelativeLayoutBlueprint = RelativeLayoutBlueprint()

    var gravity: (RenderContext) -> Int = { Gravity.NO_GRAVITY }

    override fun create(ctx: Context): View = RelativeLayout(ctx)

    override fun <VV: G> bind(v: VV, c: C, ctx: RenderContext) {
        super.bind(v, c, ctx)
        val l = v as LinearLayout
        l.setGravity(gravity(ctx))
    }
}

// DSL

fun <
        Group: ViewGroup,
        Parent: LayoutBlueprint,
        Child: LayoutBlueprint,
        C: UIComponent
        > ViewGroupBlueprint<Group, Parent, Child, C>.relativeLayout(
        initializer: RelativeGroupBlueprint<RelativeLayout, Child, C>.() -> Unit
): RelativeGroupBlueprint<RelativeLayout, Child, C> {
    return this.child(initializer, RelativeGroupBlueprint<RelativeLayout, Child, C>(this.createLayoutBlueprint()))
}

fun <C: UIComponent> relativeLayout(
        layout: LayoutBlueprint = LayoutBlueprint(),
        initializer: RelativeGroupBlueprint<RelativeLayout, LayoutBlueprint, C>.() -> Unit
): RelativeGroupBlueprint<RelativeLayout, LayoutBlueprint, C> {
    val blueprint = RelativeGroupBlueprint<RelativeLayout, LayoutBlueprint, C>(layout)
    blueprint.initializer()
    return blueprint
}*/