package com.med.drawing.advanced_editing.presentation

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.Paint
import android.os.Bundle
import android.view.View
import android.widget.SeekBar
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.med.drawing.R
import com.med.drawing.databinding.ActivityAdvancedBinding
import com.med.drawing.util.Constants
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

/**
 * @author Ahmed Guedmioui
 */
@AndroidEntryPoint
class AdvancedEditingActivity : AppCompatActivity() {


    private val advancedEditingViewModel: AdvancedEditingViewModel by viewModels()

    private lateinit var advancedEditingState: AdvancedEditingState

    private lateinit var binding: ActivityAdvancedBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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

        seekBar()
    }

    private fun updatedSelected() {
        when (advancedEditingState.selected) {
            0 -> {
                resetAllCardsColor()
            }

            1 -> {
                resetAllCardsColor()
                binding.edgeCard.setCardBackgroundColor(getColor(R.color.primary_selected))
                binding.editingSeek.progress = advancedEditingState.edge.toInt()
                binding.editingSeek.visibility = View.VISIBLE
            }

            2 -> {
                resetAllCardsColor()
                binding.contrastCard.setCardBackgroundColor(getColor(R.color.primary_selected))
                binding.editingSeek.progress = advancedEditingState.contrast.toInt()
                binding.editingSeek.visibility = View.VISIBLE
            }

            3 -> {
                resetAllCardsColor()
                binding.noiseCard.setCardBackgroundColor(getColor(R.color.primary_selected))
                binding.editingSeek.progress = advancedEditingState.noise.toInt()
                binding.editingSeek.visibility = View.VISIBLE
            }

            4 -> {
                resetAllCardsColor()
                binding.sharpnessCard.setCardBackgroundColor(getColor(R.color.primary_selected))
                binding.editingSeek.progress = advancedEditingState.sharpness.toInt()
                binding.editingSeek.visibility = View.VISIBLE
            }
        }
    }

    private fun resetAllCardsColor() {
        binding.editingSeek.visibility = View.GONE
        binding.editingSeek.progress = 0
        binding.edgeCard.setCardBackgroundColor(getColor(R.color.gray))
        binding.contrastCard.setCardBackgroundColor(getColor(R.color.gray))
        binding.noiseCard.setCardBackgroundColor(getColor(R.color.gray))
        binding.sharpnessCard.setCardBackgroundColor(getColor(R.color.gray))
    }

    private fun seekBar() {
        binding.editingSeek.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                val level = progress / 100f
                sendSelected(level)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
            }
        })
    }

    private fun sendSelected(level: Float) {
        when (advancedEditingState.selected) {
            1 -> {
                advancedEditingViewModel.onEvent(
                    AdvancedEditingUiEvent.SetEdge(level)
                )
            }

            2 -> {
                advancedEditingViewModel.onEvent(
                    AdvancedEditingUiEvent.SetContrast(level)
                )
            }

            3 -> {
                advancedEditingViewModel.onEvent(
                    AdvancedEditingUiEvent.SetNoise(level)
                )
            }

            4 -> {
                advancedEditingViewModel.onEvent(
                    AdvancedEditingUiEvent.SetSharpness(level)
                )
                sharpenImage(advancedEditingState.sharpness)
            }
        }
    }

    private fun sharpenImage(sharpnessLevel: Float) {
        val colorMatrix = ColorMatrix()
        colorMatrix.setSharpness(sharpnessValue)

        val filter = ColorMatrixColorFilter(colorMatrix)
        val paint = Paint()
        paint.colorFilter = filter

        val outputBitmap = Bitmap.createBitmap(
            Constants.bitmap!!.width, Constants.bitmap!!.height, Constants.bitmap!!.config
        )
        Canvas(outputBitmap).drawBitmap(Constants.bitmap!!, 0f, 0f, paint)

    }

}















