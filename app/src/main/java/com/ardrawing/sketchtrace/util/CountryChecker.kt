package com.ardrawing.sketchtrace.util

import android.app.Application
import android.content.Context
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.telephony.TelephonyManager
import com.ardrawing.sketchtrace.BuildConfig
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.URL
import java.util.Locale


/**
 * @author Ahmed Guedmioui
 */
class CountryChecker(
    private val application: Application,
    checkerType: CheckerType
) {

    private var onCheckerListener: OnCheckerListener? = null

    private val validate: List<String> =
        ArrayList(mutableListOf("com.android.vending", "com.google.android.feedback"))
    private val serverConfig = "https://www.speedtest.net/speedtest-config.php"

    init {
        when (checkerType) {
            CheckerType.SpeedServer -> setServerInfo()
            CheckerType.SimCountryIso -> setIsoInfo()
        }
    }

    interface OnCheckerListener {
        fun onCheckerCountry(country: String?, userFromGG: Boolean)
        fun onCheckerError(error: String?)
    }


    private fun setServerInfo() {
        Thread {
            try {
                val url = URL(serverConfig)
                val stream = url.openStream()
                val reader =
                    BufferedReader(InputStreamReader(stream))
                var line: String
                while (reader.readLine().also { line = it } != null) {
                    if (line.contains("<client ip")) {
                        val country =
                            line.split("country=\"".toRegex()).dropLastWhile { it.isEmpty() }
                                .toTypedArray()[1].split("\"".toRegex())
                                .dropLastWhile { it.isEmpty() }.toTypedArray()[0]
                                .lowercase(Locale.getDefault())

                        if (onCheckerListener != null) {
                            onCheckerListener!!.onCheckerCountry(country, isUserFromGG())
                        }

                        break
                    }
                }
                stream.close()
                reader.close()
            } catch (e: Exception) {

                if (onCheckerListener != null) {
                    onCheckerListener!!.onCheckerCountry(null, isUserFromGG())
                    onCheckerListener!!.onCheckerError(e.message)
                }
            }
        }.start()
    }

    private fun setIsoInfo() {
        try {
            val telephonyManager =
                application.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager

            Handler(Looper.getMainLooper()).postDelayed(Runnable {
                if (onCheckerListener != null) {
                    onCheckerListener!!.onCheckerCountry(
                        telephonyManager.simCountryIso,
                        isUserFromGG()
                    )
                }
            }, 100)
        } catch (e: Exception) {
            if (onCheckerListener != null) {
                onCheckerListener!!.onCheckerError(e.message)
            }
        }
    }

    private fun isUserFromGG(): Boolean {
        val installerManager: String?
        return try {
            installerManager = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                application.packageManager
                    .getInstallSourceInfo(BuildConfig.APPLICATION_ID).installingPackageName
            } else {
                application.packageManager.getInstallerPackageName(BuildConfig.APPLICATION_ID)
            }
            installerManager != null && validate.contains(installerManager)
        } catch (e: Exception) {
            false
        }
    }

    fun setOnCheckerListener(onCheckerListener: OnCheckerListener?) {
        this.onCheckerListener = onCheckerListener
    }

    enum class CheckerType {
        SpeedServer, SimCountryIso
    }

}