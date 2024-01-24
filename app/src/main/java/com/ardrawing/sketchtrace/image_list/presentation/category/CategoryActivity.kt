package com.ardrawing.sketchtrace.image_list.presentation.category

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import com.ardrawing.sketchtrace.util.LanguageChanger
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import com.ardrawing.sketchtrace.BuildConfig
import com.ardrawing.sketchtrace.R
import com.ardrawing.sketchtrace.databinding.ActivityCategoryBinding
import com.ardrawing.sketchtrace.image_list.domain.model.images.Image
import com.ardrawing.sketchtrace.image_list.data.ImagesManager
import com.ardrawing.sketchtrace.image_list.domain.repository.ImageCategoriesRepository
import com.ardrawing.sketchtrace.sketch.presentation.SketchActivity
import com.ardrawing.sketchtrace.splash.data.DataManager
import com.ardrawing.sketchtrace.splash.domain.repository.AppDataRepository
import com.ardrawing.sketchtrace.trace.presentation.TraceActivity
import com.ardrawing.sketchtrace.util.ads.InterManager
import com.ardrawing.sketchtrace.util.ads.NativeManager
import com.ardrawing.sketchtrace.util.ads.RewardedManager
import com.revenuecat.purchases.ui.revenuecatui.ExperimentalPreviewRevenueCatUIPurchasesAPI
import com.revenuecat.purchases.ui.revenuecatui.activity.PaywallActivityLauncher
import com.revenuecat.purchases.ui.revenuecatui.activity.PaywallResult
import com.revenuecat.purchases.ui.revenuecatui.activity.PaywallResultHandler
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.util.Date
import javax.inject.Inject

/**
 * @author Ahmed Guedmioui
 */
@OptIn(ExperimentalPreviewRevenueCatUIPurchasesAPI::class)
@AndroidEntryPoint
class CategoryActivity : AppCompatActivity(), PaywallResultHandler {

    private var isTrace = false
    private lateinit var categoryAdapter: CategoryAdapter
    private lateinit var pushAnimation: Animation

    @Inject
    lateinit var prefs: SharedPreferences

    @Inject
    lateinit var appDataRepository: AppDataRepository

    @Inject
    lateinit var imageCategoriesRepository: ImageCategoriesRepository

    private lateinit var paywallActivityLauncher: PaywallActivityLauncher

    private lateinit var binding: ActivityCategoryBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val languageCode = prefs.getString("language", "en") ?: "en"
        LanguageChanger.changeAppLanguage(languageCode, this)
        binding = ActivityCategoryBinding.inflate(layoutInflater)
        val view: View = binding.root
        setContentView(view)

        paywallActivityLauncher = PaywallActivityLauncher(this, this)

        var categoryPosition = 0

        val bundle = intent.extras
        if (bundle != null) {
            categoryPosition = bundle.getInt("categoryPosition", 0)
            isTrace = bundle.getBoolean("isTrace", true)

            binding.title.text = ImagesManager.imageCategoryList[categoryPosition].imageCategoryName
        }

        NativeManager.loadNative(
            findViewById(R.id.native_frame),
            findViewById(R.id.native_temp),
            this
        )

        binding.back.setOnClickListener {
            onBackPressed()
        }
        pushAnimation = AnimationUtils.loadAnimation(this, R.anim.view_push)

        binding.recyclerView.setHasFixedSize(true)
        val gridLayoutManager = GridLayoutManager(this, 3)
        binding.recyclerView.layoutManager = gridLayoutManager


        categoryAdapter = CategoryAdapter(
            this, ImagesManager.imageCategoryList[categoryPosition], 2
        )

        categoryAdapter.setClickListener(object : CategoryAdapter.ClickListener {
            override fun oClick(imagePosition: Int) {

                val imageItem =
                    ImagesManager.imageCategoryList[categoryPosition].imageList[imagePosition]

                if (imageItem.locked) {
                    rewarded(categoryPosition, imagePosition, imageItem)
                } else {
                    if (isTrace) {
                        traceDrawingScreen(imageItem.image)
                    } else {
                        sketchDrawingScreen(imageItem.image)
                    }
                }
            }
        })

        binding.recyclerView.adapter = categoryAdapter

    }

    override fun onActivityResult(result: PaywallResult) {
        when (result) {
            PaywallResult.Cancelled -> {
                Log.d("REVENUE_CUT", "Cancelled")
                lifecycleScope.launch {
                    appDataRepository.setAdsVisibilityForUser()
                }
            }

            is PaywallResult.Error -> {
                Log.d("REVENUE_CUT", "Error")
                lifecycleScope.launch {
                    appDataRepository.setAdsVisibilityForUser()
                }
            }

            is PaywallResult.Purchased -> {

                val date =
                    result.customerInfo.getExpirationDateForEntitlement(BuildConfig.ENTITLEMENT)

                date?.let {
                    if (it.after(Date())) {
                        DataManager.appData.isSubscribed = true

                        lifecycleScope.launch {
                            appDataRepository.setAdsVisibilityForUser()
                            imageCategoriesRepository.setUnlockedImages(it)
                            imageCategoriesRepository.setNativeItems(it)
                        }
                    }
                }

                Log.d("REVENUE_CUT", "Purchased")
            }

            is PaywallResult.Restored -> {
                val date =
                    result.customerInfo.getExpirationDateForEntitlement(BuildConfig.ENTITLEMENT)

                date?.let {
                    if (it.after(Date())) {
                        DataManager.appData.isSubscribed = true

                        lifecycleScope.launch {
                            appDataRepository.setAdsVisibilityForUser()
                            imageCategoriesRepository.setUnlockedImages(it)
                            imageCategoriesRepository.setNativeItems(it)
                        }
                    }
                }

                Log.d("REVENUE_CUT", "Restored")
            }
        }
    }

    private fun rewarded(
        categoryPosition: Int,
        imagePosition: Int,
        imageItem: Image
    ) {
        RewardedManager.showRewarded(
            activity = this,
            adClosedListener = object : RewardedManager.OnAdClosedListener {
                override fun onRewClosed() {}

                override fun onRewFailedToShow() {
                    Toast.makeText(
                        this@CategoryActivity,
                        getString(R.string.ad_is_not_loaded_yet),
                        Toast.LENGTH_SHORT
                    ).show()
                }

                override fun onRewComplete() {
                    ImagesManager.imageCategoryList[categoryPosition]
                        .imageList[imagePosition].locked = false
                    categoryAdapter.notifyItemChanged(imagePosition)
                    prefs.edit().putBoolean(imageItem.prefsId, false).apply()
                }

            },
            onOpenPaywall = {
                paywallActivityLauncher.launchIfNeeded(
                    requiredEntitlementIdentifier = BuildConfig.ENTITLEMENT
                )
            }
        )
    }

    private fun traceDrawingScreen(imagePath: String) {
        InterManager.showInterstitial(this, object : InterManager.OnAdClosedListener {
            override fun onAdClosed() {
                val intent = Intent(this@CategoryActivity, TraceActivity::class.java)
                intent.putExtra("imagePath", imagePath)
                startActivity(intent)
            }
        })
    }

    private fun sketchDrawingScreen(imagePath: String) {
        InterManager.showInterstitial(this, object : InterManager.OnAdClosedListener {
            override fun onAdClosed() {
                val intent = Intent(this@CategoryActivity, SketchActivity::class.java)
                intent.putExtra("imagePath", imagePath)
                startActivity(intent)
            }
        })
    }

}
