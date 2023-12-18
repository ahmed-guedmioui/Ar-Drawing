package com.med.drawing.image_list.presentation

import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.github.dhaval2404.imagepicker.ImagePicker
import com.med.drawing.R
import com.med.drawing.camera_trace.presentation.CameraTraceActivity
import com.med.drawing.databinding.ActivityImageListBinding
import com.med.drawing.image_list.domain.model.images.Image
import com.med.drawing.image_list.presentation.adapter.CategoriesAdapter
import com.med.drawing.other.AppConstant
import com.med.drawing.other.HelpActivity
import com.med.drawing.other.HelpActivity2
import com.med.drawing.image_list.data.ImagesManager
import com.med.drawing.sketch.presentation.SketchActivity
import com.med.drawing.util.ads.InterManager
import com.med.drawing.util.ads.RewardedManager
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

/**
 * @author Ahmed Guedmioui
 */
@AndroidEntryPoint
class ImageListActivity : AppCompatActivity() {

    private var isTrace = false
    private var storagePermissionRequestCode = 12

    private lateinit var drawingAdapter: CategoriesAdapter

    private lateinit var pushAnimation: Animation

    @Inject
    lateinit var prefs: SharedPreferences

    private lateinit var binding: ActivityImageListBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityImageListBinding.inflate(layoutInflater)
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

        binding.back.setOnClickListener { onBackPressed() }

        pushAnimation = AnimationUtils.loadAnimation(this, R.anim.view_push)

        binding.recyclerView.setHasFixedSize(true)
        val gridLayoutManager = LinearLayoutManager(this)
        binding.recyclerView.layoutManager = gridLayoutManager

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

                // Gallery click
//                 if (isWriteStoragePermissionGranted()) {
//                    ImagePicker.with(this@ImageListActivity).galleryOnly().start()
//                }
            }
        })

        drawingAdapter = categoriesAdapter
        binding.recyclerView.adapter = categoriesAdapter
    }

    private fun rewarded(
        categoryPosition: Int,
        imagePosition: Int,
        imageItem: Image
    ) {
        RewardedManager.showRewarded(this, object : RewardedManager.OnAdClosedListener {
            override fun onRewClosed() {}

            override fun onRewComplete() {
                imageItem.locked = false
                ImagesManager.imageCategoryList[categoryPosition]
                    .adapter?.notifyItemChanged(imagePosition)
                prefs.edit().putBoolean(imageItem.prefsId, false).apply()
            }

        })
    }

    private fun isWriteStoragePermissionGranted(): Boolean {
        if (checkSelfPermission("android.permission.WRITE_EXTERNAL_STORAGE") == PackageManager.PERMISSION_GRANTED &&
            checkSelfPermission("android.permission.READ_MEDIA_IMAGES") == PackageManager.PERMISSION_GRANTED &&
            checkSelfPermission("android.permission.CAMERA") == PackageManager.PERMISSION_GRANTED
        ) {
            return true
        }
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

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode != storagePermissionRequestCode) {
            return
        }
        if (grantResults.isNotEmpty() && grantResults[0] == 0 && grantResults[1] == 0) {
            finish()
        }
        ImagePicker.with(this@ImageListActivity).galleryOnly().start()
    }

    private fun helpScreen() {
        startActivity(Intent(this, HelpActivity::class.java))
    }

    private fun helpScreen2() {
        startActivity(Intent(this, HelpActivity2::class.java))
    }

    override fun onActivityResult(i: Int, i2: Int, data: Intent?) {
        super.onActivityResult(i, i2, intent)
        if (i2 == -1) {

            val selectedImagePath = AppConstant.getRealPathFromURI_API19(this, intent.data)

            if (isTrace) {
                traceDrawingScreen(selectedImagePath)
            } else {
                sketchDrawingScreen(selectedImagePath)
            }
        }
    }

    private fun traceDrawingScreen(imagePath: String) {
        InterManager.showInterstitial(this, object : InterManager.OnAdClosedListener {
            override fun onAdClosed() {
                val intent = Intent(this@ImageListActivity, CameraTraceActivity::class.java)
                intent.putExtra("imagePath", imagePath)
                startActivity(intent)
            }
        })
    }

    private fun sketchDrawingScreen(imagePath: String) {
        InterManager.showInterstitial(this, object : InterManager.OnAdClosedListener {
            override fun onAdClosed() {
                val intent = Intent(this@ImageListActivity, SketchActivity::class.java)
                intent.putExtra("imagePath", imagePath)
                startActivity(intent)
            }
        })
    }

}
