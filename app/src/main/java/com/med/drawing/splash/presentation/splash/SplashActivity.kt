package com.med.drawing.splash.presentation.splash

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.med.drawing.R
import com.med.drawing.util.ads.AdmobAppOpenManager
import com.med.drawing.util.ads.InterManager
import com.med.drawing.main.presentaion.get_started.GetStartedActivity
import com.med.drawing.main.presentaion.home.HomeActivity
import com.med.drawing.main.presentaion.tips.TipsActivity
import com.med.drawing.databinding.ActivitySplashBinding
import com.med.drawing.util.AppAnimation
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class SplashActivity : AppCompatActivity() {

    private val splashViewModel: SplashViewModel by viewModels()

    private lateinit var splashState: SplashState
    private lateinit var binding: ActivitySplashBinding

    @Inject
    lateinit var prefs: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashBinding.inflate(layoutInflater)
        val view: View = binding.root
        setContentView(view)

        AppAnimation().startRepeatingAnimation(binding.animationImage)

        lifecycleScope.launch {
            splashViewModel.splashState.collect { splashState = it }
        }

        binding.tryAgain.setOnClickListener {
            binding.tryAgain.visibility = View.GONE
            binding.progressBar.visibility = View.VISIBLE
            splashViewModel.onEvent(SplashUiEvent.TryAgain)
        }

        val admobAppOpenManager = AdmobAppOpenManager(
            this@SplashActivity.application, prefs
        )

        lifecycleScope.launch {
            splashViewModel.areBothImagesAndDataLoadedChannel.collect { result ->
                if (result) {
                    InterManager.loadInterstitial(this@SplashActivity)

                    admobAppOpenManager.showSplashAd {
                        startActivity(
                            if (!prefs.getBoolean("tipsShown", false)) {
                                Intent(this@SplashActivity, TipsActivity::class.java)
                            } else {

                                if (!prefs.getBoolean("getStartedShown", false)) {
                                    Intent(this@SplashActivity, GetStartedActivity::class.java)
                                } else {
                                    Intent(this@SplashActivity, HomeActivity::class.java)
                                }

                            }
                        )
                        finish()
                    }
                } else {

                    Toast.makeText(
                        this@SplashActivity,
                        getString(R.string.error_connect_to_a_network_and_try_again),
                        Toast.LENGTH_LONG
                    ).show()
                    binding.tryAgain.visibility = View.VISIBLE
                    binding.progressBar.visibility = View.GONE
                }
            }
        }


    }


}





















