package com.med.drawing.splash.domain.repository

import com.med.drawing.util.Resource
import kotlinx.coroutines.flow.Flow

/**
 * @author Ahmed Guedmioui
 */
interface AppDataRepository {
    suspend fun getAppData(): Flow<Resource<Unit>>

}