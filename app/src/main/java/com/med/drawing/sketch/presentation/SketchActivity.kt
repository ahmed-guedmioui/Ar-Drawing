package com.med.drawing.sketch.presentation

import android.content.ContentResolver
import android.content.Intent
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.Matrix
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.view.View
import android.view.Window
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.SeekBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.github.dhaval2404.imagepicker.ImagePicker
import com.med.drawing.R
import com.med.drawing.databinding.ActivitySketchBinding
import com.med.drawing.util.other.MultiTouch
import com.thebluealliance.spectrum.SpectrumDialog

class SketchActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySketchBinding
    private val PERMISSION_CODE_CAMERA = 3002
    private var bmOriginal: Bitmap? = null
    private var brightness: Int = 0
    private lateinit var cResolver: ContentResolver
    private lateinit var window: Window
    private lateinit var pushanim: Animation
    private var isLock: Boolean = false
    private var isEditSketch: Boolean = false
    private var isblack: Boolean = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySketchBinding.inflate(layoutInflater)
        setContentView(binding.root)

        pushanim = AnimationUtils.loadAnimation(this, R.anim.view_push)
        cResolver = contentResolver

        pushanim = AnimationUtils.loadAnimation(this, R.anim.view_push)

        binding.apply {
            relFlip.setOnClickListener {
                it.startAnimation(pushanim)
                bmOriginal = flip(bmOriginal, 2)
                bmOriginal?.let {
                    objImage.setImageBitmap(it)
                }
            }

            relCamera.setOnClickListener {
                it.startAnimation(pushanim)
                getExternalFilesDir(Environment.DIRECTORY_DCIM)?.let { it1 ->
                    ImagePicker.with(this@SketchActivity).cameraOnly()
                        .saveDir(it1).start(103)
                }
            }

            relGallery.setOnClickListener {
                it.startAnimation(pushanim)
                ImagePicker.with(this@SketchActivity).galleryOnly().start(102)
            }

            relLock.setOnClickListener {
                it.startAnimation(pushanim)
                if (!isLock) {
                    objImage.isEnabled = false
                    isLock = true
                    icLock.setImageResource(R.drawable.unlock)
                } else {
                    objImage.isEnabled = true
                    isLock = false
                    icLock.setImageResource(R.drawable.lock)
                }
            }

            alphaSeek.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(
                    seekBar: SeekBar?,
                    progress: Int,
                    fromUser: Boolean
                ) {
                    objImage.alpha = (alphaSeek.max - progress) / 10.0f
                }

                override fun onStartTrackingTouch(seekBar: SeekBar?) {}

                override fun onStopTrackingTouch(seekBar: SeekBar?) {}
            })

            brightnessSeek.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(
                    seekBar: SeekBar?,
                    progress: Int,
                    fromUser: Boolean
                ) {
                    if (progress <= 20) {
                        brightness = 20
                    } else {
                        brightness = progress
                    }
                    val attributes = window.attributes
                    attributes.screenBrightness = brightness / 255.0f
                    window.attributes = attributes
                }

                override fun onStartTrackingTouch(seekBar: SeekBar?) {}

                override fun onStopTrackingTouch(seekBar: SeekBar?) {}
            })
        }

        val i = Resources.getSystem().displayMetrics.widthPixels
        val imagePath = intent.extras?.getString("imagePath")

        isblack = true
        window = getWindow()
        window.attributes = window.attributes
        cResolver = contentResolver
        binding.brightnessSeek.max = 255
        binding.brightnessSeek.progress = 255
        binding.brightnessSeek.keyProgressIncrement = 1
        try {
            brightness = Settings.System.getInt(cResolver, getString(R.string.screen_brightness))
        } catch (e: Settings.SettingNotFoundException) {
            e.printStackTrace()
        }
        binding.brightnessSeek.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onStartTrackingTouch(seekBar: SeekBar) {}
            override fun onStopTrackingTouch(seekBar: SeekBar) {}
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                brightness = if (progress <= 20) {
                    20
                } else {
                    progress
                }
                val attributes = window.attributes
                attributes.screenBrightness = brightness / 255.0f
                window.attributes = attributes
            }
        })
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
                        val imageView = binding.objImage
                        val d = i.toDouble()
                        imageView.setOnTouchListener(
                            MultiTouch(
                                imageView, 1.0f, 1.0f, (d / 3.5).toInt().toFloat(), 600.0f
                            )
                        )
                        val bitmap = bmOriginal
                        if (bitmap != null) {
                            binding.objImage.setImageBitmap(bitmap)
                            isEditSketch = false
                        } else {
                            Toast.makeText(
                                this@SketchActivity,
                                getString(R.string.some_issue_with_this_image_try_another_one),
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }

                    override fun onLoadCleared(placeholder: Drawable?) {}
                })
        }
        binding.animationView.visibility = View.VISIBLE
        Handler(Looper.getMainLooper()).postDelayed({
            binding.animationView.visibility = View.GONE
        }, 7000L)
        binding.relCamera.setOnClickListener {
            it.startAnimation(pushanim)
            getExternalFilesDir(Environment.DIRECTORY_DCIM)?.let { it1 ->
                ImagePicker.with(this).cameraOnly().saveDir(
                    it1
                ).start(103)
            }
        }
        binding.relGallery.setOnClickListener {
            it.startAnimation(pushanim)
            ImagePicker.with(this).galleryOnly().start(102)
        }
        binding.relFlip.setOnClickListener {
            it.startAnimation(pushanim)
            bmOriginal = flip(bmOriginal, 2)
            if (bmOriginal != null) {
                binding.objImage.setImageBitmap(bmOriginal)
            }
        }
        binding.relEditRound.setOnClickListener {
            it.startAnimation(pushanim)
            colorDialog()
        }
        binding.relLock.setOnClickListener {
            it.startAnimation(pushanim)
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
            override fun onProgressChanged(seekBar: SeekBar, i2: Int, z: Boolean) {
                binding.objImage.alpha = (binding.alphaSeek.max - i2) / 10.0f
            }
        })
    }

    private fun colorDialog() {
        SpectrumDialog.Builder(this)
            .setColors(R.array.demo_colors)
            .setSelectedColorRes(R.color.black)
            .setDismissOnColorSelected(true)
            .setOutlineWidth(2)
            .setFixedColumnCount(4)
            .setOnColorSelectedListener { _, i ->
                binding.mainLayout.setBackgroundColor(i)
            }
            .build()
            .show(supportFragmentManager, getString(R.string.color))
    }

    private fun flip(bitmap: Bitmap?, i: Int): Bitmap? {
        val matrix = Matrix()
        return when (i) {
            1 -> {
                matrix.preScale(1.0f, -1.0f)
                Bitmap.createBitmap(bitmap!!, 0, 0, bitmap.width, bitmap.height, matrix, true)
            }

            2 -> {
                matrix.preScale(-1.0f, 1.0f)
                Bitmap.createBitmap(bitmap!!, 0, 0, bitmap.width, bitmap.height, matrix, true)
            }

            else -> null
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK) {
            val dataUri = data?.data

            if (dataUri != null) {

                val i = Resources.getSystem().displayMetrics.widthPixels
                Glide.with(this)
                    .asBitmap()
                    .load(dataUri.toString())
                    .into(object : CustomTarget<Bitmap>() {
                        override fun onResourceReady(
                            resource: Bitmap,
                            transition: Transition<in Bitmap>?
                        ) {
                            bmOriginal = resource
                            val imageView = binding.objImage
                            val d = i.toDouble()
                            imageView.setOnTouchListener(
                                MultiTouch(
                                    imageView, 1.0f, 1.0f, (d / 3.5).toInt().toFloat(), 600.0f
                                )
                            )
                            val bitmap = bmOriginal
                            if (bitmap != null) {
                                binding.objImage.setImageBitmap(bitmap)
                                isEditSketch = false
                            } else {
                                Toast.makeText(
                                    this@SketchActivity,
                                    getString(R.string.some_issue_with_this_image_try_another_one),
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }

                        override fun onLoadCleared(placeholder: Drawable?) {}
                    })
            }
        }
    }


    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        if (requestCode == PERMISSION_CODE_CAMERA && (grantResults.isEmpty() || grantResults[0] != 0)) {
            Toast.makeText(this, "Permission not granted", Toast.LENGTH_SHORT).show()
            finish()
        }
        if (requestCode != PERMISSION_CODE_CAMERA) {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        }
    }

}
