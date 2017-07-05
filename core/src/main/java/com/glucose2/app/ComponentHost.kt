package com.glucose2.app

import android.app.Activity
import com.glucose2.app.transaction.TransactionHost

interface ComponentHost : TransactionHost {

    val activity: Activity

    val factory: ComponentFactory

}


