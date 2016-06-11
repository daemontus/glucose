package com.github.daemontus.glucose.blueprints.layout

import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import com.github.daemontus.glucose.blueprints.RenderContext
import com.github.daemontus.glucose.blueprints.ViewId
import com.github.daemontus.glucose.utils.device.Device
import java.util.*

open class RelativeLayoutBlueprint : MarginLayoutBlueprint() {

    private var rules = ArrayList<Pair<Int, Int?>>()

    override fun toLayoutParams(ctx: RenderContext): ViewGroup.LayoutParams {
        val params = RelativeLayout.LayoutParams(super.toLayoutParams(ctx))
        for ((command, anchor) in rules) {
            if (anchor != null) {
                params.addRule(command, anchor)
            } else {
                params.addRule(command)
            }
        }
        return params
    }

    private fun <V: View> addCommand(command: Int, anchor: ViewId<in V>? = null) {
        rules.add(Pair(command, anchor?.id))
    }

    fun <V: View> above(anchor: ViewId<in V>) = addCommand(RelativeLayout.ABOVE, anchor)

    fun <V: View> below(anchor: ViewId<in V>) = addCommand(RelativeLayout.BELOW, anchor)

    fun <V: View> leftOf(anchor: ViewId<in V>) = addCommand(RelativeLayout.LEFT_OF, anchor)

    fun <V: View> rightOf(anchor: ViewId<in V>) = addCommand(RelativeLayout.RIGHT_OF, anchor)

    fun <V: View> startOf(anchor: ViewId<in V>) = if (Device.OS.atLeastJellyBeanMR1()) {
        addCommand(RelativeLayout.START_OF, anchor)
    } else {
        addCommand(RelativeLayout.LEFT_OF, anchor)
    }

    fun <V: View> endOf(anchor: ViewId<in V>) = if (Device.OS.atLeastJellyBeanMR1()) {
        addCommand(RelativeLayout.END_OF, anchor)
    } else {
        addCommand(RelativeLayout.RIGHT_OF, anchor)
    }


    fun <V: View> alignBaseline(anchor: ViewId<in V>) = addCommand(RelativeLayout.ALIGN_BASELINE, anchor)

    fun <V: View> alignBottom(anchor: ViewId<in V>) = addCommand(RelativeLayout.ALIGN_BOTTOM, anchor)

    fun <V: View> alignTop(anchor: ViewId<in V>) = addCommand(RelativeLayout.ALIGN_TOP, anchor)

    fun <V: View> alignLeft(anchor: ViewId<in V>) = addCommand(RelativeLayout.ALIGN_LEFT, anchor)

    fun <V: View> alignRight(anchor: ViewId<in V>) = addCommand(RelativeLayout.ALIGN_RIGHT, anchor)

    fun <V: View> alignStart(anchor: ViewId<in V>) = if (Device.OS.atLeastJellyBeanMR1()) {
        addCommand(RelativeLayout.ALIGN_START, anchor)
    } else {
        addCommand(RelativeLayout.ALIGN_RIGHT, anchor)
    }

    fun <V: View> alignEnd(anchor: ViewId<in V>) = if (Device.OS.atLeastJellyBeanMR1()) {
        addCommand(RelativeLayout.ALIGN_END, anchor)
    } else {
        addCommand(RelativeLayout.ALIGN_RIGHT, anchor)
    }


    fun <V: View> alignParentBottom(anchor: ViewId<in V>) = addCommand(RelativeLayout.ALIGN_PARENT_BOTTOM, anchor)

    fun <V: View> alignParentTop(anchor: ViewId<in V>) = addCommand(RelativeLayout.ALIGN_PARENT_TOP, anchor)

    fun <V: View> alignParentLeft(anchor: ViewId<in V>) = addCommand(RelativeLayout.ALIGN_PARENT_LEFT, anchor)

    fun <V: View> alignParentRight(anchor: ViewId<in V>) = addCommand(RelativeLayout.ALIGN_PARENT_RIGHT, anchor)

    fun <V: View> alignParentStart(anchor: ViewId<in V>) = if (Device.OS.atLeastJellyBeanMR1()) {
        addCommand(RelativeLayout.ALIGN_PARENT_START, anchor)
    } else {
        addCommand(RelativeLayout.ALIGN_PARENT_LEFT, anchor)
    }

    fun <V: View> alignParentEnd(anchor: ViewId<in V>) = if (Device.OS.atLeastJellyBeanMR1()) {
        addCommand(RelativeLayout.ALIGN_PARENT_END, anchor)
    } else {
        addCommand(RelativeLayout.ALIGN_PARENT_RIGHT, anchor)
    }

    fun <V: View> centerInParent(anchor: ViewId<in V>) = addCommand<V>(RelativeLayout.CENTER_IN_PARENT)
    fun <V: View> centerVertical(anchor: ViewId<in V>) = addCommand<V>(RelativeLayout.CENTER_VERTICAL)
    fun <V: View> centerHorizontal(anchor: ViewId<in V>) = addCommand<V>(RelativeLayout.CENTER_HORIZONTAL)

}