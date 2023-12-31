package com.med.drawing.my_creation.data.repository

import android.app.Application
import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.util.Log
import androidx.core.net.toUri
import com.med.drawing.my_creation.domian.model.Creation
import com.med.drawing.my_creation.domian.repository.CreationRepository
import com.otaliastudios.transcoder.Transcoder
import com.otaliastudios.transcoder.TranscoderListener
import com.otaliastudios.transcoder.common.TrackStatus
import com.otaliastudios.transcoder.internal.media.MediaFormatConstants
import com.otaliastudios.transcoder.source.UriDataSource
import com.otaliastudios.transcoder.strategy.DefaultAudioStrategy
import com.otaliastudios.transcoder.strategy.DefaultVideoStrategy
import com.otaliastudios.transcoder.validator.DefaultValidator
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

    override suspend fun insertVideoCreation(
        file: File,
        isFast: Boolean,
        onVideoFinished: () -> Unit
    ) {

        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val fileNameOutput = "VIDEO_$timestamp.mp4"

        withContext(Dispatchers.IO) {
            val outputStream = application.openFileOutput(fileNameOutput, Context.MODE_PRIVATE)

            if (!isFast) {
                outputStream.write(file.readBytes())
                onVideoFinished()
            } else {

                speedUpVideo(file, fileNameOutput) { transcodeOutputFile ->
                    outputStream.write(transcodeOutputFile?.readBytes())
                    onVideoFinished()
                }
            }
        }

    }

    private fun speedUpVideo(
        videoToSpeedUp: File,
        fileNameOutput: String,
        onVideoFinished: (transcodeOutputFile: File?) -> Unit
    ) {
        try {

            val outputDir = File(application.getExternalFilesDir(null), "outputs")
            outputDir.mkdir()
            val transcodeOutputFile = File.createTempFile(fileNameOutput, ".mp4", outputDir)

            val transcodeAudioStrategy = DefaultAudioStrategy.builder()
                .channels(DefaultAudioStrategy.CHANNELS_AS_INPUT)
                .sampleRate(DefaultAudioStrategy.SAMPLE_RATE_AS_INPUT)
                .build()

            val transcodeVideoStrategy = DefaultVideoStrategy.Builder().frameRate(30).build()

            val builder = Transcoder.into(transcodeOutputFile.absolutePath)
            val source = UriDataSource(application, videoToSpeedUp.toUri())

            builder
                .setListener(object : TranscoderListener {
                    override fun onTranscodeProgress(progress: Double) {
                        Log.d("tag_speed", "onTranscodeProgress: $progress")
                    }

                    override fun onTranscodeCompleted(successCode: Int) {
                        onVideoFinished(transcodeOutputFile)
                    }

                    override fun onTranscodeCanceled() {
                        onVideoFinished(null)
                    }

                    override fun onTranscodeFailed(exception: Throwable) {
                        onVideoFinished(null)
                    }
                })
                .addDataSource(source)
                .setAudioTrackStrategy(transcodeAudioStrategy)
                .setVideoTrackStrategy(transcodeVideoStrategy)
                .setValidator(object : DefaultValidator() {
                    override fun validate(
                        videoStatus: TrackStatus, audioStatus: TrackStatus
                    ): Boolean {
                        return super.validate(videoStatus, audioStatus)
                    }
                })
                .setSpeed(1.7f)
                .transcode()

        } catch (e: Exception) {
            e.printStackTrace()
            onVideoFinished(null)
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


















