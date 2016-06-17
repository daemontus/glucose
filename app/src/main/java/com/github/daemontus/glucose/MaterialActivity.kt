package com.github.daemontus.glucose

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Point
import android.os.Bundle
import android.support.v4.view.animation.FastOutSlowInInterpolator
import android.support.v7.app.AppCompatActivity
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import com.github.daemontus.glucose.utils.Log
import com.github.daemontus.glucose.utils.MaterialColor
import com.github.daemontus.glucose.utils.device.Units
import java.util.*

class MaterialActivity : AppCompatActivity() {

    private var i = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_material)
        val world = findViewById(R.id.root) as MaterialWorld
        val action = findViewById(R.id.action) as Button
        //FrameLayout
        val m = TestMaterial(this, 1)
        val dp150 = Units.dpToPx(this, 150f).toInt()
        val moved = MaterialView.LayoutParams(dp150 / 2, dp150 / 3
        ).apply {
            this.x = dp150 / 2
            this.y = dp150 / 2
            this.shape = MaterialView.LayoutParams.SHAPE_ROUND_RECT
            this.anchorX = dp150 / 2
            this.anchorY = dp150 / 2
        }
        val original = MaterialView.LayoutParams(dp150, dp150
        ).apply {
            this.x = dp150
            this.y = dp150
            this.shape = MaterialView.LayoutParams.SHAPE_ROUND_RECT
            this.anchorX = dp150 / 2
            this.anchorY = dp150 / 2
        }

        /*val t = Transformation()
        t.show.add(m)
        world.transform(t)*/

        val c = MaterialView(this, null)

        val material = object : Material {

            private val v = LayoutInflater.from(this@MaterialActivity).inflate(R.layout.material_stock_photo, c, false)

            override val view: View = v
        }

        action.setOnClickListener {
            val t = Transformation()
            i += 1
            if (i == 1) {
                t.show.add(m)
            } else if (i % 3 == 0) {
                t.move.add(m to moved)
            } else if (i % 3 == 1) {
                world.morph(m, material)
            } else {
                t.move.add(m to original)
            }
            world.transform(t)
        }
    }

}

class TestMaterial(ctx: Context, private val i: Int) : Material {

    private val v = TextView(ctx).apply {
        this.setBackgroundColor(MaterialColor.Blue.C200)
        this.text = "Lorem Ipsum dolor sit amet."
        this.setPadding(16, 16, 16, 16)
        this.elevation = 10f
        val dp150 = Units.dpToPx(ctx, 150f).toInt()
        this.layoutParams = MaterialView.LayoutParams(dp150 / 3, (dp150 / 2) * 3
        ).apply {
            this.x = dp150
            this.y = dp150
            this.shape = MaterialView.LayoutParams.SHAPE_ROUND_RECT
            this.anchorX = dp150 / 2
            this.anchorY = dp150 / 2
        }
    }

    override val view: View = v

}

class Transformation {

    val show = ArrayList<Material>()
    val move = ArrayList<Pair<Material, MaterialView.LayoutParams>>()

}

class MaterialWorld(private val ctx: Context, attr: AttributeSet) : ViewGroup(ctx, attr) {

    private val materials = HashMap<Material, MaterialView>()

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        Log.d("Layout: $changed, $l, $t, $r, $b")
        for ((m, layout) in materials) {
            val dim = m.view.layoutParams as MaterialView.LayoutParams
            layout.layout(0, 0, layout.measuredWidth, layout.measuredHeight)
            layout.x = dim.x.toFloat()
            layout.y = dim.y.toFloat()
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        //TODO: This needs more thinking? Who is responsible for margin? Or elevation?
        //Just make sure the children are measured
        var childState = 0
        var width = suggestedMinimumWidth
        var height = suggestedMinimumHeight
        forEachChild { child ->
            measureChild(child, widthMeasureSpec, heightMeasureSpec)
            width = Math.max(width, child.measuredWidth)
            height = Math.max(height, child.measuredHeight)
            childState = combineMeasuredStates(childState, child.measuredState)
        }

        setMeasuredDimension(
                resolveSizeAndState(width, widthMeasureSpec, childState),
                resolveSizeAndState(height, heightMeasureSpec, childState.shl(View.MEASURED_HEIGHT_STATE_SHIFT))
        )
    }

