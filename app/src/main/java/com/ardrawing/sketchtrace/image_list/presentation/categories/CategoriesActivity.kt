package com.ardrawing.sketchtrace.image_list.presentation.categories

import android.app.Activity
import com.ardrawing.sketchtrace.util.LanguageChanger
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.ardrawing.sketchtrace.BuildConfig
import com.github.dhaval2404.imagepicker.ImagePicker
import com.ardrawing.sketchtrace.R
import com.ardrawing.sketchtrace.sketch.presentation.SketchActivity
import com.ardrawing.sketchtrace.databinding.ActivityCategoriesBinding
import com.ardrawing.sketchtrace.image_list.data.ImagesManager
import com.ardrawing.sketchtrace.image_list.domain.repository.ImageCategoriesRepository
import com.ardrawing.sketchtrace.image_list.presentation.category.CategoryActivity
import com.ardrawing.sketchtrace.splash.data.DataManager
import com.ardrawing.sketchtrace.splash.domain.repository.AppDataRepository
import com.ardrawing.sketchtrace.trace.presentation.TraceActivity
import com.ardrawing.sketchtrace.util.ads.InterManager
import com.ardrawing.sketchtrace.util.ads.RewardedManager
import com.ardrawing.sketchtrace.util.other.AppConstant
import com.ardrawing.sketchtrace.util.other.FileUtils
import com.ardrawing.sketchtrace.util.other.HelpActivity
import com.ardrawing.sketchtrace.util.other.HelpActivity2
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
class CategoriesActivity : AppCompatActivity(), PaywallResultHandler {

    private var isTrace = false
    private var isGallery = false
    private var storagePermissionRequestCode = 12

    private lateinit var pushAnimation: Animation

    @Inject
    lateinit var prefs: SharedPreferences

    @Inject
    lateinit var appDataRepository: AppDataRepository

    @Inject
    lateinit var imageCategoriesRepository: ImageCategoriesRepository

    private lateinit var paywallActivityLauncher: PaywallActivityLauncher


    private lateinit var binding: ActivityCategoriesBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val languageCode = prefs.getString("language", "en") ?: "en"
        LanguageChanger.changeAppLanguage(languageCode, this)
        binding = ActivityCategoriesBinding.inflate(layoutInflater)
        val view: View = binding.root
        setContentView(view)

        paywallActivityLauncher = PaywallActivityLauncher(this, this)

        val bundle = intent.extras
        if (bundle != null) {
            isTrace = bundle.getBoolean("isTrace", true)
            if (isTrace) {
                binding.title.text = getString(R.string.trace)
            } else {
                binding.title.text = getString(R.string.sketch)
            }

        }

        pushAnimation = AnimationUtils.loadAnimation(this, R.anim.view_push)

        binding.recyclerView.setHasFixedSize(true)
        binding.recyclerView.layoutManager = LinearLayoutManager(this)

        binding.back.setOnClickListener {
            super.onBackPressed()
        }

        binding.relHelp.setOnClickListener {
            it.startAnimation(pushAnimation)
            if (AppConstant.selected_id == AppConstant.TraceDirect) {
                helpScreen()
            } else {
                helpScreen2()
            }
        }

        val categoriesAdapter = CategoriesAdapter(this)
        categoriesAdapter.setClickListener(object : CategoriesAdapter.ClickListener {
            override fun oClick(categoryPosition: Int, imagePosition: Int) {

                val imageItem =
                    ImagesManager.imageCategoryList[categoryPosition].imageList[imagePosition]

                if (imageItem.locked) {
                    rewarded {
                        imageItem.locked = false
                        ImagesManager.imageCategoryList[categoryPosition]
                            .adapter?.notifyItemChanged(imagePosition)
                        prefs.edit().putBoolean(imageItem.prefsId, false).apply()
                    }
                } else {
                    if (isTrace) {
                        traceDrawingScreen(imageItem.image)
                    } else {
                        sketchDrawingScreen(imageItem.image)
                    }
                }
            }
        })

        categoriesAdapter.setGalleryAndCameraClickListener(object :
            CategoriesAdapter.GalleryAndCameraClickListener {
            override fun oClick(isGallery: Boolean) {
                this@CategoriesActivity.isGallery = isGallery
                rewarded {
                    if (isWriteStoragePermissionGranted()) {
                        if (isGallery) {
                            ImagePicker.with(this@CategoriesActivity)
                                .galleryOnly()
                                .createIntent { intent ->
                                    startForProfileImageResult.launch(intent)
                                }
                        } else {
                            getExternalFilesDir(Environment.DIRECTORY_DCIM)?.let { it1 ->
                                ImagePicker.with(this@CategoriesActivity)
                                    .cameraOnly()
                                    .saveDir(it1)
                                    .createIntent { intent ->
                                        startForProfileImageResult.launch(intent)
                                    }
                            }
                        }

                    }
                }
            }
        })

