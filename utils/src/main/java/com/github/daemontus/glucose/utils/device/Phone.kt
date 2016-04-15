package com.github.daemontus.glucose.utils.device

import android.content.Context
import android.telephony.TelephonyManager

object Phone {

    fun getPrimaryPhoneNumber(ctx: Context): String {
        val manager = ctx.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
        return manager.line1Number
    }

}