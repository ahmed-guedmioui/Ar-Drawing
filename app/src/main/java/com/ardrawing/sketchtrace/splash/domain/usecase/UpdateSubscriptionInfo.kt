package com.ardrawing.sketchtrace.splash.domain.usecase

import android.annotation.SuppressLint
import android.app.Application
import com.ardrawing.sketchtrace.R
import com.ardrawing.sketchtrace.splash.data.DataManager
import java.text.SimpleDateFormat
import java.util.Date

/**
 * @author Ahmed Guedmioui
 */

class UpdateSubscriptionInfo(
    private val application: Application,
    private val date: Date?
) {

    @SuppressLint("SimpleDateFormat")
    operator fun invoke() {
        if (date != null) {
            val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
            val formattedDate: String = dateFormat.format(date)

            if (date.after(Date())) {
                DataManager.appData.subscriptionInfo =
                    application.getString(
                        R.string.your_subscription_will_expire_in_n, formattedDate
                    )
            } else {
                DataManager.appData.subscriptionInfo =
                    application.getString(R.string.your_are_not_subscribed)
            }
        } else {
            DataManager.appData.subscriptionInfo =
                application.getString(R.string.your_are_not_subscribed)
        }

    }
}