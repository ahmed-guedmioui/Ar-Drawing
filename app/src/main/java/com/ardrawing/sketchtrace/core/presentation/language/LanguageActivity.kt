package com.ardrawing.sketchtrace.core.presentation.language

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.content.res.AppCompatResources
import androidx.lifecycle.lifecycleScope
import com.ardrawing.sketchtrace.R
import com.ardrawing.sketchtrace.databinding.ActivityLanguageBinding
import com.ardrawing.sketchtrace.core.presentation.tips.TipsActivity
import com.ardrawing.sketchtrace.util.Constants
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
class LanguageActivity : AppCompatActivity() {

    val en = "en"
    val de = "de"
    val es = "es"
    val fr = "fr"
    val ja = "ja"
    val ko = "ko"
    val zh_rCN = "zh"

    private val languageViewModel: LanguageViewModel by viewModels()
    private lateinit var languageState: LanguageState

    private lateinit var binding: ActivityLanguageBinding

    @Inject
    lateinit var prefs: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val languageCode = prefs.getString("language", "en") ?: "en"
        LanguageChanger.changeAppLanguage(languageCode, this)
        binding = ActivityLanguageBinding.inflate(layoutInflater)
        val view: View = binding.root
        setContentView(view)

        lifecycleScope.launch {
            languageViewModel.languageState.collect {
                languageState = it
            }
        }

        NativeManager.loadNative(
            findViewById(R.id.native_frame),
            findViewById(R.id.native_temp),
            this, false
        )


        val fromSplash = intent?.extras?.getBoolean("from_splash")

        if (fromSplash == false) {
            binding.back.visibility = View.VISIBLE
            binding.back.setOnClickListener {
                finish()
            }
        }

        binding.apply.setOnClickListener {
            InterManager.showInterstitial(this, object : InterManager.OnAdClosedListener {
                override fun onAdClosed() {
                    prefs.edit().putString("language", languageState.language).apply()
                    prefs.edit().putBoolean("language_chosen", true).apply()

                    LanguageChanger.changeAppLanguage(languageState.language, this@LanguageActivity)

                    if (fromSplash == true) {
                        startActivity(Intent(this@LanguageActivity, TipsActivity::class.java))
                    } else {
                        Constants.languageChanged1 = true
                        Constants.languageChanged2 = true
                    }

                    finish()
                }
            })
        }

        val language = prefs.getString("language", en)
        languageViewModel.onEvent(LanguageUiEvent.ChangeLanguage(language ?: en))
        changeLanguage()

        binding.english.setOnClickListener {
            languageViewModel.onEvent(LanguageUiEvent.ChangeLanguage(en))
            changeLanguage()
        }

        binding.germany.setOnClickListener {
            languageViewModel.onEvent(LanguageUiEvent.ChangeLanguage(de))
            changeLanguage()
        }

        binding.chinese.setOnClickListener {
            languageViewModel.onEvent(LanguageUiEvent.ChangeLanguage(zh_rCN))
            changeLanguage()
        }

        binding.korean.setOnClickListener {
            languageViewModel.onEvent(LanguageUiEvent.ChangeLanguage(ko))
            changeLanguage()
        }

        binding.spanish.setOnClickListener {
            languageViewModel.onEvent(LanguageUiEvent.ChangeLanguage(es))
            changeLanguage()
        }

        binding.french.setOnClickListener {
            languageViewModel.onEvent(LanguageUiEvent.ChangeLanguage(fr))
            changeLanguage()
        }

        binding.japanese.setOnClickListener {
            languageViewModel.onEvent(LanguageUiEvent.ChangeLanguage(ja))
            changeLanguage()
        }

    }

    private fun changeLanguage() {

        binding.english.background = AppCompatResources.getDrawable(this, R.drawable.language_bg)
        binding.germany.background = AppCompatResources.getDrawable(this, R.drawable.language_bg)
        binding.chinese.background = AppCompatResources.getDrawable(this, R.drawable.language_bg)
        binding.korean.background = AppCompatResources.getDrawable(this, R.drawable.language_bg)
        binding.spanish.background = AppCompatResources.getDrawable(this, R.drawable.language_bg)
        binding.french.background = AppCompatResources.getDrawable(this, R.drawable.language_bg)
        binding.japanese.background = AppCompatResources.getDrawable(this, R.drawable.language_bg)

        when (languageState.language) {
            en -> {
                binding.english.background =
                    AppCompatResources.getDrawable(this, R.drawable.language_selected_bg)
            }

            de -> {
                binding.germany.background =
                    AppCompatResources.getDrawable(this, R.drawable.language_selected_bg)
            }

            zh_rCN -> {
                binding.chinese.background =
                    AppCompatResources.getDrawable(this, R.drawable.language_selected_bg)
            }

            ko -> {
                binding.korean.background =
                    AppCompatResources.getDrawable(this, R.drawable.language_selected_bg)
            }

            es -> {
                binding.spanish.background =
                    AppCompatResources.getDrawable(this, R.drawable.language_selected_bg)
            }

            fr -> {
                binding.french.background =
                    AppCompatResources.getDrawable(this, R.drawable.language_selected_bg)
            }

            ja -> {
                binding.japanese.background =
                    AppCompatResources.getDrawable(this, R.drawable.language_selected_bg)
            }
        }
    }

}















