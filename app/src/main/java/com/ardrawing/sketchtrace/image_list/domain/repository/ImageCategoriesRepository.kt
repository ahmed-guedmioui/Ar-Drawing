package com.ardrawing.sketchtrace.image_list.domain.repository

import com.ardrawing.sketchtrace.image_list.domain.model.images.Image
import com.ardrawing.sketchtrace.image_list.domain.model.images.ImageCategory
import com.ardrawing.sketchtrace.util.Resource
import kotlinx.coroutines.flow.Flow
import java.util.Date

/**
 * @author Ahmed Guedmioui
 */
interface ImageCategoriesRepository {
    suspend fun loadImageCategoryList(): Flow<Resource<Unit>>
    suspend fun getImageCategoryList(): List<ImageCategory>
    suspend fun unlockImageItem(imageItem: Image)
    suspend fun setGalleryAndCameraItems()
    suspend fun setNativeItems(date: Date? = null)
    suspend fun setUnlockedImages(date: Date? = null)

}