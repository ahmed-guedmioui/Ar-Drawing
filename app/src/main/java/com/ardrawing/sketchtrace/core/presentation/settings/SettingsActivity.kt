package com.ardrawing.sketchtrace.core.presentation.settings

import android.annotation.SuppressLint
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
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Button
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.ardrawing.sketchtrace.App
import com.ardrawing.sketchtrace.R
import com.ardrawing.sketchtrace.databinding.ActivitySettingsBinding
import com.ardrawing.sketchtrace.core.presentation.follow.FollowActivity
import com.ardrawing.sketchtrace.core.presentation.language.LanguageActivity
import com.ardrawing.sketchtrace.core.presentation.settings.adapter.RecommendedAppsAdapter
import com.ardrawing.sketchtrace.core.presentation.tips.TipsActivity
import com.ardrawing.sketchtrace.paywall.presentation.PaywallActivity
import com.ardrawing.sketchtrace.util.Constants
import com.ardrawing.sketchtrace.util.LanguageChanger
import com.ardrawing.sketchtrace.util.openDeveloper
import com.ardrawing.sketchtrace.util.rateApp
import com.ardrawing.sketchtrace.util.shareApp
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class SettingsActivity : AppCompatActivity() {

    private val settingsViewModel: SettingsViewModel by viewModels()

    private lateinit var settingsState: SettingsState
    private lateinit var binding: ActivitySettingsBinding


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
            settingsViewModel.settingsState.collect {
                settingsState = it
                privacyDialog()
            }
        }

        if (App.appData.showRecommendedApps) {
            binding.recommendedAppsRecyclerView.layoutManager =
                LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)

            binding.recommendedAppsRecyclerView.adapter =
                RecommendedAppsAdapter(
                    this
                )
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
            settingsViewModel.onEvent(SettingsUiEvent.ShowHidePrivacyDialog)
        }

        binding.language.setOnClickListener {
            val intent = Intent(this, LanguageActivity::class.java)
            intent.putExtra("from_splash", false)
            startActivity(intent)
        }

        binding.subscribe.setOnClickListener {
            if (App.appData.isSubscribed) {
                Toast.makeText(
                    this, getString(R.string.you_are_already_subscribed), Toast.LENGTH_SHORT
                ).show()
            } else {
                Intent(this, PaywallActivity::class.java).also {
                    startActivity(it)
                }
            }
        }

        if (App.appData.isSubscribed) {
            binding.subscribeInfo.text = getString(
                R.string.your_subscription_will_expire_in_n,
                App.appData.subscriptionExpireDate
            )
        } else {
            binding.subscribeInfo.text = getString(R.string.your_are_not_subscribed)
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
                view?.loadUrl(App.appData.privacyLink)
                return super.shouldOverrideUrlLoading(view, request)
            }
        }
        webView.loadUrl(App.appData.privacyLink)

        privacyDialog.setOnDismissListener {
            settingsViewModel.onEvent(SettingsUiEvent.ShowHidePrivacyDialog)
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
        
        if (App.appData.isSubscribed) {
            binding.subscribeInfo.text = getString(
                R.string.your_subscription_will_expire_in_n,
                App.appData.subscriptionExpireDate
            )
        } else {
            binding.subscribeInfo.text = getString(R.string.your_are_not_subscribed)
        }
    }

}





















