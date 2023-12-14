package com.med.drawing.core.presentation.home

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.StrictMode
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.med.drawing.R
import com.med.drawing.databinding.ActivityHomeBinding
import com.med.drawing.other.AppConstant
import com.med.drawing.sketch.sketch_list.presentation.SketchListActivity
import com.med.drawing.util.ads.InterManager
import com.med.drawing.util.ads.NativeManager
import com.med.drawing.util.rate
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

/**
 * @author Ahmed Guedmioui
 */
@AndroidEntryPoint
class HomeActivity : AppCompatActivity() {

    private val homeViewModel: HomeViewModel by viewModels()
    private lateinit var homeState: HomeState

    private lateinit var binding: ActivityHomeBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        val view: View = binding.root
        setContentView(view)

        lifecycleScope.launch {
            homeViewModel.homeState.collect { homeState = it }
        }

        lifecycleScope.launch {
            homeViewModel.doubleTapToastChannel.collectLatest { showToasts ->
                if (showToasts) {
                    Toast.makeText(
                        this@HomeActivity,
                        getString(R.string.double_click_to_exit),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }

        NativeManager.loadNative(
            findViewById(R.id.native_frame),
            findViewById(R.id.native_temp),
            this, true
        )

        val pushAnimation = AnimationUtils.loadAnimation(this, R.anim.view_push)

        try {
            StrictMode::class.java.getMethod(
                "disableDeathOnFileUriExposure", *arrayOfNulls(0)
            ).invoke(null, *arrayOfNulls(0))
        } catch (e: Exception) {
            e.printStackTrace()
        }

        binding.trace.setOnClickListener {
            it.startAnimation(pushAnimation)
            AppConstant.selected_id = AppConstant.TraceDirect
            drawingListScreen()
        }

        binding.sketch.setOnClickListener {
            it.startAnimation(pushAnimation)
            AppConstant.selected_id = AppConstant.TracePaper
            drawingListScreen()
        }

        binding.creation.setOnClickListener {
            it.startAnimation(pushAnimation)
        }

        binding.rate.setOnClickListener {
            it.startAnimation(pushAnimation)
            rate(this)
        }

        binding.rateBtn.setOnClickListener {
            rate(this)
        }
    }

    private fun drawingListScreen() {
        InterManager.showInterstitial(this, object : InterManager.OnAdClosedListener {
            override fun onAdClosed() {
                startActivity(Intent(this@HomeActivity, SketchListActivity::class.java))
            }
        })
    }

    override fun onBackPressed() {
        homeViewModel.onEvent(HomeUiEvent.BackPressed)
        lifecycleScope.launch {
            homeViewModel.closeChannel.collectLatest { close ->
                if (close) {
                    super.onBackPressed()
                }
            }
        }

    }

}
















