package com.ardrawing.sketchtrace.core.data.remote

import com.ardrawing.sketchtrace.BuildConfig
import com.ardrawing.sketchtrace.core.data.remote.respnod.app_data.AppDataDto
import retrofit2.http.GET

/**
 * @author Ahmed Guedmioui
 */
interface AppDataApi {

    @GET(ADS_PATH)
    suspend fun getAppData(): AppDataDto?

    companion object {
        const val ADS_BASE_URL = BuildConfig.ADS_BASE_URL
        const val ADS_PATH = BuildConfig.ADS_PATH
    }

}