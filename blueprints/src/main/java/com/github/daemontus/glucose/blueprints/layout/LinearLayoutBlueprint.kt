package com.github.daemontus.glucose.blueprints.layout

import android.view.Gravity
import android.view.ViewGroup
import android.widget.LinearLayout
import com.github.daemontus.glucose.blueprints.RenderContext


open class LinearLayoutBlueprint : MarginLayoutBlueprint() {
    var gravity: (RenderContext) -> Int = { Gravity.NO_GRAVITY }
    var weight: (RenderContext) -> Float = { 0.0f }

    override fun toLayoutParams(ctx: RenderContext): ViewGroup.LayoutParams {
        val params = LinearLayout.LayoutParams(super.toLayoutParams(ctx))
        params.gravity = gravity(ctx)
        params.weight = weight(ctx)
        return params
    }

}