package com.ardrawing.sketchtrace.splash.domain.repository

import com.ardrawing.sketchtrace.util.Resource
import kotlinx.coroutines.flow.Flow

/**
 * @author Ahmed Guedmioui
 */
interface AppDataRepository {
    suspend fun getAppData(): Flow<Resource<Unit>>
    suspend fun setAdsVisibilityForUser()
}