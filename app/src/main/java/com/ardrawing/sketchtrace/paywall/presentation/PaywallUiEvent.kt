package com.ardrawing.sketchtrace.paywall.presentation

import java.util.Date

/**
 * @author Ahmed Guedmioui
 */
sealed interface PaywallUiEvent {

    data class Subscribe(
        val isSubscribed: Boolean,
        val date: Date? = null
    ) : PaywallUiEvent
}