package com.github.daemontus.glucose.blueprints.layout

import android.view.ViewGroup
import com.github.daemontus.glucose.blueprints.RenderContext
import com.github.daemontus.glucose.blueprints.px
import com.github.daemontus.glucose.blueprints.wrap_content
import com.github.daemontus.glucose.utils.device.Device

open class LayoutBlueprint() {
    var width: (RenderContext) -> Int = wrap_content
    var height: (RenderContext) -> Int = wrap_content

    open fun toLayoutParams(ctx: RenderContext): ViewGroup.LayoutParams {
        return ViewGroup.LayoutParams(width(ctx), height(ctx))
    }
}

open class MarginLayoutBlueprint : LayoutBlueprint() {
    var startMargin: (RenderContext) -> Int = 0.px()
    var endMargin: (RenderContext) -> Int = 0.px()
    var leftMargin: (RenderContext) -> Int = 0.px()
    var rightMargin: (RenderContext) -> Int = 0.px()
    var topMargin: (RenderContext) -> Int = 0.px()
    var bottomMargin: (RenderContext) -> Int = 0.px()
    var layoutDirection: (RenderContext) -> Int = { ViewGroup.LAYOUT_DIRECTION_LTR }

    override fun toLayoutParams(ctx: RenderContext): ViewGroup.LayoutParams {
        val params = ViewGroup.MarginLayoutParams(super.toLayoutParams(ctx))
        params.topMargin = topMargin(ctx)
        params.bottomMargin = bottomMargin(ctx)
        params.leftMargin = leftMargin(ctx)
        params.rightMargin = rightMargin(ctx)
        if (Device.OS.atLeastJellyBeanMR1()) {
            params.marginStart = startMargin(ctx)
            params.marginEnd = endMargin(ctx)
            params.layoutDirection = layoutDirection(ctx)
        }
        return params
    }
}