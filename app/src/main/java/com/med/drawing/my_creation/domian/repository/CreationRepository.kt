package com.med.drawing.my_creation.domian.repository

import android.graphics.Bitmap
import com.med.drawing.my_creation.domian.model.Creation
import kotlinx.coroutines.flow.Flow
import java.io.File

/**
 * @author (Ahmed Guedmioui)
 */
interface CreationRepository {

    suspend fun insertPhotoCreation(bitmap: Bitmap)
    suspend fun insertVideoCreation(
        file: File, isFast: Boolean, onVideoFinished: () -> Unit
    )

    suspend fun deleteCreation(uri: String)

    suspend fun deleteTempCreation(uri: String): Boolean

    suspend fun getCreationList(): Flow<List<Creation>>

}