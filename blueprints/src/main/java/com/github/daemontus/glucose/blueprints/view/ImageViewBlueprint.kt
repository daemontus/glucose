package com.github.daemontus.glucose.blueprints.view

/*
open class ImageViewBlueprint<
        V: ImageView,
        L: LayoutBlueprint,
        C: UIComponent<*,*,*>
>(layout: L) : ViewBlueprint<V, L, C>(layout) {

    var src: ((RenderContext) -> Drawable?)? = null
    var scaleType: ((RenderContext) -> ImageView.ScaleType)? = null

    override fun create(ctx: Context): View = ImageView(ctx)

    override fun <VV: V> bind(v: VV, c: C, ctx: RenderContext) {
        super.bind(v, c, ctx)
        val t = v
        src?.apply { t.setImageDrawable(this(ctx)) }
        scaleType?.apply { t.scaleType = this(ctx) }
    }

}

// DSL

fun <
        Group: ViewGroup,
        Parent: LayoutBlueprint,
        Child: LayoutBlueprint,
        C: UIComponent
        > ViewGroupBlueprint<Group, Parent, Child, C>.imageView(
        initializer: ImageViewBlueprint<ImageView, Child, C>.() -> Unit
): ImageViewBlueprint<ImageView, Child, C> {
    return this.child(initializer, ImageViewBlueprint<ImageView, Child, C>(this.createLayoutBlueprint()))
}*/