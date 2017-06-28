package com.glucose2.app

import android.os.Bundle

interface ComponentGroup {

    val childrenRecursive: Sequence<Component>

    val children: Sequence<Component>

    fun performStart()

    fun performStop()

    fun performResume()

    fun performPause()

    fun addChild(component: Component)
    fun removeChild(component: Component)

    fun afterChildAttach(component: Component)
    fun beforeChildDetach(component: Component)

    fun saveInstanceState(): Bundle?

}