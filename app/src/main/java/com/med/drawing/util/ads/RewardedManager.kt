package com.med.drawing.util.ads

import android.app.Activity
import com.facebook.ads.Ad
import com.facebook.ads.RewardedVideoAd
import com.facebook.ads.RewardedVideoAdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import com.med.drawing.core.data.DataManager


object RewardedManager {

    private lateinit var onAdClosedListener: OnAdClosedListener

    private var isFacebookRewardedLoaded = false
    private var isAdmobRewardedLoaded = false

    private lateinit var admobRewardedAd: com.google.android.gms.ads.rewarded.RewardedAd
    private lateinit var facebookRewardedAd: RewardedVideoAd


    fun loadRewarded(activity: Activity) {
        when (DataManager.appData.rewarded) {
            AdType.admob -> loadAdmobRewarded(activity)
            AdType.facebook -> loadFacebookRewarded(activity)
        }
    }

    fun showRewarded(activity: Activity, adClosedListener: OnAdClosedListener) {
        onAdClosedListener = adClosedListener

        when (DataManager.appData.rewarded) {
            AdType.admob -> showAdmobRewarded(activity)
            AdType.facebook -> showFacebookRewarded(activity)
            else -> onAdClosedListener.onRewClosed()
        }
    }


    // Admob ---------------------------------------------------------------------------------------------------------------------

    private fun loadAdmobRewarded(activity: Activity) {

        isAdmobRewardedLoaded = false
        val adRequest: AdRequest = AdRequest.Builder().build()

        com.google.android.gms.ads.rewarded.RewardedAd.load(
            activity,
            DataManager.appData.admobRewarded,
            adRequest,
            object : RewardedAdLoadCallback() {

                override fun onAdLoaded(rewardedAd: com.google.android.gms.ads.rewarded.RewardedAd) {
                    admobRewardedAd = rewardedAd
                    isAdmobRewardedLoaded = true

                    rewardedAd.fullScreenContentCallback =
                        object : FullScreenContentCallback() {

                            override fun onAdDismissedFullScreenContent() {
                                isAdmobRewardedLoaded = false
                                loadRewarded(activity)
                                onAdClosedListener.onRewClosed()
                            }

                            override fun onAdShowedFullScreenContent() {
                            }

                        }
                }

                override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                    isAdmobRewardedLoaded = false
                }
            })
    }

    private fun showAdmobRewarded(activity: Activity) {
        if (isAdmobRewardedLoaded) {
            admobRewardedAd.show(activity) {
                isAdmobRewardedLoaded = false
                loadRewarded(activity)
                onAdClosedListener.onRewComplete()
            }
        } else {
            loadRewarded(activity)
            onAdClosedListener.onRewClosed()
        }
    }

    // Facebook ---------------------------------------------------------------------------------------------------------------------

    private fun loadFacebookRewarded(activity: Activity) {

        isFacebookRewardedLoaded = false
        facebookRewardedAd = RewardedVideoAd(activity, DataManager.appData.facebookRewarded)

        val rewardedVideoAdListener: RewardedVideoAdListener =
            object : RewardedVideoAdListener {
                override fun onError(ad: Ad?, adError: com.facebook.ads.AdError?) {
                    isFacebookRewardedLoaded = false
                }

                override fun onAdLoaded(ad: Ad) {
                    isFacebookRewardedLoaded = true
                }

                override fun onRewardedVideoCompleted() {
                    isFacebookRewardedLoaded = false
                    loadRewarded(activity)
                    onAdClosedListener.onRewComplete()
                }

                override fun onRewardedVideoClosed() {
                    isFacebookRewardedLoaded = false
                    loadRewarded(activity)
                    onAdClosedListener.onRewClosed()
                }

                override fun onAdClicked(ad: Ad) {}
                override fun onLoggingImpression(ad: Ad) {}

            }
        facebookRewardedAd.loadAd(
            facebookRewardedAd.buildLoadAdConfig()
                .withAdListener(rewardedVideoAdListener).build()
        )
    }

    private fun showFacebookRewarded(activity: Activity) {
        if (isFacebookRewardedLoaded) {
            facebookRewardedAd.show()
        } else {
            loadRewarded(activity)
            onAdClosedListener.onRewClosed()
        }
    }

    interface OnAdClosedListener {
        fun onRewClosed()
        fun onRewComplete()
    }
}
