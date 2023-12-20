package com.med.drawing.camera_trace.presentation

import android.app.Activity
import android.app.ProgressDialog
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.Matrix
import android.graphics.drawable.Drawable
import android.hardware.camera2.CameraAccessException
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.os.Environment.getExternalStoragePublicDirectory
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.SeekBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.content.res.AppCompatResources
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.github.dhaval2404.imagepicker.ImagePicker
import com.med.drawing.R
import com.med.drawing.databinding.ActivityCameraBinding
import com.med.drawing.other.AppConstant
import com.med.drawing.other.FileUtils
import com.med.drawing.other.MultiTouch
import com.med.drawing.util.ads.NativeManager
import com.otaliastudios.cameraview.CameraListener
import com.otaliastudios.cameraview.VideoResult
import com.otaliastudios.cameraview.controls.Flash
import com.otaliastudios.cameraview.controls.Mode
import dagger.hilt.android.AndroidEntryPoint
import jp.co.cyberagent.android.gpuimage.GPUImage
import jp.co.cyberagent.android.gpuimage.filter.GPUImageThresholdEdgeDetectionFilter
import java.io.File
import java.io.IOException
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

/**
 * @author Ahmed Guedmioui
 */
@AndroidEntryPoint
class CameraActivity : AppCompatActivity() {

    @Inject
    lateinit var prefs: SharedPreferences

    private lateinit var binding: ActivityCameraBinding

    private lateinit var pushanim: Animation
    private var ringProgressDialog: ProgressDialog? = null
    private lateinit var bmOriginal: Bitmap
    private var isFlashSupported = false
    private var isTorchOn = false
    private var isLock = false
    private var isEditSketch = false
    private var elapsedTimeMillis: Long = 0
    private var convertedBitmap: Bitmap? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCameraBinding.inflate(layoutInflater)
        setContentView(binding.root)

        handler = Handler(Looper.getMainLooper())
        pushanim = AnimationUtils.loadAnimation(this, R.anim.view_push)
        binding.close.setOnClickListener {
            binding.nativeParent.visibility = View.GONE
        }

        NativeManager.loadNative(
            findViewById(R.id.native_frame),
            findViewById(R.id.native_temp),
            this, true
        )

        setupFlashButton()

        val imagePath = intent?.extras?.getString("imagePath")
        if (imagePath != null) {
            Glide.with(this)
                .asBitmap()
                .load(imagePath)
                .into(object : CustomTarget<Bitmap>() {
                    override fun onResourceReady(
                        resource: Bitmap,
                        transition: Transition<in Bitmap>?
                    ) {
                        bmOriginal = resource
                        binding.objImage.apply {
                            val i = Resources.getSystem().displayMetrics.widthPixels
                            setOnTouchListener(
                                MultiTouch(
                                    this, 1.0f, 1.0f,
                                    (i / 3.5).toInt().toFloat(), 600.0f
                                )
                            )

                            setImageBitmap(bmOriginal)
                            isEditSketch = false
                            binding.imgOutline.setImageResource(R.drawable.outline)
                            alpha = 0.6f
                            binding.alphaSeek.progress = 4
                        }
                    }

                    override fun onLoadCleared(placeholder: Drawable?) {}
                })
        }

        binding.animationView.visibility = View.VISIBLE
        Handler(Looper.getMainLooper()).postDelayed({
            binding.animationView.visibility = View.GONE
        }, 7000)

        binding.relCamera.setOnClickListener {
            it.startAnimation(pushanim)
            getExternalFilesDir(Environment.DIRECTORY_DCIM)?.let { it1 ->
                ImagePicker.with(this)
                    .cameraOnly()
                    .saveDir(it1)
                    .start(CAMERA_IMAGE_REQ_CODE)
            }
        }

        binding.relGallery.setOnClickListener {
            it.startAnimation(pushanim)
            ImagePicker.with(this).galleryOnly().start(GALLERY_IMAGE_REQ_CODE)
        }

        binding.relFlip.setOnClickListener {
            it.startAnimation(pushanim)
            bmOriginal = flip(bmOriginal, FLIP_HORIZONTAL) ?: return@setOnClickListener
            binding.objImage.setImageBitmap(bmOriginal)
        }

        binding.relEditRound.setOnClickListener {
            convertBorderBitmap()
        }

        binding.relLock.setOnClickListener {
            if (!isLock) {
                binding.objImage.isEnabled = false
                isLock = true
                binding.icLock.setImageResource(R.drawable.unlock)
            } else {
                binding.objImage.isEnabled = true
                isLock = false
                binding.icLock.setImageResource(R.drawable.lock)
            }
        }

        binding.alphaSeek.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onStartTrackingTouch(seekBar: SeekBar) {}

