package com.med.drawing.sketch.presentation

import android.app.Activity
import android.app.Dialog
import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.hardware.camera2.CameraAccessException
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.net.Uri
import android.os.Bundle
import android.os.CountDownTimer
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.view.Gravity
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.Button
import android.widget.ImageView
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.app.ActivityCompat
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.github.dhaval2404.imagepicker.ImagePicker
import com.google.android.play.core.review.ReviewManagerFactory
import com.med.drawing.R
import com.med.drawing.advanced_editing.presentation.AdvancedEditingActivity
import com.med.drawing.databinding.ActivitySketchBinding
import com.med.drawing.my_creation.domian.repository.CreationRepository
import com.med.drawing.util.Constants
import com.med.drawing.util.PermissionUtils
import com.med.drawing.util.ads.InterManager
import com.med.drawing.util.ads.NativeManager
import com.med.drawing.util.ads.RewardedManager
import com.med.drawing.util.other.MultiTouch
import com.otaliastudios.cameraview.CameraListener
import com.otaliastudios.cameraview.VideoResult
import com.otaliastudios.cameraview.controls.Flash
import com.otaliastudios.cameraview.controls.Mode
import dagger.hilt.android.AndroidEntryPoint
import jp.co.cyberagent.android.gpuimage.GPUImage
import jp.co.cyberagent.android.gpuimage.filter.GPUImageThresholdEdgeDetectionFilter
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

/**
 * @author Ahmed Guedmioui
 */
@AndroidEntryPoint
class SketchActivity : AppCompatActivity() {

    @Inject
    lateinit var creationRepository: CreationRepository

    private lateinit var binding: ActivitySketchBinding

    private lateinit var pushanim: Animation
    private var ringProgressDialog: ProgressDialog? = null
    private var isFlashSupported = false
    private var isTorchOn = false
    private var isLock = false
    private var isEditSketch = false
    private var convertedBitmap: Bitmap? = null

    private var elapsedTimeMillis: Long = 0
    private var isRecording = false
    private lateinit var handler: Handler
    private lateinit var timestamp: String

    private lateinit var countDownTimer: CountDownTimer

    private var isDialogShowing = false
    private var isTimeIsUp = false
    private var isTimeIsUpDialogShowing = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySketchBinding.inflate(layoutInflater)
        setContentView(binding.root)

        updateMainTimerText("03:00", 300000)
        countDown()

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

        binding.theDrawingIsReadyBtn.setOnClickListener {
            takePhotoDialog()
        }

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
                        Constants.bitmap = resource
                        binding.objImage.apply {
                            val i = Resources.getSystem().displayMetrics.widthPixels
                            setOnTouchListener(
                                MultiTouch(
                                    this, 1.0f, 1.0f,
                                    (i / 3.5).toInt().toFloat(), 600.0f
                                )
                            )

                            setImageBitmap(Constants.bitmap)

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
            rewarded {
                getExternalFilesDir(Environment.DIRECTORY_DCIM)?.let { it1 ->
                    ImagePicker.with(this)
                        .cameraOnly()
                        .saveDir(it1)
                        .createIntent { intent ->
                            startForGetPhotoResult.launch(intent)
                        }
                }
            }
        }

        binding.relGallery.setOnClickListener {
            it.startAnimation(pushanim)
            rewarded {
                ImagePicker.with(this)
                    .galleryOnly()
                    .createIntent { intent ->
                        startForGetPhotoResult.launch(intent)
                    }
            }
        }

        binding.relFlip.setOnClickListener {
            it.startAnimation(pushanim)
            Constants.bitmap = flip(Constants.bitmap, FLIP_HORIZONTAL) ?: return@setOnClickListener
            binding.objImage.setImageBitmap(Constants.bitmap)
        }

        binding.relEditRound.setOnClickListener {
            convertBorderBitmap()
        }

