package com.med.drawing.core.presentation.follow

import android.app.Dialog
import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.Button
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.startActivity
import androidx.lifecycle.lifecycleScope
import com.med.drawing.App
import com.med.drawing.App.Companion.facebook
import com.med.drawing.App.Companion.instagram
import com.med.drawing.App.Companion.tiktok
import com.med.drawing.App.Companion.twitter
import com.med.drawing.R
import com.med.drawing.databinding.ActivityFollowBinding
import com.med.drawing.databinding.ActivitySettingsBinding
import com.med.drawing.util.openDeveloper
import com.med.drawing.util.rateApp
import com.med.drawing.util.shareApp
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class FollowActivity : AppCompatActivity() {

    private lateinit var binding: ActivityFollowBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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




















