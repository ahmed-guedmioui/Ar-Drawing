package com.ardrawing.sketchtrace.main.presentaion.get_started

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
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.ardrawing.sketchtrace.util.LanguageChanger
import androidx.lifecycle.lifecycleScope
import com.ardrawing.sketchtrace.BuildConfig
import com.ardrawing.sketchtrace.R
import com.ardrawing.sketchtrace.util.ads.NativeManager
import com.ardrawing.sketchtrace.main.presentaion.home.HomeActivity
import com.ardrawing.sketchtrace.databinding.ActivityGetStartedBinding
import com.ardrawing.sketchtrace.splash.data.DataManager
import com.ardrawing.sketchtrace.util.ads.InterManager
import com.revenuecat.purchases.ui.revenuecatui.ExperimentalPreviewRevenueCatUIPurchasesAPI
import com.revenuecat.purchases.ui.revenuecatui.activity.PaywallActivityLauncher
import com.revenuecat.purchases.ui.revenuecatui.activity.PaywallResult
import com.revenuecat.purchases.ui.revenuecatui.activity.PaywallResultHandler
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import javax.inject.Inject

/**
 * @author Ahmed Guedmioui
 */
@OptIn(ExperimentalPreviewRevenueCatUIPurchasesAPI::class)
@AndroidEntryPoint
class GetStartedActivity : AppCompatActivity(), PaywallResultHandler {

    private val getStartedViewModel: GetStartedViewModel by viewModels()

    private lateinit var getStartedState: GetStartedState
    private lateinit var binding: ActivityGetStartedBinding

    private lateinit var paywallActivityLauncher: PaywallActivityLauncher

    @Inject
    lateinit var prefs: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val languageCode = prefs.getString("language", "en") ?: "en"
        LanguageChanger.changeAppLanguage(languageCode, this)
        binding = ActivityGetStartedBinding.inflate(layoutInflater)
        val view: View = binding.root
        setContentView(view)

        lifecycleScope.launch {
            getStartedViewModel.getsStartedState.collect {
                getStartedState = it
                privacyDialog()
            }
        }

        binding.privacyPolicy.setOnClickListener {
            getStartedViewModel.onEvent(GetStartedUiEvent.ShowHidePrivacyDialog)
        }
        NativeManager.loadNative(
            findViewById(R.id.native_frame), findViewById(R.id.native_temp), this, true
        )


        paywallActivityLauncher = PaywallActivityLauncher(this, this)

        binding.getStarted.setOnClickListener {
            InterManager.showInterstitial(this, object : InterManager.OnAdClosedListener {
                override fun onAdClosed() {
                    prefs.edit().putBoolean("getStartedShown", true).apply()
                    move()
                }
            })
        }

    }

    private fun move() {
        if (DataManager.appData.isSubscribed) {
            goToHome()
        } else {
            paywallActivityLauncher.launchIfNeeded(
                requiredEntitlementIdentifier = BuildConfig.ENTITLEMENT
            )
        }
    }

    override fun onActivityResult(result: PaywallResult) {
        when (result) {
            PaywallResult.Cancelled -> {
                Log.d("REVENUE_CUT", "Cancelled")
                getStartedViewModel.onEvent(
                    GetStartedUiEvent.Subscribe(isSubscribed = false)
                )
                goToHome()
            }

            is PaywallResult.Error -> {
                Log.d("REVENUE_CUT", "Error")
                getStartedViewModel.onEvent(
                    GetStartedUiEvent.Subscribe(isSubscribed = false)
                )
                goToHome()
            }

            is PaywallResult.Purchased -> {

                val date =
                    result.customerInfo.getExpirationDateForEntitlement(BuildConfig.ENTITLEMENT)

                getStartedViewModel.onEvent(
                    GetStartedUiEvent.Subscribe(isSubscribed = true, date = date)
                )

                Log.d("REVENUE_CUT", "Purchased")
                goToHome()
            }

            is PaywallResult.Restored -> {
                val date =
                    result.customerInfo.getExpirationDateForEntitlement(BuildConfig.ENTITLEMENT)

                getStartedViewModel.onEvent(
                    GetStartedUiEvent.Subscribe(isSubscribed = true, date = date)
                )

                Log.d("REVENUE_CUT", "Restored")
                goToHome()
            }
        }
    }

    private fun goToHome() {
        Intent(this, HomeActivity::class.java).also {
            startActivity(it)
            finish()
        }
    }

    private fun privacyDialog() {
        val privacyDialog = Dialog(this)
        privacyDialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        privacyDialog.setCancelable(true)
        privacyDialog.setContentView(R.layout.dialog_privacy)
        val layoutParams = WindowManager.LayoutParams()

        layoutParams.copyFrom(privacyDialog.window!!.attributes)
        layoutParams.width = WindowManager.LayoutParams.MATCH_PARENT
        layoutParams.height = WindowManager.LayoutParams.WRAP_CONTENT
        layoutParams.gravity = Gravity.CENTER

        privacyDialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        privacyDialog.window!!.attributes = layoutParams

        privacyDialog.findViewById<TextView>(R.id.privacy_policy).text =
            getString(R.string.app_privacy_policy)

        privacyDialog.setOnDismissListener {
            getStartedViewModel.onEvent(GetStartedUiEvent.ShowHidePrivacyDialog)
        }

        privacyDialog.findViewById<Button>(R.id.okay).setOnClickListener {
            privacyDialog.dismiss()
        }

        if (getStartedState.showPrivacyDialog) {
            privacyDialog.show()
        } else {
            privacyDialog.dismiss()
        }
    }
}















