package com.med.drawing.util.ads

import android.app.Activity
import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.Gravity
import android.view.Window
import android.view.WindowManager
import android.widget.Button
import android.widget.ImageView
import com.facebook.ads.Ad
import com.facebook.ads.RewardedVideoAd
import com.facebook.ads.RewardedVideoAdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import com.med.drawing.R
import com.med.drawing.splash.data.DataManager

object RewardedManager {

    private lateinit var onAdClosedListener: OnAdClosedListener

    private var isFacebookRewardedLoaded = false
    private var isAdmobRewardedLoaded = false

    private lateinit var admobRewardedAd: com.google.android.gms.ads.rewarded.RewardedAd
    private lateinit var facebookRewardedAd: RewardedVideoAd


    fun loadRewarded(activity: Activity) {
        if (!DataManager.appData.showAdsForThisUser) {
            return
        }

        when (DataManager.appData.rewarded) {
            AdType.admob -> loadAdmobRewarded(activity)
            AdType.facebook -> loadFacebookRewarded(activity)
        }
    }

    fun showRewarded(
        isRewClosed: Boolean = false,
        activity: Activity,
        adClosedListener: OnAdClosedListener
    ) {
        onAdClosedListener = adClosedListener

        if (!DataManager.appData.showAdsForThisUser) {
            if (isRewClosed) {
                onAdClosedListener.onRewClosed()
            } else {
                onAdClosedListener.onRewComplete()
            }
            return
        }

        dialog(activity)
    }

    private fun dialog(activity: Activity) {
        val dialog = Dialog(activity)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(true)
        dialog.setContentView(R.layout.dialog_rewarded)
        val layoutParams = WindowManager.LayoutParams()

        layoutParams.copyFrom(dialog.window!!.attributes)
        layoutParams.width = WindowManager.LayoutParams.MATCH_PARENT
        layoutParams.height = WindowManager.LayoutParams.WRAP_CONTENT
        layoutParams.gravity = Gravity.CENTER

        dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.window!!.attributes = layoutParams

        dialog.findViewById<ImageView>(R.id.close).setOnClickListener {
            dialog.dismiss()
        }

        dialog.findViewById<Button>(R.id.watch).setOnClickListener {
            when (DataManager.appData.rewarded) {
                AdType.admob -> showAdmobRewarded(activity)
                AdType.facebook -> showFacebookRewarded(activity)
                else -> onAdClosedListener.onRewFailedToShow()
            }
            dialog.dismiss()
        }

        dialog.show()
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
            onAdClosedListener.onRewFailedToShow()
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
            onAdClosedListener.onRewFailedToShow()
        }
    }

    interface OnAdClosedListener {
        fun onRewClosed()
        fun onRewFailedToShow()
        fun onRewComplete()
    }
}
