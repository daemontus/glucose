package com.github.daemontus.glucose.blueprints

import android.app.Activity
import android.content.Context
import android.content.res.Resources
import android.view.View
import android.view.ViewGroup


fun dpToPx(value: Float, ctx: Context): Int = (value * (ctx.resources.displayMetrics.densityDpi / 160f)).toInt()
fun spToPx(value: Float, ctx: Context): Int = (value * ctx.resources.displayMetrics.scaledDensity).toInt()

class RenderContext(
        val ctx: Context,
        val theme: Resources.Theme,
        val parent: View
)

fun Activity.asRenderContext(): RenderContext {
    return RenderContext(this, this.theme, this.findViewById(android.R.id.content))
}

abstract class Pixels : (RenderContext) -> Int {}

val match_parent: Pixels = object : Pixels() {
    override fun invoke(p1: RenderContext): Int = ViewGroup.LayoutParams.MATCH_PARENT
}
val wrap_content: Pixels = object : Pixels() {
    override fun invoke(p1: RenderContext): Int = ViewGroup.LayoutParams.WRAP_CONTENT
}

val visible: (RenderContext) -> Int = { View.VISIBLE }
val invisible: (RenderContext) -> Int = { View.INVISIBLE }
val gone: (RenderContext) -> Int = { View.GONE }

class Dp(
        val dp: Float
) : Pixels() {
    override fun invoke(p1: RenderContext): Int = dpToPx(dp, p1.ctx)
}

class Sp(
        val sp: Float
) : Pixels() {
    override fun invoke(p1: RenderContext): Int = spToPx(sp, p1.ctx)
}

fun Int.px() = object : Pixels() {
    override fun invoke(p1: RenderContext): Int = this@px
}

fun Double.px() = object : Pixels() {
    override fun invoke(p1: RenderContext): Int = this@px.toInt()
}

fun Float.px() = object : Pixels() {
    override fun invoke(p1: RenderContext): Int = this@px.toInt()
}

fun Int.dp() = Dp(this.toFloat())

fun Double.dp() = Dp(this.toFloat())

fun Float.dp() = Dp(this.toFloat())

fun Int.sp() = Sp(this.toFloat())

fun Double.sp() = Sp(this.toFloat())

fun Float.sp() = Sp(this)