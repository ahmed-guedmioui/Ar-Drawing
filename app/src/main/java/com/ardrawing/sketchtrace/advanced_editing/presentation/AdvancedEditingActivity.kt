package com.ardrawing.sketchtrace.advanced_editing.presentation

import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import android.widget.SeekBar
import android.widget.Toast
import com.ardrawing.sketchtrace.util.LanguageChanger
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.ardrawing.sketchtrace.R
import com.ardrawing.sketchtrace.databinding.ActivityAdvancedBinding
import com.ardrawing.sketchtrace.util.Constants
import dagger.hilt.android.AndroidEntryPoint
import jp.co.cyberagent.android.gpuimage.GPUImage
import jp.co.cyberagent.android.gpuimage.filter.GPUImageContrastFilter
import jp.co.cyberagent.android.gpuimage.filter.GPUImageGaussianBlurFilter
import jp.co.cyberagent.android.gpuimage.filter.GPUImageSharpenFilter
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject


/**
 * @author Ahmed Guedmioui
 */
@AndroidEntryPoint
class AdvancedEditingActivity : AppCompatActivity(), SeekBar.OnSeekBarChangeListener {

    @Inject
    lateinit var prefs: SharedPreferences

    private val advancedEditingViewModel: AdvancedEditingViewModel by viewModels()
    private lateinit var advancedEditingState: AdvancedEditingState

    private lateinit var binding: ActivityAdvancedBinding

    private var edgeJob: Job? = null
    private var contrastJob: Job? = null
    private var noiseJob: Job? = null
    private var sharpnessJob: Job? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val languageCode = prefs.getString("language", "en") ?: "en"
        LanguageChanger.changeAppLanguage(languageCode, this)
        binding = ActivityAdvancedBinding.inflate(layoutInflater)
        setContentView(binding.root)

        lifecycleScope.launch {
            advancedEditingViewModel.advancedEditingState.collect {
                advancedEditingState = it
                updatedSelected()
            }
        }

        binding.objImage.setImageBitmap(Constants.bitmap)

        binding.edge.setOnClickListener {
            advancedEditingViewModel.onEvent(AdvancedEditingUiEvent.Select(1))
        }

        binding.contrast.setOnClickListener {
            advancedEditingViewModel.onEvent(AdvancedEditingUiEvent.Select(2))
        }

        binding.noise.setOnClickListener {
            advancedEditingViewModel.onEvent(AdvancedEditingUiEvent.Select(3))
        }

        binding.sharpness.setOnClickListener {
            advancedEditingViewModel.onEvent(AdvancedEditingUiEvent.Select(4))
        }

        binding.edgeSeek.setOnSeekBarChangeListener(this)
        binding.contrastSeek.setOnSeekBarChangeListener(this)
        binding.noiseSeek.setOnSeekBarChangeListener(this)
        binding.sharpnessSeek.setOnSeekBarChangeListener(this)

