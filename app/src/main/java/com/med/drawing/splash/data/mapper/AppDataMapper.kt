package com.med.drawing.splash.data.mapper

import com.med.drawing.splash.data.remote.respnod.app_data.AppDataDto
import com.med.drawing.splash.data.remote.respnod.app_data.RecommendedAppDto
import com.med.drawing.splash.domain.model.app_data.AppData
import com.med.drawing.splash.domain.model.app_data.RecommendedApp

/**
 * @author Ahmed Guedmioui
 */

fun AppDataDto.toAppData(): AppData {
    return AppData(
        interstitial = Interstitial ?: "",
        native = Native ?: "",
        openAppAd = OpenAppAd ?: "",
        rewarded = Rewarded ?: "",
        admobInterstitial = admob_interstitial ?: "",
        admobNative = admob_native ?: "",
        admobOpenApp = admob_open_app ?: "",
        admobRewarded = admob_rewarded ?: "",
        facebookInterstitial = facebook_interstitial ?: "",
        facebookNative = facebook_native ?: "",
        facebookRewarded = facebook_rewarded ?: "",
        clicksToShowInter = clicks_to_show_inter ?: 0,
        nativeRate = native_rate ?: 0,
        countriesWhiteList = countries_white_list ?: emptyList(),
        onesignalId = onesignal_id ?: "",
        appLatestVersion = app_latest_version ?: 0,
        recommendedApps = recommended_apps?.map { it.toRecommendedApp() } ?: emptyList(),
        showRecommendedApps = show_recommended_apps ?: false,
        isAppSuspended = is_app_suspended ?: false,
        suspendedURL = suspended_URL ?: "",
        suspendedMessage = suspended_message ?: "",
        suspendedTitle = suspended_title ?: ""
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