    fun addMaterial(material: Material) {
        val layout = MaterialView(ctx, null)
        layout.setMaterial(material)
        layout.elevation = material.view.elevation
        materials[material] = layout
        addView(layout)
        this.requestLayout()
    }

    fun transform(t: Transformation) {
        for (m in t.show) {
            addMaterial(m)
            val l = materials[m]!!
            l.scaleX = 0.0f
            l.scaleY = 0.0f
            l.animate().scaleX(1f).scaleY(1f).duration = 200
        }

        for ((from, to) in t.move) {
            val v = materials[from]!!
            //animate position
            //ObjectAnimator.ofFloat()
            val lp = from.view.layoutParams as MaterialView.LayoutParams
            val xDiff = v.x - to.x.toFloat()
            val yDiff = v.y - to.y.toFloat()
            val initialX = v.x
            val initialY = v.y
            val animator = ValueAnimator.ofFloat(0f, (Math.PI/2).toFloat())
            animator.duration = Duration.STANDARD
            animator.interpolator = FastOutSlowInInterpolator()
            animator.addUpdateListener {
                val A = (it.animatedValue as Float).toDouble()
                val sinA = Math.sin(A).toFloat()
                val cosA = Math.cos(A).toFloat()
                //Log.d("A: $A, sinA: $sinA, cosA: $cosA")
                if (xDiff < 0) {    //TODO: this also depends on yDiff
                    v.x = initialX - xDiff * (1 - cosA)
                    v.y = initialY - yDiff * sinA
                } else {
                    v.x = initialX - xDiff * sinA
                    v.y = initialY - yDiff * (1 - cosA)
                }
            }
            animator.start()
            //animate size
            val width = ValueAnimator.ofFloat(v.width.toFloat(), to.width.toFloat())
            width.duration = 290
            width.interpolator = FastOutSlowInInterpolator()
            width.addUpdateListener {
                val w = it.animatedValue as Float
                lp.width = w.toInt()
                lp.height = v.height
                v.measure(View.MeasureSpec.makeMeasureSpec(w.toInt(), View.MeasureSpec.EXACTLY), View.MeasureSpec.makeMeasureSpec(v.height, View.MeasureSpec.EXACTLY))
                v.layout(0,0,w.toInt(),v.height)
            }
            width.start()

            val height = ValueAnimator.ofFloat(v.height.toFloat(), to.height.toFloat())
            height.duration = 325
            height.startDelay = 50
            height.interpolator = FastOutSlowInInterpolator()
            height.addUpdateListener {
                val w = it.animatedValue as Float
                lp.width = v.width
                lp.height = w.toInt()
                v.measure(View.MeasureSpec.makeMeasureSpec(v.width, View.MeasureSpec.EXACTLY), View.MeasureSpec.makeMeasureSpec(w.toInt(), View.MeasureSpec.EXACTLY))
                v.layout(0,0,v.width,w.toInt())
            }
            height.start()
        }
    }

    fun morph(from: Material, to: Material) {
        if (from !in materials && to in materials) {
            morph(to, from)
            return
        }
        val fromLP = from.view.layoutParams as MaterialView.LayoutParams
        val toLP = to.view.layoutParams as MaterialView.LayoutParams
        val l = materials[from]!!
        materials.remove(from)
        materials[to] = l
        l.setMaterial(to)
        val anim = ValueAnimator.ofFloat(0f, 1f)
        val fromW = fromLP.width
        val fromH = fromLP.height
        val diffW = toLP.width - fromLP.width
        val diffH = toLP.height - fromLP.height
        anim.duration = Duration.STANDARD
        anim.interpolator = FastOutSlowInInterpolator()
        //anim.repeatMode = ValueAnimator.REVERSE
        //anim.repeatCount = 1
        anim.addUpdateListener {
            val w = it.animatedValue as Float
            l.layout(0,0,fromW + (diffW * w).toInt(), fromH + (diffH * w).toInt())
        }
        anim.start()
    }

}