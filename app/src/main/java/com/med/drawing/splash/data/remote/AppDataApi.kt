package com.med.drawing.splash.data.remote

import com.med.drawing.BuildConfig
import com.med.drawing.splash.data.remote.respnod.app_data.AppDataDto
import retrofit2.http.GET

/**
 * @author Ahmed Guedmioui
 */
interface AppDataApi {

    @GET(ADS_PATH)
    suspend fun getAppData(): AppDataDto

    companion object {
        const val ADS_BASE_URL = BuildConfig.ADS_BASE_URL
        const val ADS_PATH = BuildConfig.ADS_PATH
    }

}