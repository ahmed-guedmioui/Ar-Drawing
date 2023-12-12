package com.med.drawing.core.data.remote.respnod

data class AppDataDto(
    val Interstitial: String?,
    val Native: String?,
    val OpenAppAd: String?,
    val Rewarded: String?,

    val admob_interstitial: String?,
    val admob_native: String?,
    val admob_open_app: String?,
    val admob_rewarded: String?,

    val facebook_interstitial: String?,
    val facebook_native: String?,
    val facebook_rewarded: String?,

    val clicks_to_show_inter: Int?,
    val native_rate: Int?,

    val countries_white_list: List<String>?,

    val onesignal_id: String?,

    val app_latest_version: Int?,

    val recommended_apps: List<RecommendedAppDto>?,
    val show_recommended_apps: Boolean?,

    val is_app_suspended: Boolean?,
    val suspended_URL: String?,
    val suspended_message: String?,
    val suspended_title: String?
)