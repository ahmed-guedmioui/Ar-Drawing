package com.med.drawing.sketch.sketch_list.presentation

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.recyclerview.widget.GridLayoutManager
import com.github.dhaval2404.imagepicker.ImagePicker
import com.med.drawing.R
import com.med.drawing.camera.presentation.CameraActivity
import com.med.drawing.util.ads.InterManager
import com.med.drawing.databinding.ActivitySketchListBinding
import com.med.drawing.other.AppConstant
import com.med.drawing.other.AppConstants
import com.med.drawing.other.HelpActivity
import com.med.drawing.other.HelpActivity2
import com.med.drawing.other.TracePaperActivity
import com.med.drawing.sketch.sketch_list.presentation.Adapter.DrawingListAdapter
import dagger.hilt.android.AndroidEntryPoint
import java.io.File
import java.io.IOException

/**
 * @author Ahmed Guedmioui
 */
@AndroidEntryPoint
class SketchListActivity : AppCompatActivity() {

    private lateinit var selectedImagePath: String
    private var storagePermissionRequestCode = 12

    private lateinit var drawingAdapter: DrawingListAdapter

    private lateinit var pushAnimation: Animation

    private var actionName = "back"
    private var back = "back"
    private var selectedImagePosition = 0
    private var selectedImageName = ""
    private var drawingList = ArrayList<String>()


    private lateinit var binding: ActivitySketchListBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySketchListBinding.inflate(layoutInflater)
        val view: View = binding.root
        setContentView(view)

        pushAnimation = AnimationUtils.loadAnimation(this, R.anim.view_push)
        drawingList = addAssetsImages("sketch_drawing")

        binding.recyclerView.setHasFixedSize(true)
        val gridLayoutManager = GridLayoutManager(this, 3)
        binding.recyclerView.layoutManager = gridLayoutManager

        binding.relHelp.setOnClickListener {
            it.startAnimation(pushAnimation)
            if (AppConstant.selected_id == AppConstant.TraceDirect) {
                helpScreen()
            } else {
                helpScreen2()
            }
        }

        val drawingListAdapter: DrawingListAdapter = object : DrawingListAdapter(
            this,
            drawingList
        ) {
            override fun onDrawingListClickItem(i: Int, view: View) {
                selectedImagePath = drawingList[i]
                actionName = if (AppConstant.selected_id == AppConstant.TraceDirect) {
                    AppConstant.TraceDirect
                } else {
                    AppConstant.TracePaper
                }
                selectedImagePosition = i
                selectedImageName = "Image_$i"
                if (selectedImagePosition > 5) {
                    if (AppConstant.selected_id == AppConstant.TraceDirect) {
                        traceDrawingScreen()
                    } else {
                        tracePaperScreen()
                    }
                } else {
                    traceDrawingScreen()
                }
            }

            override fun onGalleryClickItem(i: Int, view: View) {
                if (isWriteStoragePermissionGranted()) {
                    ImagePicker.with(this@SketchListActivity).galleryOnly().start()
                }
            }
        }

        drawingAdapter = drawingListAdapter
        binding.recyclerView.adapter = drawingListAdapter
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
        ImagePicker.with(this@SketchListActivity).galleryOnly().start()
    }

    private fun addAssetsImages(str: String): ArrayList<String> {
        val arrayList = ArrayList<String>()
        try {
            arrayList.add("Gallery")
            for (str2 in assets.list(str)!!) {
                arrayList.add(str + File.separator + str2)
                Log.e("pathList item", str + File.separator + str2)
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return arrayList
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
            selectedImagePath = AppConstant.getRealPathFromURI_API19(this, intent.data)
            if (AppConstant.selected_id == AppConstant.TraceDirect) {
                actionName = AppConstant.TraceDirect
            } else {
                actionName = AppConstant.TracePaper
            }
            if (AppConstant.selected_id == AppConstant.TraceDirect) {
                traceDrawingScreen()
            } else {
                tracePaperScreen()
            }
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        actionName = back
        backScreen()
    }

    private fun backScreen() {
        finish()
        AppConstants.overridePendingTransitionExit(this)
    }

    private fun tracePaperScreen() {
        InterManager.showInterstitial(this, object : InterManager.OnAdClosedListener {
            override fun onAdClosed() {
                val intent = Intent(this@SketchListActivity, TracePaperActivity::class.java)
                intent.putExtra("ImagePath", selectedImagePath)
                startActivity(intent)
            }
        })
    }

    private fun traceDrawingScreen() {
        InterManager.showInterstitial(this, object : InterManager.OnAdClosedListener {
            override fun onAdClosed() {
                val intent = Intent(this@SketchListActivity, CameraActivity::class.java)
                intent.putExtra("ImagePath", selectedImagePath)
                startActivity(intent)
            }
        })
    }
}
