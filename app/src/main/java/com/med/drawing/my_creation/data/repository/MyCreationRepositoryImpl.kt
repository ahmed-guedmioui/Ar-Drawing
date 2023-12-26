package com.med.drawing.my_creation.data.repository

import android.app.Application
import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import com.med.drawing.my_creation.domian.model.Creation
import com.med.drawing.my_creation.domian.repository.CreationRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

/**
 * @author Ahmed Guedmioui
 */
class MyCreationRepositoryImpl @Inject constructor(
    private val application: Application
) : CreationRepository {


    override suspend fun insertPhotoCreation(bitmap: Bitmap) {

        val timestamp = SimpleDateFormat(
            "yyyyMMdd_HHmmss", Locale.getDefault()
        ).format(Date())
        val fileName = "creation_image_$timestamp.png"

        var outputStream: FileOutputStream? = null

        try {
            outputStream = application.openFileOutput(
                fileName, Context.MODE_PRIVATE)

            val byteArray = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArray)

            withContext(Dispatchers.IO) {
                outputStream.write(byteArray.toByteArray())
                outputStream.close()
            }

        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            withContext(Dispatchers.IO) {
                outputStream?.close()
            }
        }
    }

    override suspend fun insertVideoCreation(file: File) {
        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val fileName = "VIDEO_$timestamp.mp4"

        var outputStream: FileOutputStream? = null

        try {
            outputStream = application.openFileOutput(fileName, Context.MODE_PRIVATE)
            withContext(Dispatchers.IO) {
                outputStream.write(file.readBytes())
            }

        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            withContext(Dispatchers.IO) {
                outputStream?.close()
            }
        }
    }

    override suspend fun deleteCreation(uri: String): Boolean {
        val file = Uri.parse(uri).path?.let { File(it) }

        if (file != null) {
            if (file.exists()) {
                file.delete()
                return true
            }
        }

        return false
    }

    override suspend fun getCreationList(): Flow<List<Creation>> {

        return flow {

            val creationList = mutableListOf<Creation>()
            val files = application.filesDir.listFiles()

            files?.forEach { file ->
                when {
                    file.isFile && file.name.endsWith(".mp4") -> {
                        val uri = Uri.fromFile(File(application.filesDir, file.name))
                        creationList.add(
                            Creation(uri = uri, isVideo = true)
                        )
                    }

                    file.isFile && file.name.endsWith(".png") -> {
                        val uri = Uri.fromFile(File(application.filesDir, file.name))
                        creationList.add(
                            Creation(uri = uri, isVideo = false)
                        )
                    }
                }
            }

            emit(creationList)
        }

    }
}


















