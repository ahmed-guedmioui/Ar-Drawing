package com.med.drawing.image_list.data.remote

import com.med.drawing.BuildConfig
import com.med.drawing.image_list.data.remote.respond.ImageListDto
import retrofit2.http.GET

/**
 * @author Ahmed Guedmioui
 */
interface ImagesApi {


    @GET(IMAGES_PATH)
    suspend fun getImages(): ImageListDto

    companion object {
        const val IMAGES_BASE_URL = BuildConfig.IMAGES_BASE_URL
        const val IMAGES_PATH = BuildConfig.IMAGES_PATH
    }

}