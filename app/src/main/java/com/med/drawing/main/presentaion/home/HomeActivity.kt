package com.med.drawing.main.presentaion.home

import android.app.Dialog
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.os.StrictMode
import android.view.Gravity
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.RatingBar
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.lifecycle.lifecycleScope
import androidx.viewpager.widget.ViewPager
import com.med.drawing.BuildConfig
import com.med.drawing.R
import com.med.drawing.databinding.ActivityHomeBinding
import com.med.drawing.image_list.presentation.categories.CategoriesActivity
import com.med.drawing.main.presentaion.home.adapter.HelperPagerAdapter
import com.med.drawing.main.presentaion.settings.SettingsActivity
import com.med.drawing.my_creation.presentation.my_creation_list.MyCreationListActivity
import com.med.drawing.util.Constants
import com.med.drawing.util.UrlOpener
import com.med.drawing.util.ads.InterManager
import com.med.drawing.util.ads.NativeManager
import com.med.drawing.util.ads.RewardedManager
import com.med.drawing.util.rateApp
import com.med.drawing.util.LanguageChanger
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * @author Ahmed Guedmioui
 */
@AndroidEntryPoint
class HomeActivity : AppCompatActivity() {

    private val homeViewModel: HomeViewModel by viewModels()
    private lateinit var homeState: HomeState

    private lateinit var binding: ActivityHomeBinding

    @Inject
    lateinit var prefs: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        val languageCode = prefs.getString("language", "en") ?: "en"
        LanguageChanger.changeAppLanguage(languageCode, this)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        val view: View = binding.root
        setContentView(view)

        if (prefs.getBoolean("showRating", true)) {
            rateDialog()
            prefs.edit().putBoolean("showRating", false).apply()
        } else {
            prefs.edit().putBoolean("showRating", true).apply()
        }

        RewardedManager.loadRewarded(this)

        lifecycleScope.launch {
            homeViewModel.homeState.collect {
                homeState = it
                helperDialog()
            }
        }

        lifecycleScope.launch {
            homeViewModel.doubleTapToastChannel.collectLatest { showToasts ->
                if (showToasts) {
                    Toast.makeText(
                        this@HomeActivity,
                        getString(R.string.double_click_to_exit),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }

        NativeManager.loadNative(
            findViewById(R.id.native_frame),
            findViewById(R.id.native_temp),
            this, true
        )

        val pushAnimation = AnimationUtils.loadAnimation(this, R.anim.view_push)

        try {
            StrictMode::class.java.getMethod(
                "disableDeathOnFileUriExposure", *arrayOfNulls(0)
            ).invoke(null, *arrayOfNulls(0))
        } catch (e: Exception) {
            e.printStackTrace()
        }

        binding.settings.setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
        }

        binding.sketch.setOnClickListener {
            it.startAnimation(pushAnimation)
            drawingListScreen(false)
        }

        binding.trace.setOnClickListener {
            it.startAnimation(pushAnimation)
            drawingListScreen(true)
        }

        binding.creation.setOnClickListener {
            it.startAnimation(pushAnimation)
            startActivity(Intent(this, MyCreationListActivity::class.java))
        }

        binding.rate.setOnClickListener {
            it.startAnimation(pushAnimation)
            rateApp(this)
        }

        binding.rateBtn.setOnClickListener {
            rateApp(this)
        }

        binding.helper.setOnClickListener {
            homeViewModel.onEvent(HomeUiEvent.ShowHideHelperDialog)
        }

    }

    private fun helperDialog() {
        val helperDialog = Dialog(this)
        helperDialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        helperDialog.setCancelable(true)
        helperDialog.setContentView(R.layout.dialog_helper)
        val layoutParams = WindowManager.LayoutParams()

        layoutParams.copyFrom(helperDialog.window!!.attributes)
        layoutParams.width = WindowManager.LayoutParams.MATCH_PARENT
        layoutParams.height = WindowManager.LayoutParams.WRAP_CONTENT
        layoutParams.gravity = Gravity.CENTER

        helperDialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        helperDialog.window!!.attributes = layoutParams

        helperDialog.findViewById<ImageView>(R.id.close).setOnClickListener {
            helperDialog.dismiss()
        }

        helperDialog.setOnDismissListener {
            homeViewModel.onEvent(HomeUiEvent.ShowHideHelperDialog)
        }
        if (homeState.showHelperDialog) {
            helperDialog.show()
        } else {
            helperDialog.dismiss()
        }

        val viewPager = helperDialog.findViewById<ViewPager>(R.id.viewPager)
        val adapter = HelperPagerAdapter(this)
        viewPager.adapter = adapter

        viewPager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrolled(
                position: Int, positionOffset: Float, positionOffsetPixels: Int
            ) {
            }

            override fun onPageSelected(position: Int) {
                when (position) {
                    0 -> {
                        helperDialog.findViewById<CardView>(R.id.dot_1)
                            .setCardBackgroundColor(getColor(R.color.primary_3))
                        helperDialog.findViewById<CardView>(R.id.dot_2)
                            .setCardBackgroundColor(getColor(R.color.primary_2))
                    }

                    1 -> {
                        helperDialog.findViewById<CardView>(R.id.dot_2)
                            .setCardBackgroundColor(getColor(R.color.primary_3))
                        helperDialog.findViewById<CardView>(R.id.dot_1)
                            .setCardBackgroundColor(getColor(R.color.primary_2))
                    }
                }
            }

            override fun onPageScrollStateChanged(state: Int) {
            }
        })
    }

    private fun drawingListScreen(isTrace: Boolean) {
        InterManager.showInterstitial(this, object : InterManager.OnAdClosedListener {
            override fun onAdClosed() {
                val intent = Intent(this@HomeActivity, CategoriesActivity::class.java)
                intent.putExtra("isTrace", isTrace)
                startActivity(intent)
            }
        })
    }

    override fun onBackPressed() {
        homeViewModel.onEvent(HomeUiEvent.BackPressed)
        lifecycleScope.launch {
            homeViewModel.closeChannel.collectLatest { close ->
                if (close) {
                    super.onBackPressed()
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()

        if (Constants.languageChanged2) {
            recreate()
            Constants.languageChanged2 = false
        }
    }


    private fun rateDialog() {
        val rateDialog = Dialog(this)
        rateDialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        rateDialog.setCancelable(true)
        rateDialog.setContentView(R.layout.dialog_rate_app)
        val layoutParams = WindowManager.LayoutParams()

        layoutParams.copyFrom(rateDialog.window!!.attributes)
        layoutParams.width = WindowManager.LayoutParams.MATCH_PARENT
        layoutParams.height = WindowManager.LayoutParams.WRAP_CONTENT
        layoutParams.gravity = Gravity.CENTER

        rateDialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        rateDialog.window!!.attributes = layoutParams

        rateDialog.findViewById<ImageView>(R.id.close).setOnClickListener {
            rateDialog.dismiss()
        }

        rateDialog.findViewById<RatingBar>(R.id.rating_bar).onRatingBarChangeListener =
            RatingBar.OnRatingBarChangeListener { ratingBar, rating, fromUser ->
                if (rating > 3.5) {
                    UrlOpener.open(this, BuildConfig.APPLICATION_ID)
                } else {
                    Toast.makeText(
                        this, "Thanks for rating 😄", Toast.LENGTH_SHORT
                    ).show()
                }
            }

    }

}
















