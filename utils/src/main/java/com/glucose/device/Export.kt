package com.glucose.device

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri

object Export {

    object App {
        val Twitter = "com.twitter" //this is not the full package name, but it should give a choice between direct message and tweet
        val Facebook = "com.facebook.katana"
    }

    fun view(ctx: Context, url: String, appPackage: String? = null) {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).fromPackage(ctx, appPackage)
        if (ctx !is Activity) {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        ctx.startActivity(intent)
    }

    fun send(ctx: Context, text: String, appPackage: String? = null) {
        val intent = Intent(Intent.ACTION_SEND)
        intent.type = "text/plain"
        intent.putExtra(Intent.EXTRA_TEXT, text)
        if (ctx !is Activity) {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        ctx.startActivity(intent.fromPackage(ctx, appPackage))
    }

    private fun Intent.fromPackage(ctx: Context, appPackage: String?): Intent {
        return if (appPackage == null) {
            this
        } else {
            val matches = ctx.packageManager.queryIntentActivities(this, 0)
            for (info in matches) {
                if (info.activityInfo.packageName.toLowerCase().startsWith(appPackage)) {
                    this.`package` = info.activityInfo.packageName
                    break
                }
            }
            //Intent chooser will be created by the system
            this
        }
    }
}