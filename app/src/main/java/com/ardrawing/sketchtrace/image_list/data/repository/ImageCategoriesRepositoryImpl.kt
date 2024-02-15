package com.ardrawing.sketchtrace.image_list.data.repository

import android.app.Application
import android.content.SharedPreferences
import android.util.Log
import com.ardrawing.sketchtrace.image_list.data.ImagesManager
import com.ardrawing.sketchtrace.image_list.data.mapper.toImageCategoryList
import com.ardrawing.sketchtrace.image_list.data.remote.ImageCategoryApi
import com.ardrawing.sketchtrace.image_list.domain.model.images.ImageCategory
import com.ardrawing.sketchtrace.image_list.domain.repository.ImageCategoriesRepository
import com.ardrawing.sketchtrace.splash.data.DataManager
import com.ardrawing.sketchtrace.splash.domain.usecase.UpdateSubscriptionInfo
import com.ardrawing.sketchtrace.util.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import okio.IOException
import retrofit2.HttpException
import java.util.Date
import javax.inject.Inject


/**
 * @author Ahmed Guedmioui
 */
class ImageCategoriesRepositoryImpl @Inject constructor(
    private val application: Application,
    private val imageCategoryApi: ImageCategoryApi,
    private val prefs: SharedPreferences
) : ImageCategoriesRepository {

    override suspend fun getImageCategoryList(): Flow<Resource<Unit>> {
        return flow {

            emit(Resource.Loading(true))

            val categoryListDto = try {
                imageCategoryApi.getImageCategoryList()
            } catch (e: IOException) {
                e.printStackTrace()
                emit(Resource.Error(message = "Error loading images"))
                emit(Resource.Loading(false))
                return@flow
            } catch (e: HttpException) {
                e.printStackTrace()
                emit(Resource.Error(message = "Error loading images"))
                emit(Resource.Loading(false))
                return@flow
            } catch (e: Exception) {
                e.printStackTrace()
                emit(Resource.Error(message = "Error loading images"))
                emit(Resource.Loading(false))
                return@flow
            }

            ImagesManager.imageCategoryList = categoryListDto.toImageCategoryList().toMutableList()

            emit(Resource.Success())
            emit(Resource.Loading(false))
            return@flow


        }
    }

    override suspend fun setUnlockedImages(date: Date?) {
        date?.let {
            UpdateSubscriptionInfo(application, it).invoke()
        }

        Log.d("tag_setUnlockedImages", "setUnlockedImages: ${DataManager.appData.isSubscribed}")

        // When user is subscribed all images will be unlocked
        if (DataManager.appData.isSubscribed) {
            ImagesManager.imageCategoryList.forEach { categoryItem ->
                categoryItem.imageList.forEach { image ->
                    image.locked = false
                }
            }

            return
        }

        // When user is not subscribed unlock only the image the user manually unlocked by watching an ad
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

    override suspend fun setNativeItems(date: Date?) {
        date?.let {
            UpdateSubscriptionInfo(application, it).invoke()
        }

        if (DataManager.appData.isSubscribed) {

            val iterator: MutableIterator<ImageCategory> = ImagesManager.imageCategoryList.iterator()

            while (iterator.hasNext()) {
                val categoryItem: ImageCategory = iterator.next()
                if (categoryItem.imageCategoryName == "native") {
                    iterator.remove() // Safely remove the element using the iterator
                }
            }


            return
        }

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
    }

    override suspend fun setGalleryAndCameraItems() {
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



















