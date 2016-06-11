package com.github.daemontus.glucose.blueprints.layout

import android.view.Gravity
import android.view.ViewGroup
import android.widget.FrameLayout
import com.github.daemontus.glucose.blueprints.RenderContext

open class FrameLayoutBlueprint : MarginLayoutBlueprint() {

    var gravity: (RenderContext) -> Int = { Gravity.NO_GRAVITY }

    override fun toLayoutParams(ctx: RenderContext): ViewGroup.LayoutParams {
        val params = FrameLayout.LayoutParams(super.toLayoutParams(ctx))
        params.gravity = gravity(ctx)
        return params
    }

}