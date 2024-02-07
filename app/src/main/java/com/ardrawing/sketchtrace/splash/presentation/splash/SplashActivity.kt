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
import com.ardrawing.sketchtrace.databinding.ActivitySplashBinding
import com.ardrawing.sketchtrace.main.presentaion.get_started.GetStartedActivity
import com.ardrawing.sketchtrace.main.presentaion.home.HomeActivity
import com.ardrawing.sketchtrace.main.presentaion.language.LanguageActivity
import com.ardrawing.sketchtrace.main.presentaion.tips.TipsActivity
import com.ardrawing.sketchtrace.paywall.presentation.PaywallActivity
import com.ardrawing.sketchtrace.splash.data.DataManager
import com.ardrawing.sketchtrace.util.AppAnimation
import com.ardrawing.sketchtrace.util.LanguageChanger
import com.ardrawing.sketchtrace.util.Shared
import com.ardrawing.sketchtrace.util.UrlOpener
import com.ardrawing.sketchtrace.util.ads.AdmobAppOpenManager
import com.ardrawing.sketchtrace.util.ads.InterManager
import com.google.ads.consent.ConsentForm
import com.google.ads.consent.ConsentFormListener
import com.google.ads.consent.ConsentInfoUpdateListener
import com.google.ads.consent.ConsentInformation
import com.google.ads.consent.ConsentStatus
import com.onesignal.OneSignal
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.net.MalformedURLException
import java.net.URL
import javax.inject.Inject

@AndroidEntryPoint
class SplashActivity : AppCompatActivity() {

    private val splashViewModel: SplashViewModel by viewModels()

    private lateinit var splashState: SplashState
    private lateinit var binding: ActivitySplashBinding


    @Inject
    lateinit var prefs: SharedPreferences

    private lateinit var form: ConsentForm

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
                    getConsent(admobAppOpenManager)
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

    private fun getConsent(admobAppOpenManager: AdmobAppOpenManager) {
        val consentInformation = ConsentInformation.getInstance(
            baseContext
        )
        val publisherIds = arrayOf(DataManager.appData.admobPublisherId)
        consentInformation.requestConsentInfoUpdate(
            publisherIds,
            object : ConsentInfoUpdateListener {
                override fun onConsentInfoUpdated(consentStatus: ConsentStatus) {
                    // User's consent status successfully updated.
                    if (ConsentInformation.getInstance(baseContext).isRequestLocationInEeaOrUnknown) {
                        //inside EU
                        when (consentStatus) {
                            ConsentStatus.UNKNOWN -> {
                                displayConsentForm(admobAppOpenManager)
                            }

                            ConsentStatus.PERSONALIZED -> {
                                Shared.setBoolean(this@SplashActivity, "personalized", true)
                                navigate(admobAppOpenManager)
                                Shared.setBoolean(this@SplashActivity, "personalized", false)
                                navigate(admobAppOpenManager)
                            }

                            ConsentStatus.NON_PERSONALIZED -> {
                                Shared.setBoolean(this@SplashActivity, "personalized", false)
                                navigate(admobAppOpenManager)
                            }
                        }
                    } else {
                        //outside EU
                        Shared.setBoolean(this@SplashActivity, "personalized", true)
                        navigate(admobAppOpenManager)
                    }
                }

                override fun onFailedToUpdateConsentInfo(errorDescription: String) {
                    // User's consent status failed to update.
                    Shared.setBoolean(this@SplashActivity, "personalized", true)
                    navigate(admobAppOpenManager)
                }
            })
    }

    private fun displayConsentForm(admobAppOpenManager: AdmobAppOpenManager) {
        try {
            val privacyUrl =
                URL(Shared.getString(application, DataManager.appData.privacyLink))
            form = ConsentForm.Builder(baseContext, privacyUrl)
                .withListener(object : ConsentFormListener() {
                    override fun onConsentFormLoaded() {
                        // Consent form loaded successfully.
                        form.show()
                    }

                    override fun onConsentFormOpened() {
                        // Consent form was displayed.
                    }

                    override fun onConsentFormClosed(
                        consentStatus: ConsentStatus,
                        userPrefersAdFree: Boolean
                    ) {
                        // Consent form was closed.
                        Shared.setBoolean(
                            this@SplashActivity,
                            "personalized",
                            consentStatus != ConsentStatus.NON_PERSONALIZED
                        )
                        navigate(admobAppOpenManager)
                    }

                    override fun onConsentFormError(errorDescription: String) {
                        // Consent form error.
                        Shared.setBoolean(this@SplashActivity, "personalized", true)
                        navigate(admobAppOpenManager)
                    }
                })
                .withPersonalizedAdsOption()
                .withNonPersonalizedAdsOption()
                .build()
            form.load()
        } catch (e: MalformedURLException) {
            e.printStackTrace()
            Shared.setBoolean(this@SplashActivity, "personalized", true)
            navigate(admobAppOpenManager)
        }
    }

    private fun navigate(admobAppOpenManager: AdmobAppOpenManager) {
        tryAgainButtonVisibility(false)

        OneSignal.setLogLevel(OneSignal.LOG_LEVEL.VERBOSE, OneSignal.LOG_LEVEL.NONE)
        OneSignal.initWithContext(this@SplashActivity)
        OneSignal.setAppId(DataManager.appData.onesignalId)
        OneSignal.promptForPushNotifications()

        InterManager.loadInterstitial(this@SplashActivity)

        admobAppOpenManager.showSplashAd {

            if (!prefs.getBoolean("language_chosen", false)) {
                Intent(this@SplashActivity, LanguageActivity::class.java).also {
                    it.putExtra("from_splash", true)
                    startActivity(it)
                }

            } else if (!prefs.getBoolean("tipsShown", false)) {
                Intent(this@SplashActivity, TipsActivity::class.java).also {
                    startActivity(it)
                }

            } else if (!prefs.getBoolean("getStartedShown", false)) {
                Intent(this@SplashActivity, GetStartedActivity::class.java).also {
                    startActivity(it)
                }

            } else {
                checkSubscriptionBeforeGoingHome()
            }

            finish()

        }
    }

    private fun checkSubscriptionBeforeGoingHome() {
        if (DataManager.appData.isSubscribed) {
            splashViewModel.onEvent(SplashUiEvent.AlreadySubscribed)
            goToHome()
        } else {
            Intent(this, PaywallActivity::class.java).also {
                it.putExtra("toHome", true)
                startActivity(it)
            }
        }
    }

    private fun goToHome() {
        Intent(this, HomeActivity::class.java).also {
            startActivity(it)
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





















