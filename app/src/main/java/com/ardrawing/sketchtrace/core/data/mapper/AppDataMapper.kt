package com.ardrawing.sketchtrace.core.data.mapper

import com.ardrawing.sketchtrace.core.data.remote.respnod.app_data.AppDataDto
import com.ardrawing.sketchtrace.core.data.remote.respnod.app_data.RecommendedAppDto
import com.ardrawing.sketchtrace.core.domain.model.app_data.AppData
import com.ardrawing.sketchtrace.core.domain.model.app_data.RecommendedApp

/**
 * @author Ahmed Guedmioui
 */

fun AppDataDto.toAppData(): AppData {
    return AppData(
        interstitial = Interstitial ?: "",
        native = Native ?: "",
        openAppAd = OpenAppAd ?: "",
        rewarded = Rewarded ?: "",
        admobPublisherId = admob_publisher_id ?: "",
        admobInterstitial = admob_interstitial ?: "",
        admobNative = admob_native ?: "",
        admobOpenApp = admob_open_app ?: "",
        admobRewarded = admob_rewarded ?: "",
        facebookInterstitial = facebook_interstitial ?: "",
        facebookNative = facebook_native ?: "",
        facebookRewarded = facebook_rewarded ?: "",
        clicksToShowInter = clicks_to_show_inter ?: 0,
        nativeRate = native_rate ?: 0,
        areAdsForOnlyWhiteListCountries = are_ads_for_only_white_list_countries ?: false,
        countriesWhiteList = countries_white_list ?: emptyList(),
        onesignalId = onesignal_id ?: "",
        appLatestVersion = app_latest_version ?: 0,
        recommendedApps = recommended_apps?.map { it.toRecommendedApp() } ?: emptyList(),
        showRecommendedApps = show_recommended_apps ?: false,
        isAppSuspended = is_app_suspended ?: false,
        suspendedURL = suspended_URL ?: "",
        suspendedMessage = suspended_message ?: "",
        suspendedTitle = suspended_title ?: "",
        privacyLink = privacy_link ?: ""
    )
}

fun RecommendedAppDto.toRecommendedApp(): RecommendedApp {
    return RecommendedApp(
        icon = icon ?: "",
        image = image ?: "",
        name = name ?: "",
        shortDescription = shortDescription ?: "",
        urlOrPackage = url_or_package ?: ""
    )
}









