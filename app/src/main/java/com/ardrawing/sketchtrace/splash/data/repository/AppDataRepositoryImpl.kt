package com.ardrawing.sketchtrace.splash.data.repository

import android.app.Application
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import com.ardrawing.sketchtrace.splash.data.DataManager
import com.ardrawing.sketchtrace.splash.data.mapper.toAppData
import com.ardrawing.sketchtrace.splash.data.remote.AppDataApi
import com.ardrawing.sketchtrace.splash.data.remote.respnod.app_data.AppDataDto
import com.ardrawing.sketchtrace.splash.domain.model.app_data.AppData
import com.ardrawing.sketchtrace.splash.domain.repository.AppDataRepository
import com.ardrawing.sketchtrace.splash.domain.usecase.ShouldShowAdsForUser
import com.ardrawing.sketchtrace.util.Resource
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import retrofit2.HttpException
import java.io.IOException
import java.io.InputStream
import javax.inject.Inject


/**
 * @author Ahmed Guedmioui
 */
class AppDataRepositoryImpl @Inject constructor(
    private val application: Application,
    private val appDataApi: AppDataApi,
    private val prefs: SharedPreferences
) : AppDataRepository {

   override suspend fun getAppData(): Flow<Resource<Unit>> {
        return flow {

            emit(Resource.Loading(true))

            val appDataDto = try {
                appDataApi.getAppData()
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

            DataManager.appData = appDataDto.toAppData()

            prefs.edit()
                .putString("admobOpenApp", DataManager.appData.admobOpenApp)
                .apply()

            ShouldShowAdsForUser(application).invoke()
            DataManager.appData.showAdsForThisUser = false

            emit(Resource.Success())

            emit(Resource.Loading(false))
            return@flow

        }
    }


     suspend fun getAppDataa(): Flow<Resource<Unit>> {
        return flow {

            emit(Resource.Loading(true))

            delay(3000)

            DataManager.appData = AppDataDto(
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
            ).toAppData()

            prefs.edit()
                .putString("admobOpenApp", DataManager.appData.admobOpenApp)
                .apply()

            ShouldShowAdsForUser(application).invoke()

            emit(Resource.Success())

            emit(Resource.Loading(false))
        }
    }

    private fun readJsonFromAssets(fileName: String): String? {
        return try {
            val inputStream: InputStream = application.assets.open(fileName)
            val size: Int = inputStream.available()
            val buffer = ByteArray(size)
            inputStream.read(buffer)
            inputStream.close()
            String(buffer, Charsets.UTF_8)
        } catch (e: IOException) {
            e.printStackTrace()
            null
        }
    }

    private fun parseJsonToAppData(jsonString: String?): AppData? {
        return try {
            Gson().fromJson(jsonString, AppData::class.java)
        } catch (e: JsonSyntaxException) {
            e.printStackTrace()
            null
        }
    }


}



















