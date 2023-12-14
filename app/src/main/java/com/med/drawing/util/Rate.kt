package com.med.drawing.util

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import com.med.drawing.BuildConfig

/**
 * @author Ahmed Guedmioui
 */
fun rate(activity: Activity) {
    val uri: Uri = Uri.parse("market://details?id=${BuildConfig.APPLICATION_ID}")
    val goToMarket = Intent(Intent.ACTION_VIEW, uri)
    goToMarket.addFlags(
        Intent.FLAG_ACTIVITY_NO_HISTORY or
                Intent.FLAG_ACTIVITY_NEW_DOCUMENT or
                Intent.FLAG_ACTIVITY_MULTIPLE_TASK
    )
    try {
        activity.startActivity(goToMarket)
    } catch (e: ActivityNotFoundException) {
        activity.startActivity(
            Intent(
                Intent.ACTION_VIEW,
                Uri.parse("http://play.google.com/store/apps/details?id=${BuildConfig.APPLICATION_ID}")
            )
        )
    }
}