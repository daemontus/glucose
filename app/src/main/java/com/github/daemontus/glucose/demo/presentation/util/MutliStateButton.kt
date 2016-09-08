package com.github.daemontus.glucose.demo.presentation.util

import android.content.Context
import android.os.Parcel
import android.os.Parcelable
import android.support.annotation.DrawableRes
import android.support.v4.view.animation.FastOutLinearInInterpolator
import android.support.v4.view.animation.FastOutSlowInInterpolator
import android.support.v4.view.animation.LinearOutSlowInInterpolator
import android.util.AttributeSet
import android.view.Gravity
import android.widget.FrameLayout
import android.widget.ImageView
import com.github.daemontus.glucose.demo.R


class MultiStateButton : FrameLayout {

    constructor(context: Context?) : super(context)
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    val offset = context.resources.getDimension(R.dimen.multi_button_offset).toFloat()

    val backButtonState = MultiStateButton.ButtonState(
            R.drawable.ic_arrow_left, R.drawable.bg_ripple_gray,
            context.resources.getString(R.string.cd_back_button))

    init {
        this.isClickable = true
    }

    var state: ButtonState? = null
        set(value) {
            if (value == field) return
            val old = field
            field = value
            when {
                value == null && old != null -> {
                    //Hide
                    val child = getChildAt(0)
                    child.animate()
                            .alpha(0f)
                            .setDuration(Duration.LEAVE_FAST)
                            .setInterpolator(FastOutLinearInInterpolator())
                            .withEndAction {
                                removeView(child)
                                background = null
                            }
                }
                value != null && old == null -> {
                    //Reveal
                    val child = newChild(value)
                    child.scaleX = 0f
                    child.scaleY = 0f
                    addView(child)
                    child.animate()
                            .scaleX(1f).scaleY(1f)
                            .setDuration(Duration.ENTER_FAST)
                            .setInterpolator(LinearOutSlowInInterpolator())
                            .withEndAction {
                                setBackgroundResource(value.background)
                            }
                }
                value != null && old != null && value != old -> {
                    //Swap
                    val oldChild = getChildAt(childCount - 1)
                    val newChild = newChild(value)
                    newChild.translationY = offset
                    newChild.alpha = 0f
                    addView(newChild)
                    newChild.animate()
                            .alpha(1f).translationY(0f)
                            .setDuration(Duration.SWAP)
                            .setInterpolator(FastOutSlowInInterpolator())
                            .withEndAction {
                                setBackgroundResource(value.background)
                            }
                    oldChild.animate()
                            .alpha(0f).translationY(-offset)
                            .setDuration(Duration.SWAP)
                            .setInterpolator(FastOutSlowInInterpolator())
                            .withEndAction {
                                removeView(oldChild)
                            }
                }
            //else -> value == old
            }
        }

    private fun newChild(state: ButtonState): ImageView {
        val child = ImageView(context)
        child.layoutParams = LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT).apply {
            this.gravity = Gravity.CENTER
        }
        child.setImageResource(state.src)
        child.contentDescription = state.contentDescription
        return child
    }

    data class ButtonState(
            @DrawableRes val src: Int,
            @DrawableRes val background: Int,
            val contentDescription: String
    ) : Parcelable {
        override fun writeToParcel(p: Parcel, p1: Int) {
            p.writeInt(src)
            p.writeInt(background)
            p.writeString(contentDescription)
        }

        override fun describeContents(): Int = 0

        companion object {

            @Suppress("unused")
            @JvmField val CREATOR = object : Parcelable.Creator<ButtonState> {
                override fun createFromParcel(p0: Parcel): ButtonState = ButtonState(
                        p0.readInt(), p0.readInt(), p0.readString()
                )

                override fun newArray(p0: Int): Array<out ButtonState?> = kotlin.arrayOfNulls<ButtonState>(p0)

            }
        }

    }
}