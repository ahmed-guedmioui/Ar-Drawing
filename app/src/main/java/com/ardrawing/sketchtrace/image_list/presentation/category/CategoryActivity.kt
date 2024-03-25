package com.ardrawing.sketchtrace.image_list.presentation.category

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import com.ardrawing.sketchtrace.App
import com.ardrawing.sketchtrace.R
import com.ardrawing.sketchtrace.databinding.ActivityCategoryBinding
import com.ardrawing.sketchtrace.image_list.domain.model.images.Image
import com.ardrawing.sketchtrace.image_list.domain.repository.ImageCategoriesRepository
import com.ardrawing.sketchtrace.paywall.presentation.PaywallActivity
import com.ardrawing.sketchtrace.sketch.presentation.SketchActivity
import com.ardrawing.sketchtrace.core.domain.repository.AppDataRepository
import com.ardrawing.sketchtrace.trace.presentation.TraceActivity
import com.ardrawing.sketchtrace.util.LanguageChanger
import com.ardrawing.sketchtrace.util.ads.InterManager
import com.ardrawing.sketchtrace.util.ads.NativeManager
import com.ardrawing.sketchtrace.util.ads.RewardedManager
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

/**
 * @author Ahmed Guedmioui
 */
@AndroidEntryPoint
class CategoryActivity : AppCompatActivity(){

    private var isTrace = false
    private lateinit var categoryAdapter: CategoryAdapter
    private lateinit var pushAnimation: Animation

    @Inject
    lateinit var prefs: SharedPreferences

    @Inject
    lateinit var appDataRepository: AppDataRepository

    @Inject
    lateinit var imageCategoriesRepository: ImageCategoriesRepository

    

    private lateinit var binding: ActivityCategoryBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val languageCode = prefs.getString("language", "en") ?: "en"
        LanguageChanger.changeAppLanguage(languageCode, this)
        binding = ActivityCategoryBinding.inflate(layoutInflater)
        val view: View = binding.root
        setContentView(view)

        

        var categoryPosition = 0

        val bundle = intent.extras
        if (bundle != null) {
            categoryPosition = bundle.getInt("categoryPosition", 0)
            isTrace = bundle.getBoolean("isTrace", true)

            binding.title.text = App.imageCategoryList[categoryPosition].imageCategoryName
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
            this, App.imageCategoryList[categoryPosition], 2
        )

        categoryAdapter.setClickListener(object : CategoryAdapter.ClickListener {
            override fun oClick(imagePosition: Int) {

                val imageItem =
                    App.imageCategoryList[categoryPosition].imageList[imagePosition]

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
                    App.imageCategoryList[categoryPosition]
                        .imageList[imagePosition].locked = false
                    categoryAdapter.notifyItemChanged(imagePosition)
                    prefs.edit().putBoolean(imageItem.prefsId, false).apply()
                }

            },
            onOpenPaywall = {
                Intent(this, PaywallActivity::class.java).also {
                    startActivity(it)
                }
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
