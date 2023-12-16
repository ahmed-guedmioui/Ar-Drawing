package com.med.drawing.image_list.domain.repository

import com.med.drawing.image_list.domain.model.images.ImageList
import com.med.drawing.util.Resource
import kotlinx.coroutines.flow.Flow

/**
 * @author Ahmed Guedmioui
 */
interface ImagesRepository {
    suspend fun getImages(): Flow<Resource<Unit>>

}