package com.ardrawing.sketchtrace.core.domain.usecase

import android.annotation.SuppressLint
import android.app.Application
import com.ardrawing.sketchtrace.core.data.DataManager
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
                DataManager.appData.subscriptionExpireDate = formattedDate
            }
        }

    }
}