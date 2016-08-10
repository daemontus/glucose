package com.glucose.device

import android.Manifest
import android.content.Context
import android.support.annotation.RequiresPermission
import android.telephony.TelephonyManager

object Phone {

    /**
     * Attempts to get the phone number of this device.
     * Please be aware that the phone number might not be available due to the configuration
     * of the SIM card / other reasons. However, there is no better solution, because sometimes
     * the device just doesn't know the number. In those cases, the method returns empty string.
     *
     * Note: The READ_SMS permission is new since Android 6.0.
     */
    @RequiresPermission(allOf = arrayOf(Manifest.permission.READ_PHONE_STATE, Manifest.permission.READ_SMS))
    fun getPrimaryPhoneNumber(ctx: Context): String {
        val manager = ctx.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
        return manager.line1Number ?: ""
    }

}