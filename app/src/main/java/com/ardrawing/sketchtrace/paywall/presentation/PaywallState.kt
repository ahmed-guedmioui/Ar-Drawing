package com.ardrawing.sketchtrace.paywall.presentation

import com.revenuecat.purchases.Offering

/**
 * @author Ahmed Guedmioui
 */
data class PaywallState(
    val faq1Visibility: Boolean = false,
    val faq2Visibility: Boolean = false,
    val faq3Visibility: Boolean = false,
    val faq4Visibility: Boolean = false,

    val reviews: List<String> = emptyList(),

    val offering: Offering? = null
)