        binding.apply.setOnClickListener {
            Constants.bitmap = Constants.convertedBitmap
            Constants.convertedBitmap = null

            finish()
            Toast.makeText(
                this, getString(R.string.applied), Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun showApplyAlertDialog() {

        val alertDialog = AlertDialog.Builder(this)
            .setTitle(
                getString(R.string.do_you_want_to_apply_the_editing)
            )
            .setPositiveButton(getString(R.string.yes)) { dialog, _ ->
                Constants.bitmap = Constants.convertedBitmap
                super.onBackPressed()
                Toast.makeText(
                    this, getString(R.string.applied), Toast.LENGTH_SHORT
                ).show()
                dialog.dismiss()
            }
            .setNegativeButton(getString(R.string.no)) { dialog, _ ->
                finish()
                dialog.dismiss()
            }.create()

        alertDialog.show()
    }

    override fun onBackPressed() {
        showApplyAlertDialog()
    }

    private fun updatedSelected() {
        when (advancedEditingState.selected) {
            0 -> {
                resetAllCardsColor()
            }

            1 -> {
                resetAllCardsColor()
                binding.edgeCard.setCardBackgroundColor(getColor(R.color.primary_selected))
                binding.edgeSeek.visibility = View.VISIBLE
            }

            2 -> {
                resetAllCardsColor()
                binding.contrastCard.setCardBackgroundColor(getColor(R.color.primary_selected))
                binding.contrastSeek.visibility = View.VISIBLE
            }

            3 -> {
                resetAllCardsColor()
                binding.noiseCard.setCardBackgroundColor(getColor(R.color.primary_selected))
                binding.noiseSeek.visibility = View.VISIBLE
            }

            4 -> {
                resetAllCardsColor()
                binding.sharpnessCard.setCardBackgroundColor(getColor(R.color.primary_selected))
                binding.sharpnessSeek.visibility = View.VISIBLE
            }
        }
    }

    private fun resetAllCardsColor() {
        binding.edgeSeek.visibility = View.GONE
        binding.contrastSeek.visibility = View.GONE
        binding.noiseSeek.visibility = View.GONE
        binding.sharpnessSeek.visibility = View.GONE

        binding.edgeCard.setCardBackgroundColor(getColor(R.color.gray))
        binding.contrastCard.setCardBackgroundColor(getColor(R.color.gray))
        binding.noiseCard.setCardBackgroundColor(getColor(R.color.gray))
        binding.sharpnessCard.setCardBackgroundColor(getColor(R.color.gray))
    }

    override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
        Constants.convertedBitmap = Constants.bitmap
        setPreviousEdited()
        sendSelected(progress)
    }

    override fun onStartTrackingTouch(seekBar: SeekBar?) {
    }

    override fun onStopTrackingTouch(seekBar: SeekBar?) {
    }

    private fun setPreviousEdited() {
        when (advancedEditingState.selected) {
            1 -> {
                // apply the other filters except edge

                if (advancedEditingState.isContrast) {
                    contrast()
                }
                if (advancedEditingState.isNoise) {
                    noise()
                }
                if (advancedEditingState.isSharpened) {
                    sharpen()
                }
            }

            2 -> {
                // apply the other filters except contrast

                if (advancedEditingState.isEdged) {
                    edge()
                }
                if (advancedEditingState.isNoise) {
                    noise()
                }
                if (advancedEditingState.isSharpened) {
                    sharpen()
                }

            }

            3 -> {
                // apply the other filters except noise

                if (advancedEditingState.isEdged) {
                    edge()
                }
                if (advancedEditingState.isContrast) {
                    contrast()
                }
                if (advancedEditingState.isSharpened) {
                    sharpen()
                }
            }

            4 -> {
                // apply the other filters except sharpness

                if (advancedEditingState.isEdged) {
                    edge()
                }
                if (advancedEditingState.isContrast) {
                    contrast()
                }
                if (advancedEditingState.isNoise) {
                    noise()
                }
            }
        }
    }

    private fun sendSelected(level: Int) {
        when (advancedEditingState.selected) {
            1 -> {
                advancedEditingViewModel.onEvent(
                    AdvancedEditingUiEvent.SetEdge(level)
                )
                edge()
            }

            2 -> {
                advancedEditingViewModel.onEvent(
                    AdvancedEditingUiEvent.SetContrast(level)
                )
                contrast()
            }

            3 -> {
                advancedEditingViewModel.onEvent(
                    AdvancedEditingUiEvent.SetNoise(level)
                )
                noise()
            }

            4 -> {
                advancedEditingViewModel.onEvent(
                    AdvancedEditingUiEvent.SetSharpness(level)
                )
                sharpen()
            }
        }
    }

    private fun edge() {
        edgeJob?.cancel()
        edgeJob = lifecycleScope.launch {
            delay(300L)

            try {
                val gPUImage = GPUImage(this@AdvancedEditingActivity)
                gPUImage.setImage(Constants.convertedBitmap)

                gPUImage.setFilter(
                    GPUImageSharpenFilter(
                        range(advancedEditingState.edge, 0.0f, 4.0f)
                    )
                )

                if (gPUImage.bitmapWithFilterApplied != null) {
                    Constants.convertedBitmap = gPUImage.bitmapWithFilterApplied
                    binding.objImage.setImageBitmap(Constants.convertedBitmap)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun contrast() {
        contrastJob?.cancel()
        contrastJob = lifecycleScope.launch {
            delay(300L)

            try {
                val gPUImage = GPUImage(this@AdvancedEditingActivity)
                gPUImage.setImage(Constants.convertedBitmap)

                gPUImage.setFilter(
                    GPUImageContrastFilter(
                        range(advancedEditingState.contrast, 1.0f, 4.0f)
                    )
                )

                if (gPUImage.bitmapWithFilterApplied != null) {
                    Constants.convertedBitmap = gPUImage.bitmapWithFilterApplied
                    binding.objImage.setImageBitmap(Constants.convertedBitmap)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun noise() {
        noiseJob?.cancel()
        noiseJob = lifecycleScope.launch {
            delay(300L)

            try {
                val gPUImage = GPUImage(this@AdvancedEditingActivity)
                gPUImage.setImage(Constants.convertedBitmap)

                val filter = GPUImageGaussianBlurFilter()
                filter.setBlurSize(
                    range(advancedEditingState.noise, 0.0f, 1.0f)
                )

                gPUImage.setFilter(filter)

                if (gPUImage.bitmapWithFilterApplied != null) {
                    Constants.convertedBitmap = gPUImage.bitmapWithFilterApplied
                    binding.objImage.setImageBitmap(Constants.convertedBitmap)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }


    private fun sharpen() {
        sharpnessJob?.cancel()
        sharpnessJob = lifecycleScope.launch {
            delay(300L)

            try {
                val gPUImage = GPUImage(this@AdvancedEditingActivity)
                gPUImage.setImage(Constants.convertedBitmap)

                gPUImage.setFilter(
                    GPUImageSharpenFilter(
                        range(advancedEditingState.sharpness, 0.0f, 2.0f)
                    )
                )

                if (gPUImage.bitmapWithFilterApplied != null) {
                    Constants.convertedBitmap = gPUImage.bitmapWithFilterApplied
                    binding.objImage.setImageBitmap(Constants.convertedBitmap)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun range(percentage: Int, start: Float, end: Float): Float {
        val finePercentage = percentage / 2
        return (end - start) * finePercentage.toFloat() / 100.0f + start
    }

}















