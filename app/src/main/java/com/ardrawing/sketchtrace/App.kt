package com.ardrawing.sketchtrace

import android.app.Application
import com.facebook.ads.AudienceNetworkAds
import com.google.android.gms.ads.MobileAds
import com.google.firebase.FirebaseApp
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.revenuecat.purchases.LogLevel
import com.revenuecat.purchases.Purchases
import com.revenuecat.purchases.PurchasesConfiguration
import dagger.hilt.android.HiltAndroidApp
import java.io.File


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

    }

    override fun onCreate() {
        super.onCreate()

        trimCache()
        FirebaseApp.initializeApp(this)
        FirebaseCrashlytics.getInstance().setCrashlyticsCollectionEnabled(true)

        MobileAds.initialize(this)
        AudienceNetworkAds.initialize(this)

        Purchases.logLevel = LogLevel.DEBUG
        Purchases.configure(
            PurchasesConfiguration.Builder(
                this, BuildConfig.REVENUECUT_KEY
            ).build()
        )
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