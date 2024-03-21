package com.ardrawing.sketchtrace.paywall.presentation

import android.annotation.SuppressLint
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.content.res.AppCompatResources
import androidx.cardview.widget.CardView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.lifecycleScope
import androidx.viewpager2.widget.ViewPager2
import com.ardrawing.sketchtrace.BuildConfig
import com.ardrawing.sketchtrace.R
import com.ardrawing.sketchtrace.databinding.ActivitySplashBinding
import com.ardrawing.sketchtrace.core.presentation.home.HomeActivity
import com.ardrawing.sketchtrace.paywall.theme.ArDrawingTheme
import com.ardrawing.sketchtrace.util.LanguageChanger
import com.revenuecat.purchases.CustomerInfo
import com.revenuecat.purchases.Offering
import com.revenuecat.purchases.Purchases
import com.revenuecat.purchases.PurchasesError
import com.revenuecat.purchases.getOfferingsWith
import com.revenuecat.purchases.models.StoreTransaction
import com.revenuecat.purchases.ui.revenuecatui.ExperimentalPreviewRevenueCatUIPurchasesAPI
import com.revenuecat.purchases.ui.revenuecatui.PaywallFooter
import com.revenuecat.purchases.ui.revenuecatui.PaywallListener
import com.revenuecat.purchases.ui.revenuecatui.PaywallOptions
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.properties.Delegates

@AndroidEntryPoint
class PaywallActivity : AppCompatActivity() {

    @Inject
    lateinit var prefs: SharedPreferences

    private lateinit var reviews: List<String>
    private lateinit var images: List<Drawable?>

    private var view: View? = null

    private lateinit var reviewsViewPager: ViewPager2
    private lateinit var imagesViewPager: ViewPager2
    private lateinit var reviewsViewPagerAdapter: ReviewsViewPagerAdapter
    private lateinit var imagesViewPagerAdapter: ImagesViewPagerAdapter

    private val autoSwipeHandler = Handler(Looper.getMainLooper())
    private lateinit var autoSwipeRunnable: Runnable
    private var isAutoSwipeRunnable = false

    private val paywallViewModel: PaywallViewModel by viewModels()
    private lateinit var paywallState: PaywallState

    private var toHome by Delegates.notNull<Boolean>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val languageCode = prefs.getString("language", "en") ?: "en"
        LanguageChanger.changeAppLanguage(languageCode, this)

        toHome = intent?.extras?.getBoolean("toHome") ?: false

        lifecycleScope.launch {
            paywallViewModel.paywallState.collect {
                paywallState = it
                showHideFAQs()
            }
        }

        images = listOf(
            AppCompatResources.getDrawable(this, R.drawable.step_by_step_1),
            AppCompatResources.getDrawable(this, R.drawable.step_by_step_2),
            AppCompatResources.getDrawable(this, R.drawable.step_by_step_3),
            AppCompatResources.getDrawable(this, R.drawable.step_by_step_4),
            AppCompatResources.getDrawable(this, R.drawable.step_by_step_5),
        )
        reviews = listOf(
            this.getString(R.string.i_was_able_to_sketch_a_picture_of_my_friend_so_easily),
            this.getString(R.string.everyone_was_amazed_with_my_drawing_and_i_m_so_proud),
            this.getString(R.string.i_highly_recommend_this_app_for_those_who_want_to_learn_drawing),
            this.getString(R.string.sketching_was_always_my_passion_and_this_app_really_boosted_my_drawing_skills),
        )

