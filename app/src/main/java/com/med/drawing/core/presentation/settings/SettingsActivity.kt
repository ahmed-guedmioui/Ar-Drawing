package com.med.drawing.core.presentation.settings

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
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView.LayoutManager
import com.med.drawing.App
import com.med.drawing.R
import com.med.drawing.core.presentation.follow.FollowActivity
import com.med.drawing.core.presentation.settings.adapter.RecommendedAppsAdapter
import com.med.drawing.databinding.ActivitySettingsBinding
import com.med.drawing.util.AppDataManager
import com.med.drawing.util.openDeveloper
import com.med.drawing.util.rateApp
import com.med.drawing.util.shareApp
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class SettingsActivity : AppCompatActivity() {

    private val splashViewModel: SettingsViewModel by viewModels()

    private lateinit var settingsState: SettingsState
    private lateinit var binding: ActivitySettingsBinding

    @Inject
    lateinit var prefs: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        val view: View = binding.root
        setContentView(view)

        lifecycleScope.launch {
            splashViewModel.settingsState.collect {
                settingsState = it
                privacyDialog()
            }
        }

        if (AppDataManager.appData.showRecommendedApps) {
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

        binding.privacy.setOnClickListener {
            splashViewModel.onEvent(SettingsUiEvent.ShowHidePrivacyDialog)
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

        privacyDialog.findViewById<TextView>(R.id.privacy_policy).text = App.PRIVACY

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

}





















