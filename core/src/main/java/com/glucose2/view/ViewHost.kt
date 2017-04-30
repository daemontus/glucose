package com.glucose2.view

import android.view.View

/**
 * A simple interface which unites classes that are associated with views.
 */
interface ViewHost {

    /**
     * The view object associated with this host.
     */
    val view: View

}