        setContent {
            ArDrawingTheme {
                Surface(
                    modifier = Modifier.background(MaterialTheme.colorScheme.background)
                ) {

                    LaunchedEffect(paywallViewModel.finishActivityChannel) {
                        paywallViewModel.finishActivityChannel.collect { finish ->
                            if (finish) {
                                finish()
                            }
                        }
                    }

                    paywallState.offering?.let {
                        PaywallDialogScreen2(it) {

                            if (toHome) {
                                Intent(this, HomeActivity::class.java).also { intent ->
                                    startActivity(intent)
                                }
                            }

                            finish()
                        }
                    }

                }
            }
        }
    }

    @OptIn(ExperimentalPreviewRevenueCatUIPurchasesAPI::class)
    @Composable
    private fun PaywallDialogScreen2(offering: Offering, dismissRequest: () -> Unit) {

        PaywallFooter(
            condensed = true,
            options = PaywallOptions.Builder(dismissRequest).setOffering(offering)
                .setListener(object : PaywallListener {
                    override fun onPurchaseCompleted(
                        customerInfo: CustomerInfo, storeTransaction: StoreTransaction
                    ) {
                        val date =
                            customerInfo.getExpirationDateForEntitlement(BuildConfig.ENTITLEMENT)

                        paywallViewModel.onEvent(
                            PaywallUiEvent.Subscribe(isSubscribed = true, date = date)
                        )

                        Log.d("REVENUE_CUT", "onPurchaseCompleted")
                    }

                    override fun onRestoreCompleted(customerInfo: CustomerInfo) {
                        val date =
                            customerInfo.getExpirationDateForEntitlement(BuildConfig.ENTITLEMENT)

                        paywallViewModel.onEvent(
                            PaywallUiEvent.Subscribe(isSubscribed = true, date = date)
                        )

                        Log.d("REVENUE_CUT", "onRestoreCompleted")
                    }

                    override fun onPurchaseCancelled() {
                        Log.d("REVENUE_CUT", "onPurchaseCancelled")
                        paywallViewModel.onEvent(
                            PaywallUiEvent.Subscribe(isSubscribed = false)
                        )
                    }

                    override fun onPurchaseError(error: PurchasesError) {
                        Log.d("REVENUE_CUT", "onPurchaseError")
                        paywallViewModel.onEvent(
                            PaywallUiEvent.Subscribe(isSubscribed = false)
                        )
                    }

                    override fun onRestoreError(error: PurchasesError) {
                        Log.d("REVENUE_CUT", "onRestoreError")
                        paywallViewModel.onEvent(
                            PaywallUiEvent.Subscribe(isSubscribed = false)
                        )
                    }
                }).build()
        ) { padding ->
            PaywallScreen(padding)
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    @Composable
    private fun PaywallScreen(padding: PaddingValues) {
        val context = LocalContext.current

        AndroidView(modifier = Modifier
            .fillMaxSize()
            .padding(bottom = padding.calculateBottomPadding()),
            factory = {
                val view = LayoutInflater.from(context).inflate(
                    R.layout.activity_paywall, null, false
                )
                view
            },
            update = {
                view = it

                if (toHome) {
                    Handler(Looper.getMainLooper()).postDelayed({
                        it.findViewById<CardView>(R.id.close).visibility = View.VISIBLE
                    }, 3000)
                } else {
                    it.findViewById<CardView>(R.id.close).visibility = View.VISIBLE
                }

                it.findViewById<CardView>(R.id.close).setOnClickListener {
                    if (toHome) {
                        Intent(this, HomeActivity::class.java).also { intent ->
                            startActivity(intent)
                        }
                    }

                    finish()
                }

                imagesViewPager = it.findViewById(R.id.imagesViewPager)
                imagesViewPagerAdapter = ImagesViewPagerAdapter(this, images)
                imagesViewPager.adapter = imagesViewPagerAdapter

                reviewsViewPager = it.findViewById(R.id.reviewsViewPager)
                reviewsViewPagerAdapter = ReviewsViewPagerAdapter(this, reviews)
                reviewsViewPager.adapter = reviewsViewPagerAdapter

                it.findViewById<ImageView>(R.id.arrow1).setOnClickListener {
                    paywallViewModel.onEvent(PaywallUiEvent.ShowHideFaq(1))
                }
                it.findViewById<ImageView>(R.id.arrow2).setOnClickListener {
                    paywallViewModel.onEvent(PaywallUiEvent.ShowHideFaq(2))
                }
                it.findViewById<ImageView>(R.id.arrow3).setOnClickListener {
                    paywallViewModel.onEvent(PaywallUiEvent.ShowHideFaq(3))
                }
                it.findViewById<ImageView>(R.id.arrow4).setOnClickListener {
                    paywallViewModel.onEvent(PaywallUiEvent.ShowHideFaq(4))
                }

                startAutoSwipe()
            })
    }

    private fun showHideFAQs() {
        view?.let {
            if (paywallState.faq1Visibility) {
                it.findViewById<ImageView>(R.id.arrow1).setImageDrawable(
                    AppCompatResources.getDrawable(this, R.drawable.ic_arrow_up)
                )
                it.findViewById<TextView>(R.id.a1).visibility = View.VISIBLE
            } else {
                it.findViewById<ImageView>(R.id.arrow1).setImageDrawable(
                    AppCompatResources.getDrawable(this, R.drawable.ic_arrow_down)
                )
                it.findViewById<TextView>(R.id.a1).visibility = View.GONE
            }

            if (paywallState.faq2Visibility) {
                it.findViewById<ImageView>(R.id.arrow2).setImageDrawable(
                    AppCompatResources.getDrawable(this, R.drawable.ic_arrow_up)
                )
                it.findViewById<TextView>(R.id.a2).visibility = View.VISIBLE
            } else {
                it.findViewById<ImageView>(R.id.arrow2).setImageDrawable(
                    AppCompatResources.getDrawable(this, R.drawable.ic_arrow_down)
                )
                it.findViewById<TextView>(R.id.a2).visibility = View.GONE
            }

            if (paywallState.faq3Visibility) {
                it.findViewById<ImageView>(R.id.arrow3).setImageDrawable(
                    AppCompatResources.getDrawable(this, R.drawable.ic_arrow_up)
                )
                it.findViewById<TextView>(R.id.a3).visibility = View.VISIBLE
            } else {
                it.findViewById<ImageView>(R.id.arrow3).setImageDrawable(
                    AppCompatResources.getDrawable(this, R.drawable.ic_arrow_down)
                )
                it.findViewById<TextView>(R.id.a3).visibility = View.GONE
            }

            if (paywallState.faq4Visibility) {
                it.findViewById<ImageView>(R.id.arrow4).setImageDrawable(
                    AppCompatResources.getDrawable(this, R.drawable.ic_arrow_up)
                )
                it.findViewById<TextView>(R.id.a4).visibility = View.VISIBLE
            } else {
                it.findViewById<ImageView>(R.id.arrow4).setImageDrawable(
                    AppCompatResources.getDrawable(this, R.drawable.ic_arrow_down)
                )
                it.findViewById<TextView>(R.id.a4).visibility = View.GONE
            }

        }
    }

    private fun startAutoSwipe() {
        isAutoSwipeRunnable = true
        autoSwipeRunnable = Runnable {
            var reviewsCurrentItem = reviewsViewPager.currentItem
            reviewsCurrentItem++

            if (reviewsCurrentItem >= reviewsViewPagerAdapter.itemCount) {
                reviewsCurrentItem = 0
            }
            reviewsViewPager.setCurrentItem(reviewsCurrentItem, true)


            var imagesCurrentItem = imagesViewPager.currentItem
            imagesCurrentItem++

            if (imagesCurrentItem >= imagesViewPagerAdapter.itemCount) {
                imagesCurrentItem = 0
            }
            imagesViewPager.setCurrentItem(imagesCurrentItem, true)

            autoSwipeHandler.postDelayed(autoSwipeRunnable, AUTO_SWIPE_INTERVAL)
        }
        autoSwipeHandler.postDelayed(autoSwipeRunnable, AUTO_SWIPE_INTERVAL)
    }

    override fun onDestroy() {
        super.onDestroy()
        if (isAutoSwipeRunnable) {
            autoSwipeHandler.removeCallbacks(autoSwipeRunnable)
        }
    }

    companion object {
        private const val AUTO_SWIPE_INTERVAL: Long = 2000
    }
}













