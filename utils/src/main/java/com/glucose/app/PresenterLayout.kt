package com.glucose.app

import android.content.Context
import android.util.AttributeSet
import android.widget.FrameLayout

/**
 * TODO: This should take a class attribute and attach it upon creation or something like that.
 */
class PresenterLayout : FrameLayout {
    constructor(context: Context?) : super(context)
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int, defStyleRes: Int) : super(context, attrs, defStyleAttr, defStyleRes)
}