        binding.advanced.setOnClickListener {
            startActivity(
                Intent(this, AdvancedEditingActivity::class.java)
            )
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
                    gPUImage.setImage(Constants.bitmap)
                    gPUImage.setFilter(GPUImageThresholdEdgeDetectionFilter())
                    val bitmapWithFilterApplied = gPUImage.bitmapWithFilterApplied
                    if (bitmapWithFilterApplied != null) {
                        convertedBitmap = getBitmapWithTransparentBG(bitmapWithFilterApplied, -1)
                    } else {
                        Toast.makeText(
                            this@SketchActivity,
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
                        getString(R.string.can_t_convert_this_image_try_with_another),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } else {
                isEditSketch = false
                binding.objImage.setImageBitmap(Constants.bitmap)
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

    private val startForGetPhotoResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
            val resultCode = result.resultCode

            if (resultCode == Activity.RESULT_OK) {
                //Image Uri will not be null for RESULT_OK
                val uri = result.data?.data!!

                Glide.with(this)
                    .asBitmap()
                    .load(uri.toString())
                    .into(object : CustomTarget<Bitmap>() {
                        override fun onResourceReady(
                            resource: Bitmap,
                            transition: Transition<in Bitmap>?
                        ) {
                            Constants.bitmap = resource
                            binding.objImage.apply {
                                val i = Resources.getSystem().displayMetrics.widthPixels
                                setOnTouchListener(
                                    MultiTouch(
                                        this, 1.0f, 1.0f,
                                        (i / 3.5).toInt().toFloat(), 600.0f
                                    )
                                )

                                setImageBitmap(Constants.bitmap)
                                isEditSketch = false
                                binding.imgOutline.setImageResource(R.drawable.outline)
                                alpha = 0.6f
                                binding.alphaSeek.progress = 4
                            }
                        }

                        override fun onLoadCleared(placeholder: Drawable?) {}
                    })
            } else {
                Toast.makeText(this, "Error taking image", Toast.LENGTH_SHORT).show()
            }
        }


    private val startForTakeAndSaveDrawingPhotoResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->

            if (result.resultCode == Activity.RESULT_OK) {
                //Image Uri will not be null for RESULT_OK
                val uri = result.data?.data!!

                Glide.with(this)
                    .asBitmap()
                    .load(uri.toString())
                    .into(object : CustomTarget<Bitmap>() {
                        override fun onResourceReady(
                            resource: Bitmap,
                            transition: Transition<in Bitmap>?
                        ) {
                            savePhotoDialog(resource)
                        }

                        override fun onLoadCleared(placeholder: Drawable?) {}
                    })
            } else {
                Toast.makeText(
                    this, getString(R.string.error_importing_photo), Toast.LENGTH_SHORT
                ).show()
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


    private fun setupCameraCallbacks() {
        binding.cameraView.mode = Mode.VIDEO
        binding.recordVideo.setOnClickListener {
            if (isRecording) {
                binding.cameraView.stopVideo()
                stopVideo()
            } else {
                rewarded {
                    Handler(Looper.getMainLooper()).postDelayed({
                        takeVideo()
                    }, 500)
                }
            }
        }

        binding.cameraView.addCameraListener(object : CameraListener() {
            override fun onVideoTaken(result: VideoResult) {

                saveRecordedVideo(result.file)

            }
        })
    }

    private fun saveRecordedVideo(file: File) {

        val progressDialog = ProgressDialog(this)
        progressDialog.setMessage(getString(R.string.speeding_up_and_saving_video))
        progressDialog.setCancelable(false)
        progressDialog.setOnDismissListener {
            Toast.makeText(
                application, application.getString(R.string.video_saved), Toast.LENGTH_SHORT
            ).show()
            binding.fastVideoCheck.isChecked = false
        }

        if (binding.fastVideoCheck.isChecked) {
            progressDialog.show()
        } else {
            Toast.makeText(
                application, application.getString(R.string.video_saved), Toast.LENGTH_SHORT
            ).show()
            binding.fastVideoCheck.isChecked = false
        }

        lifecycleScope.launch {
            creationRepository.insertVideoCreation(file, binding.fastVideoCheck.isChecked) {
                runBlocking {
                    creationRepository.deleteTempCreation(filePath)
                }

                if (binding.fastVideoCheck.isChecked) {
                    progressDialog.dismiss()
                }
            }
        }
    }

    private fun stopVideo() {
        isRecording = false
        handler.removeCallbacks(timerRunnable)
        binding.recordVideoImage.setImageDrawable(
            AppCompatResources.getDrawable(
                this@SketchActivity, R.drawable.rec
            )
        )

        elapsedTimeMillis = 0
        binding.temp.visibility = View.GONE
        binding.fastVideoCheck.visibility = View.GONE
        binding.temp.text = getString(R.string._00_00)
    }

    private lateinit var filePath: String
    private fun takeVideo() {
        try {

            timestamp = SimpleDateFormat(
                "yyyyMMdd_HHmmss", Locale.getDefault()
            ).format(Date())
            val videoFile = File(filesDir, "VIDEO_$timestamp.mp4")

            filePath = Uri.fromFile(videoFile).toString()

            binding.cameraView.takeVideo(videoFile)
            isRecording = true
            binding.recordVideoImage.setImageDrawable(
                AppCompatResources.getDrawable(
                    this, R.drawable.rec_stop
                )
            )


            binding.temp.visibility = View.VISIBLE
            binding.fastVideoCheck.visibility = View.VISIBLE
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

    private fun countDown() {
        isTimeIsUp = false
        binding.theDrawingIsReadyBtn.visibility = View.GONE

        val countdownDurationMillis: Long = 3 * 60 * 1000
        // Set the countdown interval (e.g., 1 second)
        val countdownIntervalMillis: Long = 1000

        countDownTimer = object : CountDownTimer(countdownDurationMillis, countdownIntervalMillis) {
            override fun onTick(millisUntilFinished: Long) {
                val minutes = (millisUntilFinished / 1000) / 60
                val seconds = (millisUntilFinished / 1000) % 60
                val formattedTime = "%02d:%02d".format(minutes, seconds)

                updateMainTimerText(formattedTime, millisUntilFinished)
            }

            override fun onFinish() {
                isTimeIsUp = true
                updateMainTimerText("00:00", 0)
                if (!isDialogShowing && !isTimeIsUpDialogShowing) {
                    timeDialog()
                }
            }
        }

        // Start the countdown timer
        countDownTimer.start()
    }

    private fun updateMainTimerText(timerText: String, millisUntilFinished: Long) {
        // Update your TextView with the timerText
        binding.mainTemp.text = timerText

        if (millisUntilFinished <= 90000) {
            binding.theDrawingIsReadyBtn.visibility = View.VISIBLE
        }
    }

    private fun timeDialog() {
        isTimeIsUpDialogShowing = true
        val timeDialog = Dialog(this)
        timeDialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        timeDialog.setCancelable(false)
        timeDialog.setContentView(R.layout.dialog_time)
        val layoutParams = WindowManager.LayoutParams()

        layoutParams.copyFrom(timeDialog.window!!.attributes)
        layoutParams.width = WindowManager.LayoutParams.MATCH_PARENT
        layoutParams.height = WindowManager.LayoutParams.WRAP_CONTENT
        layoutParams.gravity = Gravity.CENTER

        timeDialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        timeDialog.window!!.attributes = layoutParams

        timeDialog.findViewById<Button>(R.id.watch).setOnClickListener {
            rewarded { countDown() }
            isTimeIsUpDialogShowing = false
            timeDialog.dismiss()
        }

        timeDialog.show()
    }

    private fun rewarded(onRewComplete: () -> Unit) {
        RewardedManager.showRewarded(this, object : RewardedManager.OnAdClosedListener {
            override fun onRewClosed() {
                onRewComplete()
            }

            override fun onRewFailedToShow() {
                onRewComplete()
            }

            override fun onRewComplete() {}
        })
    }


    private fun takePhotoDialog() {
        isDialogShowing = true

        val takePhotoDialog = Dialog(this)
        takePhotoDialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        takePhotoDialog.setCancelable(true)
        takePhotoDialog.setContentView(R.layout.dialog_take_photo)
        val layoutParams = WindowManager.LayoutParams()

        layoutParams.copyFrom(takePhotoDialog.window!!.attributes)
        layoutParams.width = WindowManager.LayoutParams.MATCH_PARENT
        layoutParams.height = WindowManager.LayoutParams.WRAP_CONTENT
        layoutParams.gravity = Gravity.CENTER

        takePhotoDialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        takePhotoDialog.window!!.attributes = layoutParams

        val itsNotFinished = takePhotoDialog.findViewById<TextView>(R.id.its_not_finished)
        itsNotFinished.paintFlags =
            itsNotFinished.paintFlags or Paint.UNDERLINE_TEXT_FLAG

        takePhotoDialog.findViewById<ImageView>(R.id.close).setOnClickListener {
            isDialogShowing = false
            takePhotoDialog.dismiss()

            if (isTimeIsUp && !isTimeIsUpDialogShowing) {
                timeDialog()
            }
        }

        itsNotFinished.setOnClickListener {
            isDialogShowing = false
            takePhotoDialog.dismiss()

            if (isTimeIsUp && !isTimeIsUpDialogShowing) {
                timeDialog()
            }
        }

        takePhotoDialog.findViewById<Button>(R.id.take_photo).setOnClickListener {
            isDialogShowing = false
            takePhotoDialog.dismiss()
            takePhoto()
        }

        takePhotoDialog.show()
    }

    private fun savePhotoDialog(bitmap: Bitmap) {
        isDialogShowing = true
        val savePhotoDialog = Dialog(this)
        savePhotoDialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        savePhotoDialog.setCancelable(true)
        savePhotoDialog.setContentView(R.layout.dialog_save_photo)
        val layoutParams = WindowManager.LayoutParams()

        layoutParams.copyFrom(savePhotoDialog.window!!.attributes)
        layoutParams.width = WindowManager.LayoutParams.MATCH_PARENT
        layoutParams.height = WindowManager.LayoutParams.WRAP_CONTENT
        layoutParams.gravity = Gravity.CENTER

        savePhotoDialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        savePhotoDialog.window!!.attributes = layoutParams

        val takeAnotherPhoto = savePhotoDialog.findViewById<TextView>(R.id.take_another_photo)
        takeAnotherPhoto.paintFlags =
            takeAnotherPhoto.paintFlags or Paint.UNDERLINE_TEXT_FLAG

        savePhotoDialog.findViewById<ImageView>(R.id.photo).setImageBitmap(bitmap)

        savePhotoDialog.findViewById<ImageView>(R.id.close).setOnClickListener {
            isDialogShowing = false
            savePhotoDialog.dismiss()

            if (isTimeIsUp && !isTimeIsUpDialogShowing) {
                timeDialog()
            }
        }

        takeAnotherPhoto.setOnClickListener {
            isDialogShowing = false
            savePhotoDialog.dismiss()
            takePhoto()
        }

        savePhotoDialog.findViewById<Button>(R.id.save_photo).setOnClickListener {
            isDialogShowing = false
            savePhotoDialog.dismiss()
            saveImage(bitmap)
            Toast.makeText(
                this@SketchActivity, getString(R.string.photo_saved), Toast.LENGTH_SHORT
            ).show()

            inAppReview()
        }

        savePhotoDialog.show()
    }

    private fun takePhoto() {
        getExternalFilesDir(Environment.DIRECTORY_DCIM)?.let { it1 ->
            ImagePicker.with(this)
                .cameraOnly()
                .saveDir(it1)
                .createIntent { intent ->
                    startForTakeAndSaveDrawingPhotoResult.launch(intent)
                }
        }
    }

    private fun saveImage(bitmap: Bitmap) {
        lifecycleScope.launch {
            creationRepository.insertPhotoCreation(bitmap)
        }
    }

    private fun inAppReview() {
        val manager = ReviewManagerFactory.create(this)

        val request = manager.requestReviewFlow()
        request.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val reviewInfo = task.result

                val flow = manager.launchReviewFlow(this, reviewInfo)
                flow.addOnCompleteListener { _ ->
                    super.onBackPressed()
                }

            } else {
                super.onBackPressed()
            }
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
                this, "android.permission.CAMERA", PERMISSIONS_CODE
            )
        }

        if (Constants.bitmap != null) {
            binding.objImage.setImageBitmap(Constants.bitmap)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        if (requestCode == PERMISSIONS_CODE && (grantResults.isEmpty() || grantResults[0] != 0)) {
            Toast.makeText(
                this, getString(R.string.permission_not_granted), Toast.LENGTH_SHORT
            ).show()
            finish()
        }
        if (requestCode != PERMISSIONS_CODE) {
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

        if (isRecording) {
            stopVideo()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacks(timerRunnable)
        countDownTimer.cancel()
        Constants.bitmap = null
        Constants.convertedBitmap = null
    }

    companion object {
        const val FLIP_HORIZONTAL = 2
        private const val FLIP_VERTICAL = 1
        const val PERMISSIONS_CODE = 3002

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