        categoriesAdapter.setViewMoreClickListener(object :
            CategoriesAdapter.ViewMoreClickListener {
            override fun oClick(categoryPosition: Int) {
                InterManager.showInterstitial(
                    this@CategoriesActivity,
                    object : InterManager.OnAdClosedListener {
                        override fun onAdClosed() {
                            val intent = Intent(
                                this@CategoriesActivity, CategoryActivity::class.java
                            )
                            intent.putExtra("categoryPosition", categoryPosition)
                            intent.putExtra("isTrace", isTrace)
                            startActivity(intent)
                        }
                    })
            }
        })

        binding.recyclerView.adapter = categoriesAdapter


        writeStoragePermission()
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
        onRewComplete: () -> Unit
    ) {
        RewardedManager.showRewarded(
            activity = this,
            adClosedListener = object : RewardedManager.OnAdClosedListener {
                override fun onRewClosed() {}

                override fun onRewFailedToShow() {
                    Toast.makeText(
                        this@CategoriesActivity,
                        getString(R.string.ad_is_not_loaded_yet),
                        Toast.LENGTH_SHORT
                    ).show()

                }

                override fun onRewComplete() {
                    onRewComplete()
                }
            },
            onOpenPaywall = {
                paywallActivityLauncher.launchIfNeeded(
                    requiredEntitlementIdentifier = BuildConfig.ENTITLEMENT
                )
            }
        )
    }

    private fun isWriteStoragePermissionGranted(): Boolean {
        if (
            checkSelfPermission("android.permission.WRITE_EXTERNAL_STORAGE") == PackageManager.PERMISSION_GRANTED &&
            checkSelfPermission("android.permission.READ_MEDIA_IMAGES") == PackageManager.PERMISSION_GRANTED &&
            checkSelfPermission("android.permission.CAMERA") == PackageManager.PERMISSION_GRANTED
        ) {
            Log.d("tag_per", "isWriteStoragePermissionGranted: true")
            return true
        }

        Log.d("tag_per", "isWriteStoragePermissionGranted: requestPermissions")
        ActivityCompat.requestPermissions(
            this,
            arrayOf(
                "android.permission.WRITE_EXTERNAL_STORAGE",
                "android.permission.READ_MEDIA_IMAGES",
                "android.permission.CAMERA"
            ),
            storagePermissionRequestCode
        )
        return false
    }

    private fun writeStoragePermission() {
        if (
            checkSelfPermission("android.permission.WRITE_EXTERNAL_STORAGE") == PackageManager.PERMISSION_GRANTED &&
            checkSelfPermission("android.permission.READ_MEDIA_IMAGES") == PackageManager.PERMISSION_GRANTED
        ) {
            return
        }

        ActivityCompat.requestPermissions(
            this, arrayOf(
                "android.permission.WRITE_EXTERNAL_STORAGE",
                "android.permission.READ_MEDIA_IMAGES"
            ), 20011
        )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode != storagePermissionRequestCode) {
            return
        }

        if (grantResults.isEmpty() || grantResults[0] == 0 && grantResults[1] == 0 && grantResults[2] == 0) {
            return
        }

        if (isGallery) {
            ImagePicker.with(this)
                .galleryOnly()
                .createIntent { intent ->
                    startForProfileImageResult.launch(intent)
                }
        } else {
            getExternalFilesDir(Environment.DIRECTORY_DCIM)?.let { it1 ->
                ImagePicker.with(this@CategoriesActivity)
                    .cameraOnly()
                    .saveDir(it1)
                    .createIntent { intent ->
                        startForProfileImageResult.launch(intent)
                    }
            }
        }
    }

    private fun helpScreen() {
        startActivity(Intent(this, HelpActivity::class.java))
    }

    private fun helpScreen2() {
        startActivity(Intent(this, HelpActivity2::class.java))
    }

    private val startForProfileImageResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
            val resultCode = result.resultCode

            if (resultCode == Activity.RESULT_OK) {
                //Image Uri will not be null for RESULT_OK
                val fileUri = result.data?.data!!

                Log.d("tag_per", "registerForActivityResult: data = null ${result.data == null}")
                val selectedImagePath = if (isGallery) {
                    AppConstant.getRealPathFromURI_API19(this, fileUri)
                } else {
                    FileUtils.getPath(fileUri)
                }

                if (isTrace) {
                    traceDrawingScreen(selectedImagePath)
                } else {
                    sketchDrawingScreen(selectedImagePath)
                }
            } else {
                Toast.makeText(this, "Error picking image", Toast.LENGTH_SHORT).show()
            }
        }

    private fun traceDrawingScreen(imagePath: String) {
        InterManager.showInterstitial(this, object : InterManager.OnAdClosedListener {
            override fun onAdClosed() {
                val intent = Intent(this@CategoriesActivity, TraceActivity::class.java)
                intent.putExtra("imagePath", imagePath)
                startActivity(intent)
            }
        })
    }

    private fun sketchDrawingScreen(imagePath: String) {
        InterManager.showInterstitial(this, object : InterManager.OnAdClosedListener {
            override fun onAdClosed() {
                val intent = Intent(this@CategoriesActivity, SketchActivity::class.java)
                intent.putExtra("imagePath", imagePath)
                startActivity(intent)
            }
        })
    }

}
