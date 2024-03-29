package com.ardrawing.sketchtrace.image_list.presentation.categories

import android.app.Activity
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
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.ardrawing.sketchtrace.App
import com.ardrawing.sketchtrace.R
import com.ardrawing.sketchtrace.databinding.ActivityCategoriesBinding
import com.ardrawing.sketchtrace.image_list.domain.repository.ImageCategoriesRepository
import com.ardrawing.sketchtrace.image_list.presentation.category.CategoryActivity
import com.ardrawing.sketchtrace.paywall.presentation.PaywallActivity
import com.ardrawing.sketchtrace.sketch.presentation.SketchActivity
import com.ardrawing.sketchtrace.core.domain.repository.AppDataRepository
import com.ardrawing.sketchtrace.my_creation.presentation.my_creation_list.MyCreationListState
import com.ardrawing.sketchtrace.my_creation.presentation.my_creation_list.MyCreationListViewModel
import com.ardrawing.sketchtrace.trace.presentation.TraceActivity
import com.ardrawing.sketchtrace.util.LanguageChanger
import com.ardrawing.sketchtrace.util.ads.InterManager
import com.ardrawing.sketchtrace.util.ads.RewardedManager
import com.ardrawing.sketchtrace.util.other.AppConstant
import com.ardrawing.sketchtrace.util.other.FileUtils
import com.ardrawing.sketchtrace.util.other.HelpActivity
import com.ardrawing.sketchtrace.util.other.HelpActivity2
import com.github.dhaval2404.imagepicker.ImagePicker
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject


/**
 * @author Ahmed Guedmioui
 */
@AndroidEntryPoint
class CategoriesActivity : AppCompatActivity() {

    private var storagePermissionRequestCode = 12

    private val categoriesViewModel: CategoriesViewModel by viewModels()
    private lateinit var categoriesState: CategoriesState

    @Inject
    lateinit var prefs: SharedPreferences

    private var categoriesAdapter: CategoriesAdapter? = null

    private lateinit var binding: ActivityCategoriesBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val languageCode = prefs.getString("language", "en") ?: "en"
        LanguageChanger.changeAppLanguage(languageCode, this)
        binding = ActivityCategoriesBinding.inflate(layoutInflater)
        val view: View = binding.root

        setContentView(view)

        val bundle = intent.extras
        if (bundle != null) {
            val isTrace = bundle.getBoolean("isTrace", true)

            categoriesViewModel.onEvent(CategoriesUiEvents.UpdateIsTrace(isTrace))
        }

        lifecycleScope.launch {
            categoriesViewModel.categoriesState.collect {
                categoriesState = it
                categoriesAdapter?.notifyDataSetChanged()
                if (categoriesState.isTrace) {
                    binding.title.text = getString(R.string.trace)
                } else {
                    binding.title.text = getString(R.string.sketch)
                }
            }
        }

        lifecycleScope.launch {
            categoriesViewModel.navigateToDrawingChannel.collect { navigate ->
                if (navigate) {
                    categoriesState.clickedImageItem?.let { clickedImageItem ->
                        if (categoriesState.isTrace) {
                            traceDrawingScreen(clickedImageItem.image)
                        } else {
                            sketchDrawingScreen(clickedImageItem.image)
                        }
                    }
                }
            }
        }

        lifecycleScope.launch {
            categoriesViewModel.unlockImageChannel.collect { unlock ->
                if (unlock) {
                    rewarded {
                        categoriesState.clickedImageItem?.locked = false
                        categoriesState.imageCategory?.adapter?.notifyItemChanged(
                            categoriesState.imagePosition
                        )
                    }
                }
            }
        }


        binding.recyclerView.setHasFixedSize(true)
        binding.recyclerView.layoutManager = LinearLayoutManager(this)

        binding.back.setOnClickListener {
            super.onBackPressed()
        }


        val pushAnimation = AnimationUtils.loadAnimation(this, R.anim.view_push)

        binding.relHelp.setOnClickListener {
            it.startAnimation(pushAnimation)
            if (categoriesState.isTrace) {
                helpScreen()
            } else {
                helpScreen2()
            }
        }

        categoriesAdapter = CategoriesAdapter(
            imageCategoryList = categoriesState.imageCategoryList,
            activity = this
        )
        categoriesAdapter?.setClickListener(object : CategoriesAdapter.ClickListener {
            override fun oClick(categoryPosition: Int, imagePosition: Int) {

                categoriesViewModel.onEvent(
                    CategoriesUiEvents.OnImageClick(
                        categoryPosition = categoryPosition,
                        imagePosition = imagePosition
                    )
                )
            }
        })

        categoriesAdapter?.setGalleryAndCameraClickListener(object :
            CategoriesAdapter.GalleryAndCameraClickListener {
            override fun oClick(isGallery: Boolean) {
                categoriesViewModel.onEvent(
                    CategoriesUiEvents.UpdateIsGallery(isGallery)
                )
                rewarded {
                    if (isWriteStoragePermissionGranted()) {
                        if (categoriesState.isGallery) {
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

        categoriesAdapter?.setViewMoreClickListener(object :
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
                            intent.putExtra("isTrace", categoriesState.isTrace)
                            startActivity(intent)
                        }
                    })
            }
        })

        binding.recyclerView.adapter = categoriesAdapter

        writeStoragePermission()
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
                Intent(this, PaywallActivity::class.java).also {
                    startActivity(it)
                }
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

        if (categoriesState.isGallery) {
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
        startActivity(Intent(this, HelpActivity2::class.java))
    }

    private fun helpScreen2() {
        startActivity(Intent(this, HelpActivity::class.java))
    }

    private val startForProfileImageResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
            val resultCode = result.resultCode

            if (resultCode == Activity.RESULT_OK) {
                //Image Uri will not be null for RESULT_OK
                val fileUri = result.data?.data!!

                Log.d("tag_per", "registerForActivityResult: data = null ${result.data == null}")
                val selectedImagePath = if (categoriesState.isGallery) {
                    AppConstant.getRealPathFromURI_API19(this, fileUri)
                } else {
                    FileUtils.getPath(fileUri)
                }

                if (selectedImagePath != null) {
                    if (categoriesState.isTrace) {
                        traceDrawingScreen(selectedImagePath)
                    } else {
                        sketchDrawingScreen(selectedImagePath)
                    }
                } else {
                    Toast.makeText(
                        this,
                        getString(R.string.error_picking_image),
                        Toast.LENGTH_SHORT
                    ).show()
                }

            } else {
                Toast.makeText(
                    this,
                    getString(R.string.error_picking_image),
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

    private fun traceDrawingScreen(imagePath: String) {
        InterManager.showInterstitial(this, object : InterManager.OnAdClosedListener {
            override fun onAdClosed() {
                Intent(
                    this@CategoriesActivity, TraceActivity::class.java
                ).also {
                    it.putExtra("imagePath", imagePath)
                    startActivity(it)
                }
            }
        })
    }

    private fun sketchDrawingScreen(imagePath: String) {
        InterManager.showInterstitial(this, object : InterManager.OnAdClosedListener {
            override fun onAdClosed() {
                Intent(
                    this@CategoriesActivity, SketchActivity::class.java
                ).also {
                    it.putExtra("imagePath", imagePath)
                    startActivity(it)
                }
            }
        })
    }

}
