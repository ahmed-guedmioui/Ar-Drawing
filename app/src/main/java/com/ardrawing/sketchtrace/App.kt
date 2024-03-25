package com.ardrawing.sketchtrace

import android.app.Application
import android.content.Context
import com.ardrawing.sketchtrace.core.domain.model.app_data.AppData
import com.ardrawing.sketchtrace.databinding.ActivitySketchBinding
import com.ardrawing.sketchtrace.image_list.domain.model.images.ImageCategory
import com.ardrawing.sketchtrace.util.LanguageChanger
import com.facebook.ads.AudienceNetworkAds
import com.google.android.gms.ads.MobileAds
import com.google.firebase.FirebaseApp
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.onesignal.OneSignal
import com.revenuecat.purchases.LogLevel
import com.revenuecat.purchases.Purchases
import com.revenuecat.purchases.PurchasesConfiguration
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
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

        lateinit var imageCategoryList: MutableList<ImageCategory>
        lateinit var appData: AppData

    }

    override fun onCreate() {
        super.onCreate()

        trimCache()
        FirebaseApp.initializeApp(this)
        FirebaseCrashlytics.getInstance().setCrashlyticsCollectionEnabled(true)

        AudienceNetworkAds.initialize(this)

        Purchases.logLevel = LogLevel.DEBUG
        Purchases.configure(
            PurchasesConfiguration.Builder(
                this, BuildConfig.REVENUECUT_KEY
            ).build()
        )

        OneSignal.Debug.logLevel = com.onesignal.debug.LogLevel.VERBOSE
        // OneSignal Initialization
        OneSignal.initWithContext(
            this.baseContext, "016473e9-27ee-4bc9-8789-d20057f3dea5"
        )
        // requestPermission will show the native Android notification permission prompt.
        // NOTE: It's recommended to use a OneSignal In-App Message to prompt instead.
        CoroutineScope(Dispatchers.IO).launch {
            OneSignal.Notifications.requestPermission(true)
        }
    }

    override fun attachBaseContext(base: Context) {

        val prefs = base.getSharedPreferences(
            "ar_drawing_med_prefs_file", Context.MODE_PRIVATE
        )
        val languageCode = prefs.getString("language", "en") ?: "en"
        val newBase = LanguageChanger.changeAppLanguage(languageCode, base)

        super.attachBaseContext(newBase)

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