package com.med.drawing.splash.data.repository

import android.content.SharedPreferences
import com.med.drawing.splash.data.mapper.toAppData
import com.med.drawing.splash.data.remote.AppDataApi
import com.med.drawing.splash.domain.repository.AppDataRepository
import com.med.drawing.splash.data.DataManager
import com.med.drawing.util.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import okio.IOException
import retrofit2.HttpException
import javax.inject.Inject

/**
 * @author Ahmed Guedmioui
 */
class AppDataRepositoryImpl @Inject constructor(
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

            val appData = appDataDto.toAppData()

            DataManager.appData = appData

            prefs.edit()
                .putString("admobOpenApp", appData.admobOpenApp)
                .apply()

            emit(Resource.Success())

            emit(Resource.Loading(false))
            return@flow


        }
    }

}



