            override fun onStopTrackingTouch(seekBar: SeekBar) {}

            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                binding.objImage.alpha = (binding.alphaSeek.max - progress) / 10.0f
            }
        })

        binding.relFlash.setOnClickListener {
            switchFlash()
        }
    }

    private fun convertBorderBitmap() {
        val gPUImage = GPUImage(this)
        val show = ProgressDialog.show(this, "", getString(R.string.convert_bitmap), true)
        ringProgressDialog = show
        show.setCancelable(false)
        Thread {
            try {
                if (!isEditSketch) {
                    gPUImage.setImage(bmOriginal)
                    gPUImage.setFilter(GPUImageThresholdEdgeDetectionFilter())
                    val bitmapWithFilterApplied = gPUImage.bitmapWithFilterApplied
                    if (bitmapWithFilterApplied != null) {
                        convertedBitmap = getBitmapWithTransparentBG(bitmapWithFilterApplied, -1)
                    } else {
                        Toast.makeText(
                            this,
                            getString(R.string.can_t_convert_this_image_try_with_another),
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
            ringProgressDialog?.dismiss()
        }.start()
        ringProgressDialog?.setOnDismissListener {
            if (!isEditSketch) {
                if (convertedBitmap != null) {
                    isEditSketch = true
                    binding.objImage.setImageBitmap(convertedBitmap)
                    binding.imgOutline.setImageResource(R.drawable.normal)
                } else {
                    Toast.makeText(
                        this,
                        "Can't Convert this image, try with another",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } else {
                isEditSketch = false
                binding.objImage.setImageBitmap(bmOriginal)
                binding.imgOutline.setImageResource(R.drawable.outline)
            }
        }
    }

    private fun switchFlash() {
        try {
            isFlashSupported = (getSystemService(Context.CAMERA_SERVICE) as CameraManager)
                .getCameraCharacteristics("0")
                .get(CameraCharacteristics.FLASH_INFO_AVAILABLE) == true

            if (isTorchOn) {
                isTorchOn = false
                binding.icFlash.setImageResource(R.drawable.ic_flash_off)
                binding.cameraView.flash = Flash.OFF
            } else {
                isTorchOn = true
                binding.icFlash.setImageResource(R.drawable.ic_flash_on)
                binding.cameraView.flash = Flash.TORCH
            }
        } catch (e: CameraAccessException) {
            e.printStackTrace()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            val realPathFromURI_API19: String?
            val uri = data?.data
            if (requestCode != GALLERY_IMAGE_REQ_CODE) {
                if (requestCode != CAMERA_IMAGE_REQ_CODE) {
                    return
                }
                val bitmap = AppConstant.getBitmap(FileUtils.getPath(uri))
                bmOriginal = bitmap
                binding.objImage.setImageBitmap(bitmap)
                isEditSketch = false
            } else {
                realPathFromURI_API19 = AppConstant.getRealPathFromURI_API19(this, uri)
                val bitmap = AppConstant.getBitmap(realPathFromURI_API19)
                bmOriginal = bitmap
                binding.objImage.setImageBitmap(bitmap)
                isEditSketch = false
            }
        }
    }

    private fun setupFlashButton() {
        try {
            isFlashSupported = (getSystemService(Context.CAMERA_SERVICE) as CameraManager)
                .getCameraCharacteristics("0")
                .get(CameraCharacteristics.FLASH_INFO_AVAILABLE) == true

            if (isFlashSupported) {
                binding.relFlash.visibility = View.VISIBLE
                binding.icFlash.setImageResource(
                    if (!isTorchOn) R.drawable.ic_flash_off
                    else R.drawable.ic_flash_on
                )
            } else {
                binding.relFlash.visibility = View.GONE
            }
        } catch (e: CameraAccessException) {
            e.printStackTrace()
        }
    }

    override fun onResume() {
        super.onResume()
        if (PermissionUtils.isCameraGranted(this)) {
            binding.cameraView.open()
            binding.cameraView.clearFocus()
            setupCameraCallbacks()
        } else if (!PermissionUtils.isCameraGranted(this)) {
            PermissionUtils.checkPermission(
                this,
                "android.permission.CAMERA",
                PERMISSION_CODE_CAMERA
            )
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        if (requestCode == PERMISSION_CODE_CAMERA && (grantResults.isEmpty() || grantResults[0] != 0)) {
            Toast.makeText(this, getString(R.string.permission_not_granted), Toast.LENGTH_SHORT)
                .show()
            finish()
        }
        if (requestCode != PERMISSION_CODE_CAMERA) {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        }
    }

    override fun onPause() {
        super.onPause()
        binding.cameraView.close()
        if (ringProgressDialog != null) {
            if (ringProgressDialog?.isShowing == true) {
                ringProgressDialog?.dismiss()
            }
        }

    }


    private var isRecording = false
    private lateinit var handler: Handler
    private lateinit var timestamp: String
    private fun setupCameraCallbacks() {
        binding.cameraView.mode = Mode.VIDEO
        binding.recordVideo.setOnClickListener {
            if (isRecording) {
                stopVideo()
            } else {
                takeVideo()
            }
        }

        binding.cameraView.addCameraListener(object : CameraListener() {
            override fun onVideoTaken(result: VideoResult) {
                Toast.makeText(
                    this@CameraActivity,
                    getString(R.string.video_saved),
                    Toast.LENGTH_SHORT
                ).show()
                stopVideo()

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    saveRecordedVideo(result.file)
                } else {
                    notifyMediaScanner(result.file)
                }
            }
        })
    }

    private fun saveRecordedVideo(file: File) {
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, generateFileName())
            put(MediaStore.MediaColumns.MIME_TYPE, "video/mp4")
            put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_MOVIES)
        }

        val contentResolver = contentResolver
        var outputStream: OutputStream? = null
        val contentUri: Uri?

        try {
            // Insert the video details into the MediaStore
            contentUri =
                contentResolver.insert(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, contentValues)

            // Open an OutputStream for the content Uri
            contentUri?.let {
                outputStream = contentResolver.openOutputStream(it)
                outputStream?.write(file.readBytes())
            }
        } finally {
            outputStream?.close()
        }

    }

    private fun notifyMediaScanner(file: File) {
        // Use MediaScannerConnection to notify the media scanner
        MediaScannerConnection.scanFile(
            this,
            arrayOf(file.path),
            arrayOf("video/mp4")
        ) { _, uri ->
            // Optionally, you can broadcast an intent to notify the gallery
            val mediaScanIntent = Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, uri)
            sendBroadcast(mediaScanIntent)
        }
    }

    private fun stopVideo() {
        isRecording = false
        handler.removeCallbacks(timerRunnable)
        binding.recordVideo.setImageDrawable(
            AppCompatResources.getDrawable(
                this@CameraActivity, R.drawable.rec_button
            )
        )

        elapsedTimeMillis = 0
        binding.temp.visibility = View.GONE
        binding.temp.text = getString(R.string._00_00)
    }

    private fun takeVideo() {
        try {

            timestamp = SimpleDateFormat(
                "yyyyMMdd_HHmmss", Locale.getDefault()
            ).format(Date())
            val videoFile = File(
                getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES),
                "VIDEO_$timestamp.mp4"
            )

            binding.cameraView.takeVideo(videoFile)
            isRecording = true
            binding.recordVideo.setImageDrawable(
                AppCompatResources.getDrawable(
                    this, R.drawable.record
                )
            )


            binding.temp.visibility = View.VISIBLE
            handler.postDelayed(timerRunnable, 1000)

        } catch (e: IOException) {
            e.printStackTrace()

            Toast.makeText(
                this,
                getString(R.string.error_recording_the_video),
                Toast.LENGTH_SHORT
            ).show()

            stopVideo()
        } catch (e: Exception) {
            e.printStackTrace()

            Toast.makeText(
                this,
                getString(R.string.error_recording_the_video),
                Toast.LENGTH_SHORT
            ).show()

            stopVideo()
        }

    }

    private val timerRunnable = object : Runnable {
        override fun run() {
            // Update the elapsed time
            elapsedTimeMillis += 1000
            updateTimerText()

            // Schedule the next update after 1 second
            handler.postDelayed(this, 1000)
        }
    }

    private fun updateTimerText() {
        val seconds = elapsedTimeMillis / 1000
        val minutes = seconds / 60
        val remainingSeconds = seconds % 60
        val timerText = String.format("%02d:%02d", minutes, remainingSeconds)

        // Update your TextView with the timerText
        binding.temp.text = timerText
    }

    private fun saveRecordedVideo(videoData: ByteArray) {
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, generateFileName())
            put(MediaStore.MediaColumns.MIME_TYPE, "video/mp4")
            put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_MOVIES)
        }

        val contentResolver = contentResolver
        var outputStream: OutputStream? = null
        val contentUri: Uri?

        try {
            // Insert the video details into the MediaStore
            contentUri =
                contentResolver.insert(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, contentValues)

            // Open an OutputStream for the content Uri
            contentUri?.let {
                outputStream = contentResolver.openOutputStream(it)
                outputStream?.write(videoData)
            }
        } finally {
            outputStream?.close()
        }

    }

    private fun generateFileName(): String {
        val timestamp =
            SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        return "VIDEO_$timestamp.mp4"
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacks(timerRunnable)
    }

    companion object {
        const val CAMERA_IMAGE_REQ_CODE = 103
        const val FLIP_HORIZONTAL = 2
        const val GALLERY_IMAGE_REQ_CODE = 102
        const val PERMISSION_CODE_CAMERA = 3002
        private const val FLIP_VERTICAL = 1

        fun flip(bitmap: Bitmap?, type: Int): Bitmap? {
            if (bitmap != null) {
                val matrix = Matrix()
                when (type) {
                    FLIP_VERTICAL -> matrix.preScale(1.0f, -1.0f)
                    FLIP_HORIZONTAL -> matrix.preScale(-1.0f, 1.0f)
                    else -> return null
                }
                return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
            }
            return null
        }

        fun getBitmapWithTransparentBG(bitmap: Bitmap, color: Int): Bitmap {
            val copy = bitmap.copy(Bitmap.Config.ARGB_8888, true)
            val width = copy.width
            val height = copy.height
            for (i in 0 until height) {
                for (j in 0 until width) {
                    if (copy.getPixel(j, i) == color) {
                        copy.setPixel(j, i, 0)
                    }
                }
            }
            return copy
        }
    }

}
