package com.med.drawing.core.data.repository

import android.content.SharedPreferences
import com.med.drawing.core.data.mapper.toAppData
import com.med.drawing.core.data.remote.AppDataApi
import com.med.drawing.core.domain.model.AppData
import com.med.drawing.core.domain.repository.AppDataRepository
import com.med.drawing.util.AppDataManager
import com.med.drawing.util.AppDataResult
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import okio.IOException
import retrofit2.HttpException
import javax.inject.Inject

/**
 * @author Android Devs Academy (Ahmed Guedmioui)
 */
class AppDataRepositoryImpl @Inject constructor(
    private val appDataApi: AppDataApi,
    private val prefs: SharedPreferences
) : AppDataRepository {

    override suspend fun getAppData(): Flow<AppDataResult<Unit>> {
        return flow {

            emit(AppDataResult.Loading(true))

            val appDataDto = try {
                appDataApi.getAppData()
            } catch (e: IOException) {
                e.printStackTrace()
                emit(AppDataResult.Error(message = "Error loading data"))
                return@flow
            } catch (e: HttpException) {
                e.printStackTrace()
                emit(AppDataResult.Error(message = "Error loading data"))
                return@flow
            } catch (e: Exception) {
                e.printStackTrace()
                emit(AppDataResult.Error(message = "Error loading data"))
                return@flow
            }

            val appData = appDataDto.toAppData()

            AppDataManager.appData = appData

            prefs.edit()
                .putString("admobOpenApp", appData.admobOpenApp)
                .apply()

            emit(AppDataResult.Success())

            emit(AppDataResult.Loading(false))
            return@flow


        }
    }

}



















