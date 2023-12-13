package com.med.drawing

import android.app.Application
import com.facebook.ads.AudienceNetworkAds
import com.google.android.gms.ads.MobileAds
import dagger.hilt.android.HiltAndroidApp
import java.io.File

/**
 * @author Android Devs Academy (Ahmed Guedmioui)
 */
@HiltAndroidApp
class App: Application() {

    companion object {
        const val PRIVACY =
            "We are committed to maintaining the accuracy, confidentiality, and security of your personally identifiable information (\"Personal Information\").\n" +
                    "As part of this commitment, our privacy policy governs our actions as they relate to the collection, use and disclosure of Personal Information."
    }

    override fun onCreate() {
        super.onCreate()

        trimCache()

        MobileAds.initialize(this)
        AudienceNetworkAds.initialize(this)
    }

    private fun trimCache() {
        try {
            val dir = cacheDir
            if (dir != null && dir.isDirectory) {
                deleteDir(dir)
            }
        } catch (_: Exception) {
        }
    }

    private fun deleteDir(dir: File?): Boolean {
        if (dir != null && dir.isDirectory) {
            val children = dir.list()
            if (children != null) {
                for (i in children.indices) {
                    val success = deleteDir(File(dir, children[i]))
                    if (!success) {
                        return false
                    }
                }
            }
        }
        return dir!!.delete()
    }
}