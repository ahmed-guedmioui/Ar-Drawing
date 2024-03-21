package com.ardrawing.sketchtrace.core.domain.model.app_data

data class AppData(
    val interstitial: String,
    val native: String,
    val openAppAd: String,
    val rewarded: String,

    val admobPublisherId: String,
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

    val areAdsForOnlyWhiteListCountries: Boolean,
    val recommendedApps: List<RecommendedApp>,

    val showRecommendedApps: Boolean,

    val isAppSuspended: Boolean,
    val suspendedURL: String,
    val suspendedMessage: String,
    val suspendedTitle: String,


    val privacyLink: String,


    // these one is not gotten from json configuration, we assign a value to based on the user's
    // country.
    var showAdsForThisUser: Boolean = false,
    var isSubscribed: Boolean = false,

    var subscriptionExpireDate: String = ""
)