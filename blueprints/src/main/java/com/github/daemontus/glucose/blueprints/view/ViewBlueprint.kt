package com.github.daemontus.glucose.blueprints.view
/*
import android.content.Context
import android.graphics.drawable.Drawable
import android.view.View
import com.github.daemontus.glucose.blueprints.RenderContext
import com.github.daemontus.glucose.blueprints.ViewId
import com.github.daemontus.glucose.blueprints.layout.LayoutBlueprint
import com.github.daemontus.glucose.utils.device.Device
import java.util.*

open class ViewBlueprint<FinalView: View, Parent: LayoutBlueprint, Component: UIComponent<*, *>>(
        val layout: Parent,
        private val constructor: (Context) -> FinalView
) {

    var id: ViewId<FinalView>? = null

    var tag: Any? = null

    //note: We allow nulls to prevent unnecessary object creation (if it were lambdas returning 0)

    var startPadding: ((RenderContext) -> Int)? = null
    var endPadding: ((RenderContext) -> Int)? = null
    var topPadding: ((RenderContext) -> Int)? = null
    var bottomPadding: ((RenderContext) -> Int)? = null

    var background: ((RenderContext) -> Drawable?)? = null

    var visibility: ((RenderContext) -> Int)? = null

    open fun create(ctx: Context): FinalView = constructor(ctx)

    open fun bind(v: FinalView, c: Component, ctx: RenderContext) {
        id?.apply { v.id = id }
        v.tag = tag
        visibility?.apply { v.visibility = this(ctx) }
        v.setPadding(
                startPadding?.invoke(ctx) ?: 0,
                topPadding?.invoke(ctx) ?: 0,
                endPadding?.invoke(ctx) ?: 0,
                bottomPadding?.invoke(ctx) ?: 0)
        if (Device.OS.atLeastJellyBean()) {
            v.background = background?.invoke(ctx)
        } else {
            @Suppress("DEPRECATION")
            v.setBackgroundDrawable(background?.invoke(ctx))
        }
        bindings.forEach {
            it(v, c)
        }
    }

    internal val bindings = ArrayList<(FinalView, Component) -> Unit>()

    //TODO: Shit-ton of other properties...

}*/