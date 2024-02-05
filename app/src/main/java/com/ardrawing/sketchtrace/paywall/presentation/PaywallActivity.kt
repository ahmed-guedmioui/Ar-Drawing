package com.ardrawing.sketchtrace.paywall.presentation

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import com.ardrawing.sketchtrace.BuildConfig
import com.ardrawing.sketchtrace.R
import com.ardrawing.sketchtrace.main.presentaion.home.HomeActivity
import com.ardrawing.sketchtrace.paywall.theme.ArDrawingTheme
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

@AndroidEntryPoint
class PaywallActivity : AppCompatActivity() {


    private val paywallViewModel: PaywallViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val isFromGetStarted = intent?.extras?.getBoolean("isFromGetStarted") ?: false

        setContent {
            ArDrawingTheme {
                Surface(
                    modifier = Modifier
                        .background(MaterialTheme.colorScheme.background)
                ) {

                    var offering by remember {
                        mutableStateOf<Offering?>(null)
                    }

                    Purchases.sharedInstance.getOfferingsWith(
                        onError = { error ->
                            finish()
                        },
                        onSuccess = { offerings ->
                            offerings.current?.let { currentOffering ->
                                offering = currentOffering
                            }
                        },
                    )

                    offering?.let {
                        PaywallDialogScreen2(it) {

                            if (isFromGetStarted) {
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
            options = PaywallOptions.Builder(dismissRequest)
                .setOffering(offering)
                .setListener(
                    object : PaywallListener {
                        override fun onPurchaseCompleted(
                            customerInfo: CustomerInfo, storeTransaction: StoreTransaction
                        ) {
                            val date =
                                customerInfo.getExpirationDateForEntitlement(BuildConfig.ENTITLEMENT)

                            paywallViewModel.onEvent(
                                PaywallUiEvent.Subscribe(isSubscribed = true, date = date)
                            )

                            Log.d("REVENUE_CUT", "Purchased")
                        }

                        override fun onRestoreCompleted(customerInfo: CustomerInfo) {
                            val date =
                                customerInfo.getExpirationDateForEntitlement(BuildConfig.ENTITLEMENT)

                            paywallViewModel.onEvent(
                                PaywallUiEvent.Subscribe(isSubscribed = true, date = date)
                            )

                            Log.d("REVENUE_CUT", "Purchased")
                        }

                        override fun onPurchaseCancelled() {
                            Log.d("REVENUE_CUT", "Cancelled")
                            paywallViewModel.onEvent(
                                PaywallUiEvent.Subscribe(isSubscribed = false)
                            )
                        }

                        override fun onPurchaseError(error: PurchasesError) {
                            Log.d("REVENUE_CUT", "Cancelled")
                            paywallViewModel.onEvent(
                                PaywallUiEvent.Subscribe(isSubscribed = false)
                            )
                        }

                        override fun onRestoreError(error: PurchasesError) {
                            Log.d("REVENUE_CUT", "Cancelled")
                            paywallViewModel.onEvent(
                                PaywallUiEvent.Subscribe(isSubscribed = false)
                            )
                        }

                    }
                )
                .build()
        ) { padding ->

            val context = LocalContext.current

            AndroidView(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = padding.calculateBottomPadding()),
                factory = {
                    val view = LayoutInflater.from(context).inflate(
                        R.layout.activity_paywall, null, false
                    )
                    view
                }
            )
        }
    }
}