package com.ardrawing.sketchtrace.core.presentation.get_started

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
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.ardrawing.sketchtrace.R
import com.ardrawing.sketchtrace.databinding.ActivityGetStartedBinding
import com.ardrawing.sketchtrace.core.presentation.home.HomeActivity
import com.ardrawing.sketchtrace.paywall.presentation.PaywallActivity
import com.ardrawing.sketchtrace.core.data.DataManager
import com.ardrawing.sketchtrace.util.LanguageChanger
import com.ardrawing.sketchtrace.util.ads.InterManager
import com.ardrawing.sketchtrace.util.ads.NativeManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * @author Ahmed Guedmioui
 */
@AndroidEntryPoint
class GetStartedActivity : AppCompatActivity() {

    private val getStartedViewModel: GetStartedViewModel by viewModels()

    private lateinit var getStartedState: GetStartedState
    private lateinit var binding: ActivityGetStartedBinding


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
            findViewById(R.id.native_frame),
            findViewById(R.id.native_temp),
            this, false
        )


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
            Intent(this, PaywallActivity::class.java).also {
                it.putExtra("toHome", true)
                startActivity(it)
                finish()
            }
        }
    }

    private fun goToHome() {
        Intent(this, HomeActivity::class.java).also {
            startActivity(it)
            finish()
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















