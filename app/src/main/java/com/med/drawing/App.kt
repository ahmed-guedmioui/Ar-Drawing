package com.med.drawing

import android.app.Application
import android.content.Context
import android.util.Log
import com.facebook.ads.AudienceNetworkAds
import com.google.android.gms.ads.MobileAds
import com.google.firebase.FirebaseApp
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.med.drawing.util.LocaleManager
import dagger.hilt.android.HiltAndroidApp
import java.io.File
import javax.inject.Inject


/**
 * @author Ahmed Guedmioui
 */
@HiltAndroidApp
class App : Application() {

    companion object {
        const val DEVELOPER_NAME = "AhmedGuedmioui"

        const val tiktok = "realmadrid"
        const val facebook = "RealMadrid"
        const val instagram = "realmadrid"
        const val twitter = "realmadrid"

        const val PRIVACY =
            "We are committed to maintaining the accuracy, confidentiality, and security of your personally identifiable information (\"Personal Information\").\n" +
                    "As part of this commitment, our privacy policy governs our actions as they relate to the collection, use and disclosure of Personal Information."

    }

    override fun onCreate() {
        super.onCreate()

        trimCache()

        FirebaseApp.initializeApp(this)
        FirebaseCrashlytics.getInstance().setCrashlyticsCollectionEnabled(true)

        MobileAds.initialize(this)
        AudienceNetworkAds.initialize(this)

    }

    override fun attachBaseContext(base: Context) {
        super.attachBaseContext(LocaleManager.setLocale(base, getCurrentLanguage(base)))
    }

    private fun getCurrentLanguage(base: Context): String {
        val prefs = base.getSharedPreferences(
            "ar_drawing_med_prefs_file", Context.MODE_PRIVATE
        )
        return prefs.getString("language", "en") ?: "en"
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