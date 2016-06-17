package com.github.daemontus.glucose

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import com.github.daemontus.glucose.utils.Log

/**
 * Material view serves as a universal container for all materials inside a MaterialLayout.
 * It has three base functions:
 *  - Cross-fade between two materials when morphing.
 *  - Clip material to size and anchor specified in its material layout params.
 *  - Clip material with round-corner rectangle or a circle.
 */
class MaterialView(ctx: Context, attr: AttributeSet?) : ViewGroup(ctx, attr) {

    init {
        clipChildren = true
        clipToOutline = true
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        //Just make sure the children are measured
        var childState = 0
        var width = suggestedMinimumWidth
        var height = suggestedMinimumHeight
        forEachChild { child ->
            measureChildWithMargins(child, widthMeasureSpec, 0, heightMeasureSpec, 0)
            width = Math.max(width, child.measuredWidth)
            height = Math.max(height, child.measuredHeight)
            childState = combineMeasuredStates(childState, child.measuredState)
        }

        Log.d("Measured: $width, $height")
        setMeasuredDimension(
                resolveSizeAndState(width, widthMeasureSpec, childState),
                resolveSizeAndState(height, heightMeasureSpec, childState.shl(View.MEASURED_HEIGHT_STATE_SHIFT))
        )

    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        Log.d("L: $l, $t, $r, $b")
        val givenWidth = r - l
        val givenHeight = b - t
        forEachChild { child ->
            val lp = child.layoutParams as LayoutParams
            //TODO: Consider what happens when the view outgrows the content, because right now it will just stick in upper-left corner
            //make sure the content is either in the upper left corner,
            //or shifted up/left so that the anchor is in the center.
            val x = Math.min(l, Math.max(   //ensure we don't move the content too much to the right
                    r - child.measuredWidth,    //ensure we don't move the content too much to the left
                    l + givenWidth/2 - lp.anchorX   //adjust left bound according to anchor
            ))
            val y = Math.min(t, Math.max(
                    b - child.measuredHeight,
                    t + givenHeight/2 - lp.anchorY
            ))
            Log.d("Layout: $x, $y, ${child.measuredWidth}, ${child.measuredHeight}")
            child.layout(x, y, x + child.measuredWidth, y + child.measuredHeight)
        }
    }

    override fun shouldDelayChildPressedState(): Boolean = false

    fun setMaterial(material: Material) {
        //ensure child has correct layout parameters
        if (material.view.layoutParams == null) {
            material.view.layoutParams = generateDefaultLayoutParams()
        }
        checkLayoutParams(material.view.layoutParams)
        val lp = material.view.layoutParams as LayoutParams
        when (lp.shape) {
            LayoutParams.SHAPE_RECT -> setBackgroundColor(Color.WHITE)
            LayoutParams.SHAPE_ROUND_RECT -> setBackgroundResource(R.drawable.bg_material_rect)
            LayoutParams.SHAPE_OVAL -> setBackgroundResource(R.drawable.bg_material_oval)
        }
        if (childCount == 0) {
            //Adding first material, no animation
            this.addView(material.view)
        } else {    //childCount > 0
            for (i in 1 until childCount) {
                //stop any ongoing fades and remove immediately
                val child = getChildAt(i)
                child.animate().cancel()
                removeView(child)
            }
            //there should be one child remaining,
            //restart animations on it and make sure it gets removed later.
            val child = getChildAt(0)
            child.animate().cancel()    //cancels possible reveal animation
            child.animate().alpha(0f).withEndAction {
                Log.d("View removed! $child")
                removeView(child)
            }.duration = (Duration.STANDARD * child.alpha).toLong()

            //Now add the new child
            Log.d("Add: ${material.view}")
            this.addView(material.view)
            material.view.alpha = 0f
            material.view.animate().alpha(1f).withEndAction {
                //removeView(child)
            }.duration = Duration.STANDARD
        }
    }

    //due diligence

    override fun generateLayoutParams(attrs: AttributeSet?): LayoutParams?
            = LayoutParams(context, attrs)

    override fun generateDefaultLayoutParams(): LayoutParams?
            = LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)

    override fun generateLayoutParams(p: ViewGroup.LayoutParams?): LayoutParams?
            = LayoutParams(p)

    override fun checkLayoutParams(p: ViewGroup.LayoutParams?): Boolean = p is LayoutParams


    /**
     * Layout parameters specify how the material should behave inside
     * of the MaterialLayout. Note that these properties are accessed both
     * by the enclosing MaterialView and the parent MaterialWorld (if any).
     */
    class LayoutParams : ViewGroup.MarginLayoutParams {

        companion object {
            val SHAPE_RECT = 0
            val SHAPE_ROUND_RECT = 1
            val SHAPE_OVAL = 2
        }
        constructor(ctx: Context, attrs: AttributeSet?) : super(ctx, attrs) {
            val a = ctx.obtainStyledAttributes(attrs, R.styleable.MaterialView_Layout)
            x = a.getDimensionPixelSize(R.styleable.MaterialView_Layout_layout_x, 0)
            y = a.getDimensionPixelSize(R.styleable.MaterialView_Layout_layout_y, 0)
            anchorX = a.getDimensionPixelSize(R.styleable.MaterialView_Layout_layout_anchor_x, 0)
            anchorY = a.getDimensionPixelSize(R.styleable.MaterialView_Layout_layout_anchor_y, 0)
            shape = a.getInt(R.styleable.MaterialView_Layout_layout_shape, 0)
            a.recycle()
        }

        constructor(width: Int, height: Int) : super(width, height)

        constructor(params: ViewGroup.LayoutParams?) : super(params)

        //TODO: Add special values for position and anchor, like wrap_content for width, etc.

        //Position of THE CENTER of the material in the world.
        var x: Int = 0
        var y: Int = 0

        //Position of the anchor point inside the material.
        var anchorX: Int = 0
        var anchorY: Int = 0

        var shape: Int = 0

    }
}