package com.med.drawing.splash.presentation.splash

import android.app.Dialog
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.med.drawing.BuildConfig
import com.med.drawing.R
import com.med.drawing.util.ads.AdmobAppOpenManager
import com.med.drawing.util.ads.InterManager
import com.med.drawing.main.presentaion.get_started.GetStartedActivity
import com.med.drawing.main.presentaion.home.HomeActivity
import com.med.drawing.main.presentaion.tips.TipsActivity
import com.med.drawing.databinding.ActivitySplashBinding
import com.med.drawing.splash.data.DataManager
import com.med.drawing.util.AppAnimation
import com.med.drawing.util.UrlOpener
import com.onesignal.OneSignal
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
            splashViewModel.splashState.collect {
                splashState = it
            }
        }

        val admobAppOpenManager = AdmobAppOpenManager(
            this@SplashActivity.application, prefs
        )

        lifecycleScope.launch {
            splashViewModel.continueAppChannel.collect { continueApp ->
                if (continueApp) {

                    tryAgainButtonVisibility(false)

                    OneSignal.setLogLevel(OneSignal.LOG_LEVEL.VERBOSE, OneSignal.LOG_LEVEL.NONE)
                    OneSignal.initWithContext(this@SplashActivity)
                    OneSignal.setAppId(DataManager.appData.onesignalId)
                    OneSignal.promptForPushNotifications()

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
                }
            }
        }

        lifecycleScope.launch {
            splashViewModel.showUpdateDialogChannel.collect { show ->
                if (show) {
                    updateDialog()
                }
            }
        }

        lifecycleScope.launch {
            splashViewModel.showErrorToastChannel.collect { show ->
                if (show) {
                    Toast.makeText(
                        this@SplashActivity,
                        getString(R.string.error_connect_to_a_network_and_try_again),
                        Toast.LENGTH_SHORT
                    ).show()

                    tryAgainButtonVisibility(true)
                }
            }
        }

        binding.tryAgain.setOnClickListener {
            tryAgainButtonVisibility(false)
            splashViewModel.onEvent(SplashUiEvent.TryAgain)
        }

    }


    private fun updateDialog() {

        val isSuspended = splashState.updateDialogState == 2

        val updateDialog = Dialog(this)
        updateDialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        updateDialog.setCancelable(!isSuspended)
        updateDialog.setContentView(R.layout.dialog_app_update)
        val layoutParams = WindowManager.LayoutParams()

        layoutParams.copyFrom(updateDialog.window!!.attributes)
        layoutParams.width = WindowManager.LayoutParams.MATCH_PARENT
        layoutParams.height = WindowManager.LayoutParams.WRAP_CONTENT
        layoutParams.gravity = Gravity.CENTER

        updateDialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        updateDialog.window!!.attributes = layoutParams

        updateDialog.findViewById<Button>(R.id.update).setOnClickListener {
            if (isSuspended) {
                UrlOpener.open(this, DataManager.appData.suspendedURL)
            } else {
                UrlOpener.open(this, BuildConfig.APPLICATION_ID)
            }
        }

        if (!isSuspended) {
            updateDialog.findViewById<ImageView>(R.id.close).visibility = View.VISIBLE
        } else {
            updateDialog.findViewById<TextView>(R.id.title).text =
                DataManager.appData.suspendedTitle
            updateDialog.findViewById<TextView>(R.id.msg).text =
                DataManager.appData.suspendedMessage
        }

        updateDialog.setOnDismissListener {
            binding.progressBar.visibility = View.VISIBLE
            splashViewModel.onEvent(SplashUiEvent.ContinueApp)
        }

        updateDialog.findViewById<ImageView>(R.id.close).setOnClickListener {
            updateDialog.dismiss()
        }

        updateDialog.show()
        binding.progressBar.visibility = View.GONE

    }

    private fun tryAgainButtonVisibility(show: Boolean) {
        if (show) {
            binding.tryAgain.visibility = View.VISIBLE
            binding.progressBar.visibility = View.GONE
        } else {
            binding.tryAgain.visibility = View.GONE
            binding.progressBar.visibility = View.VISIBLE
        }


    }
}





















