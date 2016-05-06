package com.github.daemontus.glucose.utils.device

import android.content.Context
import android.content.Intent
import android.net.Uri

object Share {

    fun openUrl(ctx: Context, url: String) {
        ctx.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
    }

}