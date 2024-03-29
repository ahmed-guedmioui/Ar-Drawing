package com.ardrawing.sketchtrace.image_list.data.remote

import com.ardrawing.sketchtrace.BuildConfig
import com.ardrawing.sketchtrace.image_list.data.remote.respond.images.ImageCategoryListDto
import retrofit2.http.GET

/**
 * @author Ahmed Guedmioui
 */
interface ImageCategoryApi {


    @GET(IMAGES_PATH)
    suspend fun getImageCategoryList(): ImageCategoryListDto?

    companion object {
        const val IMAGES_BASE_URL = BuildConfig.IMAGES_BASE_URL
        const val IMAGES_PATH = BuildConfig.IMAGES_PATH
    }

}