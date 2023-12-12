package com.med.drawing.core.domain.model

data class AppData(
    val interstitial: String,
    val native: String,
    val openAppAd: String,
    val rewarded: String,

    val admobInterstitial: String,
    val admobNative: String,
    val admobOpenApp: String,
    val admobRewarded: String,

    val facebookInterstitial: String,
    val facebookNative: String,
    val facebookRewarded: String,

    val clicksToShowInter: Int,
    val nativeRate: Int,

    val countriesWhiteList: List<String>,

    val onesignalId: String,

    val appLatestVersion: Int,

    val recommendedApps: List<RecommendedApp>,
    val showRecommendedApps: Boolean,

    val isAppSuspended: Boolean,
    val suspendedURL: String,
    val suspendedMessage: String,
    val suspendedTitle: String
)