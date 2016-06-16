package com.github.daemontus.glucose

import android.graphics.Color
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.github.daemontus.glucose.utils.Log
import com.github.daemontus.glucose.utils.MaterialColor
import com.github.daemontus.glucose.utils.device.Units

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val root = findViewById(R.id.root) as ViewGroup

        val map = mapOf(
                "Red" to MaterialColor.Red,
                "Pink" to MaterialColor.Pink,
                "Purple" to MaterialColor.Purple,
                "DeepPurple" to MaterialColor.DeepPurple,
                "Indigo" to MaterialColor.Indigo,
                "Blue" to MaterialColor.Blue,
                "LightBlue" to MaterialColor.LightBlue,
                "Cyan" to MaterialColor.Cyan,
                "Teal" to MaterialColor.Teal,
                "Green" to MaterialColor.Green,
                "LightGreen" to MaterialColor.LightGreen,
                "Lime" to MaterialColor.Lime,
                "Yellow" to MaterialColor.Yellow,
                "Amber" to MaterialColor.Amber,
                "Orange" to MaterialColor.Orange,
                "DeepOrange" to MaterialColor.DeepOrange,
                "Brown" to MaterialColor.Brown,
                "Grey" to MaterialColor.Grey,
                "BlueGrey" to MaterialColor.BlueGrey
        )

        map.entries.forEach {
            val (name, palette) = it
            renderColorPalette(name, palette, root)
            if (palette is MaterialColor.AccentPalette) {
                renderAccentPalette(name, palette, root)
            }
        }
    }

    private fun renderAccentPalette(name: String, palette: MaterialColor.AccentPalette, root: ViewGroup) {
        root.addView(renderColor("$name A100", palette.A100))
        root.addView(renderColor("$name A200", palette.A200))
        root.addView(renderColor("$name A400", palette.A400))
        root.addView(renderColor("$name A700", palette.A700))
    }

    private fun renderColorPalette(name: String, palette: MaterialColor.ColorPalette, root: ViewGroup) {
        root.addView(renderColor("$name C50", palette.C50))
        root.addView(renderColor("$name C100", palette.C100))
        root.addView(renderColor("$name C200", palette.C200))
        root.addView(renderColor("$name C300", palette.C300))
        root.addView(renderColor("$name C400", palette.C400))
        root.addView(renderColor("$name C500", palette.C500))
        root.addView(renderColor("$name C600", palette.C600))
        root.addView(renderColor("$name C700", palette.C700))
        root.addView(renderColor("$name C800", palette.C800))
        root.addView(renderColor("$name C900", palette.C900))
    }

    private fun renderColor(name: String, color: Int): View {
        val view = TextView(this)
        view.text = name
        view.setBackgroundColor(color)
        val dp8 = Units.dpToPx(this, 8f).toInt()
        view.setPadding(dp8, dp8, dp8, dp8)
        printColor(name, color)
        return view
    }

    private fun printColor(name: String, color: Int) {
        val strColor = String.format("#%06X", 0xFFFFFF and color);
        Log.i("<color name\"${name.toLowerCase().replace(' ', '_')}\">$strColor</color>")
    }
}
