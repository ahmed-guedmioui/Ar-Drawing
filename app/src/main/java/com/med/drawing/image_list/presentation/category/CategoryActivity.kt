package com.med.drawing.image_list.presentation.category

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import com.med.drawing.R
import com.med.drawing.camera_trace.presentation.CameraActivity
import com.med.drawing.databinding.ActivityCategoryBinding
import com.med.drawing.image_list.domain.model.images.Image
import com.med.drawing.image_list.data.ImagesManager
import com.med.drawing.sketch.presentation.SketchActivity
import com.med.drawing.util.ads.InterManager
import com.med.drawing.util.ads.NativeManager
import com.med.drawing.util.ads.RewardedManager
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

/**
 * @author Ahmed Guedmioui
 */
@AndroidEntryPoint
class CategoryActivity : AppCompatActivity() {

    private var isTrace = false
    private lateinit var categoryAdapter: CategoryAdapter
    private lateinit var pushAnimation: Animation

    @Inject
    lateinit var prefs: SharedPreferences

    private lateinit var binding: ActivityCategoryBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCategoryBinding.inflate(layoutInflater)
        val view: View = binding.root
        setContentView(view)

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

        binding.back.setOnClickListener { onBackPressed() }

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

    private fun rewarded(
        categoryPosition: Int,
        imagePosition: Int,
        imageItem: Image
    ) {
        RewardedManager.showRewarded(this, object : RewardedManager.OnAdClosedListener {
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

        })
    }

    private fun traceDrawingScreen(imagePath: String) {
        InterManager.showInterstitial(this, object : InterManager.OnAdClosedListener {
            override fun onAdClosed() {
                val intent = Intent(this@CategoryActivity, CameraActivity::class.java)
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
