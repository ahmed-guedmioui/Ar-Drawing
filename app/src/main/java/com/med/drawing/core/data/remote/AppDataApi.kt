package com.med.drawing.core.data.remote

import com.med.drawing.BuildConfig
import com.med.drawing.core.data.remote.respnod.AppDataDto
import retrofit2.http.GET

/**
 * @author Android Devs Academy (Ahmed Guedmioui)
 */
interface AppDataApi {

    @GET(ADS_PATH)
    suspend fun getAppData(): AppDataDto

    companion object {
        const val ADS_BASE_URL = BuildConfig.ADS_BASE_URL
        const val ADS_PATH = BuildConfig.ADS_PATH

        const val IMAGES_BASE_URL = BuildConfig.IMAGES_BASE_URL
        const val IMAGES_PATH = BuildConfig.IMAGES_PATH
    }

}