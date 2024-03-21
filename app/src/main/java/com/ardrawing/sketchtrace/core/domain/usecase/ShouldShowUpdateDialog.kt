package com.ardrawing.sketchtrace.core.domain.usecase

import com.ardrawing.sketchtrace.BuildConfig
import com.ardrawing.sketchtrace.core.data.DataManager

/**
 * @author Ahmed Guedmioui
 */
class ShouldShowUpdateDialog {

    operator fun invoke(): Int {

        if (DataManager.appData.isAppSuspended) {
            return 2
        }

        if (BuildConfig.VERSION_CODE < DataManager.appData.appLatestVersion) {
            return 1
        }

        return 0
    }
}