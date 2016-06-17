package com.github.daemontus.glucose

import android.animation.ValueAnimator
import android.graphics.Point
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import com.github.daemontus.glucose.utils.MaterialColor
import com.github.daemontus.glucose.utils.device.Units

class MaterialViewActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_material_view)
        val root = findViewById(R.id.root) as ViewGroup

        val materialView = MaterialView(this, null)

        val material = object : Material {

            private val v = LayoutInflater.from(this@MaterialViewActivity).inflate(R.layout.material_stock_photo, materialView, false)/* ImageView(this@MaterialViewActivity).apply {
                setImageResource(R.mipmap.stock_photo)
                val lp = MaterialView.LayoutParams(
                        Units.dpToPx(this@MaterialViewActivity, 150f).toInt(),
                        Units.dpToPx(this@MaterialViewActivity, 100f).toInt()
                )
                lp.anchorX = lp.width / 2
                lp.anchorY = lp.height / 2
                layoutParams = lp
            }*/

            override val view: View = v
        }

        val material1 = object : Material {

            private val v = ImageView(this@MaterialViewActivity).apply {
                setImageResource(com.github.daemontus.glucose.utils.R.color.green_a200)
                val lp = MaterialView.LayoutParams(
                        Units.dpToPx(this@MaterialViewActivity, 150f).toInt(),
                        Units.dpToPx(this@MaterialViewActivity, 100f).toInt()
                )
                lp.anchorX = lp.width / 2
                lp.anchorY = lp.height / 2
                layoutParams = lp
            }

            override val view: View = v
        }

        materialView.setMaterial(material)
        //materialView.setBackgroundColor(MaterialColor.Cyan.C500)

        val anim = ValueAnimator.ofFloat(0f, 1f)
        anim.repeatMode = ValueAnimator.REVERSE
        anim.repeatCount = ValueAnimator.INFINITE
        anim.duration = 2*Duration.COMPLEX
        anim.addUpdateListener {
            val lp = material.view.layoutParams as MaterialView.LayoutParams
            lp.anchorX = (lp.width * it.animatedFraction).toInt()
            lp.anchorY = (lp.height * it.animatedFraction).toInt()
            materialView.requestLayout()
        }
        anim.start()


        root.addView(materialView)

        findViewById(R.id.action)!!.setOnClickListener {
            val target = if (root.layoutParams.width > 100) 100 else 350
            val a = ValueAnimator.ofInt(root.layoutParams.width, target)
            a.duration = Duration.STANDARD
            a.addUpdateListener {
                root.layoutParams.width = it.animatedValue as Int
                root.layoutParams.height = it.animatedValue as Int
                root.requestLayout()
            }
            a.start()
            /*if (material.view.parent == null) {
                materialView.setMaterial(material)
            } else if (material1.view.parent == null) {
                materialView.setMaterial(material1)
            }*/
        }

    }
}
