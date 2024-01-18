package com.ardrawing.sketchtrace.main.presentaion.pay_wall

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import com.revenuecat.purchases.CustomerInfo
import com.revenuecat.purchases.Package
import com.revenuecat.purchases.Purchases
import com.revenuecat.purchases.PurchasesError
import com.revenuecat.purchases.getOfferingsWith
import com.revenuecat.purchases.models.StoreTransaction
import com.revenuecat.purchases.ui.revenuecatui.ExperimentalPreviewRevenueCatUIPurchasesAPI
import com.revenuecat.purchases.ui.revenuecatui.PaywallDialog
import com.revenuecat.purchases.ui.revenuecatui.PaywallDialogOptions
import com.revenuecat.purchases.ui.revenuecatui.PaywallListener
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class PayWallActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            PayWallScreen()
        }

        Purchases.sharedInstance.getOfferingsWith(
            onError = { error ->
                Log.d("REVENUE_CUT", "getOfferingsWith: onError: ${error.underlyingErrorMessage}")
            },
            onSuccess = { offerings ->
                Log.d("REVENUE_CUT", "getOfferingsWith: onSuccess: ${offerings.current}")
            }
        )


//        Purchases.sharedInstance.purchaseWith(
//            PurchaseParams.Builder(
//                this, xxx
//            ).build(),
//            onError = { error, userCancelled -> /* No purchase */ },
//            onSuccess = { storeTransaction, customerInfo ->
//                if (customerInfo.entitlements["j"]?.isActive == true) {
//                    // Unlock that great "pro" content
//                }
//
//            }
//        )

    }

    @OptIn(ExperimentalPreviewRevenueCatUIPurchasesAPI::class)
    @Composable
    private fun PayWallScreen() {

        PaywallDialog(
            PaywallDialogOptions.Builder()
                .setRequiredEntitlementIdentifier("pro")
                .setListener(
                    object : PaywallListener {
                        override fun onPurchaseCompleted(
                            customerInfo: CustomerInfo,
                            storeTransaction: StoreTransaction
                        ) {
                            Log.d("REVENUE_CUT", "onPurchaseCompleted")
                        }

                        override fun onRestoreCompleted(customerInfo: CustomerInfo) {
                            Log.d("REVENUE_CUT", "onRestoreCompleted")
                        }

                        override fun onPurchaseCancelled() {
                            Log.d("REVENUE_CUT", "onPurchaseCancelled")
                        }

                        override fun onPurchaseError(error: PurchasesError) {
                            Log.d("REVENUE_CUT", "onPurchaseError")
                        }

                        override fun onRestoreError(error: PurchasesError) {
                            Log.d("REVENUE_CUT", "onRestoreError")
                        }

                        override fun onPurchaseStarted(rcPackage: Package) {
                            Log.d("REVENUE_CUT", "onPurchaseStarted")
                        }

                        override fun onRestoreStarted() {
                            Log.d("REVENUE_CUT", "onRestoreStarted")
                        }
                    }
                )
                .build()
        )
    }
}





















