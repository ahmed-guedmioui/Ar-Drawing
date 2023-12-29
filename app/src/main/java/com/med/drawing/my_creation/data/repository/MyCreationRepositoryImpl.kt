package com.med.drawing.my_creation.data.repository

import android.app.Application
import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import com.arthenica.ffmpegkit.FFmpegKit
import com.med.drawing.my_creation.domian.model.Creation
import com.med.drawing.my_creation.domian.repository.CreationRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
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
                fileName, Context.MODE_PRIVATE
            )

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

//        val speedUpVideo = speedUpVideo(file)

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

    private suspend fun speedUpVideo(inputFile: File, speedFactor: Float = 1.5f): File {
        val outputFile = withContext(Dispatchers.IO) {
            File.createTempFile("sped_up_video", ".mp4")
        }

        val command =
            "ffmpeg -i ${inputFile.absolutePath} -filter_complex \"[0:v]setpts=0.5*PTS[v];[0:a]atempo=${speedFactor}[a]\" -map \"[v]\" -map \"[a]\" ${outputFile.absolutePath}"


        withContext(Dispatchers.IO) {
            FFmpegKit.execute(command)
        }

//        try {
//            withContext(Dispatchers.IO) {
//                FFmpegKit.execute(command)
//                return@withContext outputFile
//            }
//        } catch (e: Exception) {
//            e.printStackTrace()
//        }
//
        return outputFile

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


















