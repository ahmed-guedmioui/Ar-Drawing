package com.ardrawing.sketchtrace.util.ads

import android.app.Activity
import android.os.Bundle
import com.ardrawing.sketchtrace.splash.data.DataManager
import com.ardrawing.sketchtrace.util.Shared
import com.facebook.ads.Ad
import com.facebook.ads.AdError
import com.facebook.ads.InterstitialAdListener
import com.google.ads.mediation.admob.AdMobAdapter
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback

object InterManager {


    private lateinit var admobInterstitialAd: InterstitialAd
    private lateinit var facebookInterstitialAd: com.facebook.ads.InterstitialAd

    private var isFacebookInterLoaded = false
    private var isAdmobInterLoaded = false

    private lateinit var onAdClosedListener: OnAdClosedListener
    private var counter = 1

    fun loadInterstitial(activity: Activity) {
        if (!DataManager.appData.showAdsForThisUser) {
            return
        }

        when (DataManager.appData.interstitial) {
            AdType.admob -> loadAdmobInter(activity)
            AdType.facebook -> loadFacebookInter(activity)
        }
    }

    fun showInterstitial(activity: Activity, adClosedListener: OnAdClosedListener) {
        onAdClosedListener = adClosedListener

        if (!DataManager.appData.showAdsForThisUser) {
            onAdClosedListener.onAdClosed()
            return
        }

        if (DataManager.appData.clicksToShowInter == counter) {
            counter = 1

            when (DataManager.appData.interstitial) {
                AdType.admob -> showAdmobInter(activity)
                AdType.facebook -> showFacebookInter(activity)
                else -> onAdClosedListener.onAdClosed()
            }

        } else {
            counter++
            onAdClosedListener.onAdClosed()
        }
    }


    // Facebook ---------------------------------------------------------------------------------------------------------------------

    private fun loadFacebookInter(activity: Activity) {
        isFacebookInterLoaded = false
        facebookInterstitialAd =
            com.facebook.ads.InterstitialAd(activity, DataManager.appData.facebookInterstitial)
        val interstitialAdListener: InterstitialAdListener = object : InterstitialAdListener {
            override fun onInterstitialDisplayed(ad: Ad) {}
            override fun onInterstitialDismissed(ad: Ad) {
                isFacebookInterLoaded = false
                loadInterstitial(activity)
                onAdClosedListener.onAdClosed()
            }

            override fun onError(ad: Ad, adError: AdError) {
                isFacebookInterLoaded = false
            }

            override fun onAdLoaded(ad: Ad) {
                isFacebookInterLoaded = true
            }

            override fun onAdClicked(ad: Ad) {}
            override fun onLoggingImpression(ad: Ad) {}
        }
        facebookInterstitialAd.loadAd(
            facebookInterstitialAd.buildLoadAdConfig()
                .withAdListener(interstitialAdListener).build()
        )
    }

    private fun showFacebookInter(activity: Activity) {
        if (isFacebookInterLoaded) {
            facebookInterstitialAd.show()
        } else {
            loadInterstitial(activity)
            onAdClosedListener.onAdClosed()
        }
    }

    // Admob ---------------------------------------------------------------------------------------------------------------------

    private fun loadAdmobInter(activity: Activity) {
        isAdmobInterLoaded = false

        val personalized = Shared.getBoolean(activity, "personalized", true)

        val adRequest = if (personalized) {
            AdRequest.Builder().build()
        } else {
            val bundle = Bundle()
            bundle.putString("npa", "1")
            AdRequest.Builder()
                .addNetworkExtrasBundle(AdMobAdapter::class.java, bundle)
                .build()
        }

        InterstitialAd.load(
            activity,
            DataManager.appData.admobInterstitial,
            adRequest,
            object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(interstitialAd: InterstitialAd) {
                    isAdmobInterLoaded = true
                    admobInterstitialAd = interstitialAd
                    interstitialAd.fullScreenContentCallback =
                        object : FullScreenContentCallback() {
                            override fun onAdDismissedFullScreenContent() {
                                isAdmobInterLoaded = false
                                loadInterstitial(activity)
                                onAdClosedListener.onAdClosed()
                            }

                            override fun onAdFailedToShowFullScreenContent(adError: com.google.android.gms.ads.AdError) {
                                isAdmobInterLoaded = false
                                loadInterstitial(activity)
                                onAdClosedListener.onAdClosed()
                            }

                            override fun onAdShowedFullScreenContent() {}
                        }
                }

                override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                    isAdmobInterLoaded = false
                }
            })
    }

    private fun showAdmobInter(activity: Activity) {
        if (isAdmobInterLoaded) {
            admobInterstitialAd.show(activity)
        } else {
            loadInterstitial(activity)
            onAdClosedListener.onAdClosed()
        }
    }


    interface OnAdClosedListener {
        fun onAdClosed()
    }
}



