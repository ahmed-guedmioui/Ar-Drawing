package com.med.drawing.main.presentaion.follow

import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import com.med.drawing.util.LanguageChanger
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.med.drawing.App.Companion.facebook
import com.med.drawing.App.Companion.instagram
import com.med.drawing.App.Companion.tiktok
import com.med.drawing.App.Companion.twitter
import com.med.drawing.databinding.ActivityFollowBinding
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class FollowActivity : AppCompatActivity() {

    @Inject
    lateinit var prefs: SharedPreferences

    private lateinit var binding: ActivityFollowBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        val languageCode = prefs.getString("language", "en") ?: "en"
        LanguageChanger.changeAppLanguage(languageCode, this)
        binding = ActivityFollowBinding.inflate(layoutInflater)
        val view: View = binding.root
        setContentView(view)

        binding.back.setOnClickListener {
            onBackPressed()
        }

        binding.tiktok.setOnClickListener {
            openAppWithUserId("tiktok")
        }

        binding.facebook.setOnClickListener {
            openAppWithUserId("facebook")
        }

        binding.instagram.setOnClickListener {
            openAppWithUserId("instagram")
        }

        binding.x.setOnClickListener {
            openAppWithUserId("x")
        }


    }

    private fun openAppWithUserId(name: String) {
        val appIntent = Intent(Intent.ACTION_VIEW)

        when (name) {
            "tiktok" -> { // TikTok
                appIntent.data = Uri.parse("https://www.tiktok.com/@$tiktok")
            }

            "facebook" -> { // Facebook
                appIntent.data = Uri.parse("https://www.facebook.com/$facebook")
            }

            "instagram" -> { // Instagram
                appIntent.data = Uri.parse("https://www.instagram.com/$instagram")
            }

            "x" -> { // Twitter
                appIntent.data = Uri.parse("https://twitter.com/$twitter")
            }
        }

        try {
            startActivity(appIntent)
        } catch (e: ActivityNotFoundException) {
            e.printStackTrace()
        }
    }


}




















