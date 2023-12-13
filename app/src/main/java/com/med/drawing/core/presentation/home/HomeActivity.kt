package com.med.drawing.core.presentation.home

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.StrictMode
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.med.drawing.R
import com.med.drawing.core.domain.usecase.ads.InterManager
import com.med.drawing.core.domain.usecase.ads.NativeManager
import com.med.drawing.databinding.ActivityHomeBinding
import com.med.drawing.other.AppConstant
import com.med.drawing.other.PrivacyPolicyActivity
import com.med.drawing.sketch.sketch_list.presentation.SketchListActivity

/**
 * @author Android Devs Academy (Ahmed Guedmioui)
 */
class HomeActivity : AppCompatActivity() {
    private var pushAnimation: Animation? = null

    private var doubleBackToExitPressedOnce = false

    private lateinit var binding: ActivityHomeBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        val view: View = binding.root
        setContentView(view)

        NativeManager.loadNative(
            findViewById(R.id.native_frame),
            findViewById(R.id.native_temp),
            this
        )

        pushAnimation = AnimationUtils.loadAnimation(this, R.anim.view_push)

        binding.privacyPolicy.setOnClickListener {
            startActivity(
                Intent(
                    this@HomeActivity,
                    PrivacyPolicyActivity::class.java
                )
            )
        }

        try {
            StrictMode::class.java.getMethod(
                "disableDeathOnFileUriExposure", *arrayOfNulls(0)
            ).invoke(null, *arrayOfNulls(0))
        } catch (e: Exception) {
            e.printStackTrace()
        }

        binding.relStartTrace.setOnClickListener {
            it.startAnimation(pushAnimation)
            AppConstant.selected_id = AppConstant.TraceDirect
            drawingListScreen()
        }

        binding.relTracePaper.setOnClickListener {
            it.startAnimation(pushAnimation)
            AppConstant.selected_id = AppConstant.TracePaper
            drawingListScreen()
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
        if (doubleBackToExitPressedOnce) {
            super.onBackPressed()
        }
        doubleBackToExitPressedOnce = true
        Toast.makeText(this, "double tap to exit!", Toast.LENGTH_SHORT).show()
        Handler(Looper.getMainLooper()).postDelayed({
            doubleBackToExitPressedOnce = false
        }, 2000)
    }
}