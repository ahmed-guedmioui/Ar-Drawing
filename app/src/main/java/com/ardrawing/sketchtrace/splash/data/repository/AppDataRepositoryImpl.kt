package com.ardrawing.sketchtrace.splash.data.repository

import android.app.Application
import android.content.SharedPreferences
import com.ardrawing.sketchtrace.BuildConfig
import com.ardrawing.sketchtrace.R
import com.ardrawing.sketchtrace.splash.data.DataManager
import com.ardrawing.sketchtrace.splash.data.mapper.toAppData
import com.ardrawing.sketchtrace.splash.data.remote.AppDataApi
import com.ardrawing.sketchtrace.splash.data.remote.respnod.app_data.AppDataDto
import com.ardrawing.sketchtrace.splash.domain.repository.AppDataRepository
import com.ardrawing.sketchtrace.splash.domain.usecase.ShouldShowAdsForUser
import com.ardrawing.sketchtrace.splash.domain.usecase.UpdateSubscriptionInfo
import com.ardrawing.sketchtrace.util.Resource
import com.revenuecat.purchases.CustomerInfo
import com.revenuecat.purchases.Purchases
import com.revenuecat.purchases.PurchasesError
import com.revenuecat.purchases.interfaces.ReceiveCustomerInfoCallback
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import retrofit2.HttpException
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
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

            subscription()

            emit(Resource.Success())

            emit(Resource.Loading(false))
            return@flow

        }
    }

    override suspend fun setAdsVisibilityForUser() {
        ShouldShowAdsForUser(application).invoke()
    }

    private fun subscription() {
        Purchases.sharedInstance.getCustomerInfo(
            object : ReceiveCustomerInfoCallback {
                override fun onError(error: PurchasesError) {
                    UpdateSubscriptionInfo(application, null).invoke()
                }

                override fun onReceived(customerInfo: CustomerInfo) {
                    val date = customerInfo.getExpirationDateForEntitlement(BuildConfig.ENTITLEMENT)
                    date?.let {
                        if (it.after(Date())) {
                            DataManager.appData.isSubscribed = true
                        }
                    }
                    UpdateSubscriptionInfo(application, date).invoke()
                    ShouldShowAdsForUser(application).invoke()
                }
            }
        )
    }


    suspend fun getAppTestData(): Flow<Resource<Unit>> {
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

}



















