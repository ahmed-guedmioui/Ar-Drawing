package com.ardrawing.sketchtrace.splash.presentation.splash

import android.app.Dialog
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
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
import com.ardrawing.sketchtrace.BuildConfig
import com.ardrawing.sketchtrace.R
import com.ardrawing.sketchtrace.util.ads.AdmobAppOpenManager
import com.ardrawing.sketchtrace.util.ads.InterManager
import com.ardrawing.sketchtrace.main.presentaion.get_started.GetStartedActivity
import com.ardrawing.sketchtrace.main.presentaion.home.HomeActivity
import com.ardrawing.sketchtrace.main.presentaion.tips.TipsActivity
import com.ardrawing.sketchtrace.databinding.ActivitySplashBinding
import com.ardrawing.sketchtrace.main.presentaion.language.LanguageActivity
import com.ardrawing.sketchtrace.splash.data.DataManager
import com.ardrawing.sketchtrace.util.AppAnimation
import com.ardrawing.sketchtrace.util.LanguageChanger
import com.ardrawing.sketchtrace.util.UrlOpener
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

        val languageCode = prefs.getString("language", "en") ?: "en"
        LanguageChanger.changeAppLanguage(languageCode, this)

        binding = ActivitySplashBinding.inflate(layoutInflater)
        val view: View = binding.root
        setContentView(view)

        AppAnimation().startRepeatingAnimation(binding.animationImage)

        lifecycleScope.launch {
            splashViewModel.splashState.collect {
                splashState = it
            }
        }

        lifecycleScope.launch {
            splashViewModel.updateDialogState.collect { state ->
                if (state > 0) {
                    updateDialog(state)
                }
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

                        val intent =
                            if (!prefs.getBoolean("language_chosen", false)) {
                                Intent(this@SplashActivity, LanguageActivity::class.java)

                            } else if (!prefs.getBoolean("tipsShown", false)) {
                                Intent(this@SplashActivity, TipsActivity::class.java)

                            } else if (!prefs.getBoolean("getStartedShown", false)) {
                                Intent(this@SplashActivity, GetStartedActivity::class.java)

                            } else {
                                Intent(this@SplashActivity, HomeActivity::class.java)
                            }

                        intent.putExtra("from_splash", true)

                        startActivity(intent)
                        finish()
                    }
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

    private fun updateDialog(state: Int) {

        val isSuspended = state == 2

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





















