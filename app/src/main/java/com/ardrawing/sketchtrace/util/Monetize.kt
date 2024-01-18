package com.ardrawing.sketchtrace.util

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import com.ardrawing.sketchtrace.App.Companion.DEVELOPER_NAME
import com.ardrawing.sketchtrace.BuildConfig

/**
 * @author Ahmed Guedmioui
 */
fun rateApp(
    activity: Activity,
    packageName: String = BuildConfig.APPLICATION_ID
) {
    val uri: Uri = Uri.parse("market://details?id=${packageName}")
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
                Uri.parse("http://play.google.com/store/apps/details?id=${packageName}")
            )
        )
    }
}

fun openDeveloper(activity: Activity) {
    try {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("market://developer?id=$DEVELOPER_NAME"))
        activity.startActivity(intent)
    } catch (e: android.content.ActivityNotFoundException) {
        // If Google Play is not installed, open the web version
        val intent = Intent(
            Intent.ACTION_VIEW,
            Uri.parse("https://play.google.com/store/apps/developer?id=$DEVELOPER_NAME")
        )
        activity.startActivity(intent)
    }
}

fun shareApp(activity: Activity) {
    val message =
        "Check out this drawing app:\n" +
                "https://play.google.com/store/apps/details?id=${BuildConfig.APPLICATION_ID}"

    val sendIntent = Intent().apply {
        action = Intent.ACTION_SEND
        putExtra(Intent.EXTRA_TEXT, message)
        type = "text/plain"
    }

    val shareIntent = Intent.createChooser(sendIntent, null)
    activity.startActivity(shareIntent)
}