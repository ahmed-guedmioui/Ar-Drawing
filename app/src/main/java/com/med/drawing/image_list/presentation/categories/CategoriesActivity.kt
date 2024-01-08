package com.med.drawing.image_list.presentation.categories

import android.app.Activity
import com.med.drawing.util.LanguageChanger
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
import androidx.recyclerview.widget.LinearLayoutManager
import com.github.dhaval2404.imagepicker.ImagePicker
import com.med.drawing.R
import com.med.drawing.sketch.presentation.SketchActivity
import com.med.drawing.databinding.ActivityCategoriesBinding
import com.med.drawing.image_list.data.ImagesManager
import com.med.drawing.image_list.domain.model.images.Image
import com.med.drawing.image_list.presentation.category.CategoryActivity
import com.med.drawing.trace.presentation.TraceActivity
import com.med.drawing.util.ads.InterManager
import com.med.drawing.util.ads.RewardedManager
import com.med.drawing.util.other.AppConstant
import com.med.drawing.util.other.FileUtils
import com.med.drawing.util.other.HelpActivity
import com.med.drawing.util.other.HelpActivity2
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject


/**
 * @author Ahmed Guedmioui
 */
@AndroidEntryPoint
class CategoriesActivity : AppCompatActivity() {

    private var isTrace = false
    private var isGallery = false
    private var storagePermissionRequestCode = 12

    private lateinit var pushAnimation: Animation

    @Inject
    lateinit var prefs: SharedPreferences

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

        categoriesAdapter.setGalleryAndCameraClickListener(object :
            CategoriesAdapter.GalleryAndCameraClickListener {
            override fun oClick(isGallery: Boolean) {
                this@CategoriesActivity.isGallery = isGallery

                if (isWriteStoragePermissionGranted()) {
                    if (isGallery) {
                        Log.d("tag_per", "isGallery: ImagePicker")
                        ImagePicker.with(this@CategoriesActivity)
                            .galleryOnly()
                            .createIntent { intent ->
                                startForProfileImageResult.launch(intent)
                            }
                    } else {
                        Log.d("tag_per", "isCamera: ImagePicker")
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

    private fun rewarded(
        categoryPosition: Int,
        imagePosition: Int,
        imageItem: Image
    ) {
        RewardedManager.showRewarded(this, object : RewardedManager.OnAdClosedListener {
            override fun onRewClosed() {}

            override fun onRewFailedToShow() {
                Toast.makeText(
                    this@CategoriesActivity,
                    getString(R.string.ad_is_not_loaded_yet),
                    Toast.LENGTH_SHORT
                ).show()
            }

            override fun onRewComplete() {
                imageItem.locked = false
                ImagesManager.imageCategoryList[categoryPosition]
                    .adapter?.notifyItemChanged(imagePosition)
                prefs.edit().putBoolean(imageItem.prefsId, false).apply()
            }

        })
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

        if (grantResults.isEmpty() && grantResults[0] == 0 && grantResults[1] == 0 && grantResults[2] == 0) {
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
