package com.med.drawing.util

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri

class UrlOpener {

    companion object {

        fun open(activity: Activity, url: String) {
            if (url.contains("https://")) {
                val intent = Intent(Intent.ACTION_VIEW).setData(Uri.parse(url))
                activity.startActivity(intent)

            } else {
                try {
                    activity.startActivity(
                        Intent(
                            Intent.ACTION_VIEW,
                            Uri.parse("market://details?id=$url")
                        )
                    )

                } catch (anfe: ActivityNotFoundException) {
                    activity.startActivity(
                        Intent(
                            Intent.ACTION_VIEW,
                            Uri.parse("https://play.google.com/store/apps/details?id=$url")
                        )
                    )
                }
            }
        }

    }

}