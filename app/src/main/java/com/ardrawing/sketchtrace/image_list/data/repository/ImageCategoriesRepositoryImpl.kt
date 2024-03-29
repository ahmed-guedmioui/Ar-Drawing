package com.ardrawing.sketchtrace.image_list.data.repository

import android.app.Application
import android.content.SharedPreferences
import android.util.Log
import com.ardrawing.sketchtrace.App
import com.ardrawing.sketchtrace.R
import com.ardrawing.sketchtrace.image_list.data.mapper.toImageCategoryList
import com.ardrawing.sketchtrace.image_list.data.remote.ImageCategoryApi
import com.ardrawing.sketchtrace.image_list.domain.model.images.ImageCategory
import com.ardrawing.sketchtrace.image_list.domain.repository.ImageCategoriesRepository
import com.ardrawing.sketchtrace.core.domain.usecase.UpdateSubscriptionInfo
import com.ardrawing.sketchtrace.image_list.domain.model.images.Image
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

    override suspend fun loadImageCategoryList(): Flow<Resource<Unit>> {
        return flow {

            emit(Resource.Loading(true))

            val categoryListDto = try {
                imageCategoryApi.getImageCategoryList()
            } catch (e: IOException) {
                e.printStackTrace()
                emit(
                    Resource.Error(application.getString(R.string.error_loading_images))
                )
                emit(Resource.Loading(false))
                return@flow
            } catch (e: HttpException) {
                e.printStackTrace()
                emit(
                    Resource.Error(application.getString(R.string.error_loading_images))
                )
                emit(Resource.Loading(false))
                return@flow
            } catch (e: Exception) {
                e.printStackTrace()
                emit(
                    Resource.Error(application.getString(R.string.error_loading_images))
                )
                emit(Resource.Loading(false))
                return@flow
            }

            categoryListDto?.let {
                App.imageCategoryList = it.toImageCategoryList().toMutableList()
                emit(Resource.Success())
                emit(Resource.Loading(false))
                return@flow
            }

            emit(
                Resource.Error(application.getString(R.string.error_loading_images))
            )
            emit(Resource.Loading(false))
        }
    }

    override suspend fun getImageCategoryList(): List<ImageCategory> {
        return App.imageCategoryList
    }

    override suspend fun unlockImageItem(imageItem: Image) {
        prefs.edit().putBoolean(imageItem.prefsId, false).apply()
    }

    override suspend fun setUnlockedImages(date: Date?) {
        date?.let {
            UpdateSubscriptionInfo(application, it).invoke()
        }

        Log.d("tag_setUnlockedImages", "setUnlockedImages: ${App.appData.isSubscribed}")

        // When user is subscribed all images will be unlocked
        if (App.appData.isSubscribed) {
            App.imageCategoryList.forEach { categoryItem ->
                categoryItem.imageList.forEach { image ->
                    image.locked = false
                }
            }

            return
        }

        // When user is not subscribed unlock only the image the user manually unlocked by watching an ad
        App.imageCategoryList.forEach { categoryItem ->
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

        if (App.appData.isSubscribed) {

            val iterator: MutableIterator<ImageCategory> = App.imageCategoryList.iterator()

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

        var index = App.appData.nativeRate
        while (index < App.imageCategoryList.size) {
            App.imageCategoryList.add(index, nativeItem)
            index += App.appData.nativeRate + 1
        }
    }

    override suspend fun setGalleryAndCameraItems() {
        App.imageCategoryList.add(
            0,
            ImageCategory(
                imageCategoryName = "gallery and camera",
                categoryId = -1,
                imageList = emptyList(),
            )
        )

        App.imageCategoryList.add(
            1,
            ImageCategory(
                imageCategoryName = "explore",
                categoryId = -1,
                imageList = emptyList(),
            )
        )
    }

}



















