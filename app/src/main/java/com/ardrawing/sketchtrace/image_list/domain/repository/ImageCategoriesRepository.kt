package com.ardrawing.sketchtrace.image_list.domain.repository

import com.ardrawing.sketchtrace.util.Resource
import kotlinx.coroutines.flow.Flow

/**
 * @author Ahmed Guedmioui
 */
interface ImageCategoriesRepository {
    suspend fun getImageCategoryList(): Flow<Resource<Unit>>
    suspend fun setGalleryAndCameraAndNativeItems()

}