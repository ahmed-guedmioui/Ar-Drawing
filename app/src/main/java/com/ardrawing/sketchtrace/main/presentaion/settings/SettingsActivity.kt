package com.ardrawing.sketchtrace.main.presentaion.settings

import android.annotation.SuppressLint
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
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.ardrawing.sketchtrace.BuildConfig
import com.ardrawing.sketchtrace.R
import com.ardrawing.sketchtrace.databinding.ActivitySettingsBinding
import com.ardrawing.sketchtrace.main.presentaion.follow.FollowActivity
import com.ardrawing.sketchtrace.main.presentaion.language.LanguageActivity
import com.ardrawing.sketchtrace.main.presentaion.settings.adapter.RecommendedAppsAdapter
import com.ardrawing.sketchtrace.main.presentaion.tips.TipsActivity
import com.ardrawing.sketchtrace.splash.data.DataManager
import com.ardrawing.sketchtrace.splash.presentation.splash.SplashUiEvent
import com.ardrawing.sketchtrace.util.Constants
import com.ardrawing.sketchtrace.util.LanguageChanger
import com.ardrawing.sketchtrace.util.openDeveloper
import com.ardrawing.sketchtrace.util.rateApp
import com.ardrawing.sketchtrace.util.shareApp
import com.revenuecat.purchases.CacheFetchPolicy
import com.revenuecat.purchases.CustomerInfo
import com.revenuecat.purchases.Purchases
import com.revenuecat.purchases.PurchasesError
import com.revenuecat.purchases.interfaces.ReceiveCustomerInfoCallback
import com.revenuecat.purchases.ui.revenuecatui.ExperimentalPreviewRevenueCatUIPurchasesAPI
import com.revenuecat.purchases.ui.revenuecatui.activity.PaywallActivityLauncher
import com.revenuecat.purchases.ui.revenuecatui.activity.PaywallResult
import com.revenuecat.purchases.ui.revenuecatui.activity.PaywallResultHandler
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import javax.inject.Inject

@OptIn(ExperimentalPreviewRevenueCatUIPurchasesAPI::class)
@AndroidEntryPoint
class SettingsActivity : AppCompatActivity(), PaywallResultHandler {

    private val splashViewModel: SettingsViewModel by viewModels()

    private lateinit var settingsState: SettingsState
    private lateinit var binding: ActivitySettingsBinding

    private lateinit var paywallActivityLauncher: PaywallActivityLauncher

    @Inject
    lateinit var prefs: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val languageCode = prefs.getString("language", "en") ?: "en"
        LanguageChanger.changeAppLanguage(languageCode, this)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        val view: View = binding.root
        setContentView(view)

        lifecycleScope.launch {
            splashViewModel.settingsState.collect {
                settingsState = it
                privacyDialog()
            }
        }

        if (DataManager.appData.showRecommendedApps) {
            binding.recommendedAppsRecyclerView.layoutManager =
                LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)

            binding.recommendedAppsRecyclerView.adapter = RecommendedAppsAdapter(this)
        } else {
            binding.recommendedAppsParent.visibility = View.GONE
        }

        binding.back.setOnClickListener {
            onBackPressed()
        }

        binding.rateUs.setOnClickListener {
            rateApp(this)
        }

        binding.moreApps.setOnClickListener {
            openDeveloper(this)
        }

        binding.share.setOnClickListener {
            shareApp(this)
        }

        binding.followUs.setOnClickListener {
            startActivity(Intent(this, FollowActivity::class.java))
        }

        binding.tips.setOnClickListener {
            val intent = Intent(this, TipsActivity::class.java)
            intent.putExtra("from_splash", false)
            startActivity(intent)
        }

        binding.privacy.setOnClickListener {
            splashViewModel.onEvent(SettingsUiEvent.ShowHidePrivacyDialog)
        }

        binding.language.setOnClickListener {
            val intent = Intent(this, LanguageActivity::class.java)
            intent.putExtra("from_splash", false)
            startActivity(intent)
        }

        paywallActivityLauncher = PaywallActivityLauncher(this, this)
        binding.subscribe.setOnClickListener {
            if (DataManager.appData.isSubscribed) {
                Toast.makeText(
                    this, getString(R.string.you_are_already_subscribed), Toast.LENGTH_SHORT
                ).show()
            } else {
                paywallActivityLauncher.launchIfNeeded(
                    requiredEntitlementIdentifier = BuildConfig.ENTITLEMENT
                )
            }
        }

        binding.subscribeInfo.text = DataManager.appData.subscriptionInfo
    }

    override fun onActivityResult(result: PaywallResult) {
        when (result) {
            PaywallResult.Cancelled -> {
                Log.d("REVENUE_CUT", "Cancelled")
                splashViewModel.onEvent(
                    SettingsUiEvent.Subscribe(isSubscribed = false)
                )
            }

            is PaywallResult.Error -> {
                Log.d("REVENUE_CUT", "Error")
                splashViewModel.onEvent(
                    SettingsUiEvent.Subscribe(isSubscribed = false)
                )
            }

            is PaywallResult.Purchased -> {

                val date =
                    result.customerInfo.getExpirationDateForEntitlement(BuildConfig.ENTITLEMENT)

                splashViewModel.onEvent(
                    SettingsUiEvent.Subscribe(isSubscribed = true, date = date)
                )

                Log.d("REVENUE_CUT", "Purchased")
            }

            is PaywallResult.Restored -> {
                val date =
                    result.customerInfo.getExpirationDateForEntitlement(BuildConfig.ENTITLEMENT)

                splashViewModel.onEvent(
                    SettingsUiEvent.Subscribe(isSubscribed = true, date = date)
                )

                Log.d("REVENUE_CUT", "Restored")
            }
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
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

        val webView = privacyDialog.findViewById<WebView>(R.id.web_view)
        webView.settings.javaScriptEnabled = true

        webView.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(
                view: WebView?, request: WebResourceRequest?
            ): Boolean {
                view?.loadUrl(DataManager.appData.privacyLink)
                return super.shouldOverrideUrlLoading(view, request)
            }
        }
        webView.loadUrl(DataManager.appData.privacyLink)

        privacyDialog.setOnDismissListener {
            splashViewModel.onEvent(SettingsUiEvent.ShowHidePrivacyDialog)
        }

        privacyDialog.findViewById<Button>(R.id.okay).setOnClickListener {
            privacyDialog.dismiss()
        }

        if (settingsState.showPrivacyDialog) {
            privacyDialog.show()
        } else {
            privacyDialog.dismiss()
        }
    }

    override fun onResume() {
        super.onResume()
        if (Constants.languageChanged1) {
            recreate()
            Constants.languageChanged1 = false
        }
    }

}





















