package com.med.drawing.image_list.data.repository

import android.app.Application
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import com.med.drawing.image_list.data.mapper.toImageCategoryList
import com.med.drawing.image_list.data.remote.ImageCategoryApi
import com.med.drawing.image_list.domain.model.images.ImageCategory
import com.med.drawing.image_list.domain.repository.ImageCategoriesRepository
import com.med.drawing.splash.data.DataManager
import com.med.drawing.image_list.data.ImagesManager
import com.med.drawing.image_list.data.remote.respond.images.ImageCategoryListDto
import com.med.drawing.util.Resource
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import okio.IOException
import retrofit2.HttpException
import java.io.InputStream
import javax.inject.Inject

/**
 * @author Ahmed Guedmioui
 */
class ImageCategoriesRepositoryImpl @Inject constructor(
    private val imageCategoryApi: ImageCategoryApi,
    private val prefs: SharedPreferences
) : ImageCategoriesRepository {

  override  suspend fun getImageCategoryList(): Flow<Resource<Unit>> {
        return flow {

            emit(Resource.Loading(true))

            val categoryListDto = try {
                imageCategoryApi.getImageCategoryList()
            } catch (e: IOException) {
                e.printStackTrace()
                emit(Resource.Error(message = "Error loading images"))
                return@flow
            } catch (e: HttpException) {
                e.printStackTrace()
                emit(Resource.Error(message = "Error loading images"))
                return@flow
            } catch (e: Exception) {
                e.printStackTrace()
                emit(Resource.Error(message = "Error loading images"))
                return@flow
            }

            ImagesManager.imageCategoryList = categoryListDto.toImageCategoryList().toMutableList()

            setUnlockedImages()


            emit(Resource.Success())

            emit(Resource.Loading(false))
            return@flow


        }
    }

     suspend fun getImageCategoryListt(): Flow<Resource<Unit>> {
        return flow {
            emit(Resource.Loading(true))

            delay(3000)

            ImagesManager.imageCategoryList =
                ImageCategoryListDto(null).toImageCategoryList().toMutableList()

            setUnlockedImages()

            emit(Resource.Success())

            emit(Resource.Loading(false))
        }
    }

    private fun setUnlockedImages() {
        ImagesManager.imageCategoryList.forEach { categoryItem ->
            categoryItem.imageList.forEach { image ->
                if (image.locked) {
                    prefs.getBoolean(image.prefsId, true).let { locked ->
                        image.locked = locked
                    }
                }
            }
        }
    }

    override suspend fun setGalleryAndCameraAndNativeItems() {

        val nativeItem = ImageCategory(
            imageCategoryName = "native",
            categoryId = -1,
            imageList = emptyList()
        )

        var index = DataManager.appData.nativeRate
        while (index < ImagesManager.imageCategoryList.size) {
            ImagesManager.imageCategoryList.add(index, nativeItem)
            index += DataManager.appData.nativeRate + 1
        }

        ImagesManager.imageCategoryList.add(
            0,
            ImageCategory(
                imageCategoryName = "gallery and camera",
                categoryId = -1,
                imageList = emptyList(),
            )
        )

        ImagesManager.imageCategoryList.add(
            1,
            ImageCategory(
                imageCategoryName = "explore",
                categoryId = -1,
                imageList = emptyList(),
            )
        )
    }
}



















