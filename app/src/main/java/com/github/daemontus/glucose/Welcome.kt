package com.github.daemontus.glucose

import android.graphics.Color
import android.widget.FrameLayout
import com.github.daemontus.glucose.blueprints.component.SimpleComponentActivity
import com.github.daemontus.glucose.blueprints.component.UIComponent
import com.github.daemontus.glucose.blueprints.component.intState
import com.github.daemontus.glucose.blueprints.layout.FrameLayoutBlueprint
import com.github.daemontus.glucose.blueprints.layout.LayoutBlueprint
import com.github.daemontus.glucose.blueprints.view.*
import com.github.daemontus.glucose.utils.MaterialColor

class FinalFrameLayout<P: LayoutBlueprint, C: UIComponent<*,*,*>>(
        l: P
) : FrameGroupBlueprint<FrameLayout, P, C>(l, ::FrameLayout)

class LoginActivity : SimpleComponentActivity<LoginActivity>(rootComponent(::LoginComponent, {
    FrameGroupBlueprint(it, ::FrameLayout)
}) {
    uiComponent(::LoginComponent, {
        FrameGroupBlueprint(it, ::FrameLayout)
    }) {
        uiComponent(::LoginComponent, { FinalFrameLayout(it) }) {

        }
    }
})

/*

component(::LoginComponent, ::frameLayout) {
    background = { ... }
    component(::RandomColorBackground, ::frameLayout) {
        imageView {
        }
        imageView {
            background = { ... }
        }
    }
}

 */

class LoginComponent(
        view: FrameLayout, ctx: LoginActivity
) : UIComponent<FrameLayout, LoginActivity, LoginComponent>(view, ctx)


/*
class LoginComponent(
        ctx: LoginActivity,
        initialState: Bundle?,
        parent: View
) : UIGroupComponent<FrameLayout, LoginActivity, LoginComponent>(frameLayout<LoginComponent> {
    layout.width = match_parent
    layout.height = 50.dp()
    background = { ColorDrawable(Color.BLACK) }
}, parent, initialState, ctx ) {

    constructor(ctx: LoginActivity, initialState: Bundle?): this(ctx, initialState, ctx.findViewById(android.R.id.content))

    //val background = childUIComponent(::RandomColorBackground)
    init {
        childUIComponent(::RandomColorBackground)
    }
}*/

class RandomColorBackground(view: FrameLayout, ctx: LoginActivity) : UIComponent<FrameLayout, LoginActivity, RandomColorBackground>(
        view, ctx
) {

    private val colors = MaterialColor.colorPalettes.map { it.C500 }

    var activeColor by intState("active_color", nextRandomColor())

    val layout = rootView

    init {
        layout.addView(ImageView(ctx))
        val current = ImageView(ctx)
        current.setBackgroundColor(activeColor)
        layout.addView(current)
        layout.layoutTransition = LayoutTransition()
        layout.layoutTransition.setDuration(1000)
        whileVisible {
            Observable
                    .interval(3, TimeUnit.SECONDS)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe {
                activeColor = nextRandomColor(activeColor)
                val newCurrent = layout.getChildAt(0)
                layout.removeView(newCurrent)
                newCurrent.setBackgroundColor(activeColor)
                layout.addView(newCurrent)
            }
        }
    }

    private fun nextRandomColor(previous: Int = Color.WHITE): Int {
        var color = previous
        while (color == previous) {
            color = colors[(colors.size * Math.random()).toInt()]
        }
        return color
    }

}