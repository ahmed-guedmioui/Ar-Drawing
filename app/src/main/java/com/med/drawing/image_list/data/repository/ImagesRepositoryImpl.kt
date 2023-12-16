package com.med.drawing.image_list.data.repository

import com.med.drawing.image_list.data.mapper.toImageList
import com.med.drawing.image_list.data.remote.ImagesApi
import com.med.drawing.image_list.domain.repository.ImagesRepository
import com.med.drawing.util.DataManager
import com.med.drawing.util.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import okio.IOException
import retrofit2.HttpException
import javax.inject.Inject

/**
 * @author Ahmed Guedmioui
 */
class ImagesRepositoryImpl @Inject constructor(
    private val imagesApi: ImagesApi
) : ImagesRepository {

    override suspend fun getImages(): Flow<Resource<Unit>> {
        return flow {

            emit(Resource.Loading(true))

            val imageListDto = try {
                imagesApi.getImages()
            } catch (e: IOException) {
                e.printStackTrace()
                emit(Resource.Error(message = "Error loading data"))
                return@flow
            } catch (e: HttpException) {
                e.printStackTrace()
                emit(Resource.Error(message = "Error loading data"))
                return@flow
            } catch (e: Exception) {
                e.printStackTrace()
                emit(Resource.Error(message = "Error loading data"))
                return@flow
            }

            val imageList = imageListDto.toImageList()

            DataManager.images = imageList

            emit(Resource.Success())

            emit(Resource.Loading(false))
            return@flow


        }
    }

}



















