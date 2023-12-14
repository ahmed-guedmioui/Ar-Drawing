package com.med.drawing.core.domain.repository

import com.med.drawing.core.domain.model.AppData
import com.med.drawing.util.AppDataResult
import kotlinx.coroutines.flow.Flow

/**
 * @author Ahmed Guedmioui
 */
interface AppDataRepository {
    suspend fun getAppData(): Flow<AppDataResult<Unit>>

}