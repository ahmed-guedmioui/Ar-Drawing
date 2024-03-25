package com.ardrawing.sketchtrace.core.domain.usecase

import com.ardrawing.sketchtrace.App
import com.ardrawing.sketchtrace.BuildConfig

/**
 * @author Ahmed Guedmioui
 */
class ShouldShowUpdateDialog {

    operator fun invoke(): Int {

        if (App.appData.isAppSuspended) {
            return 2
        }

        if (BuildConfig.VERSION_CODE < App.appData.appLatestVersion) {
            return 1
        }

        return 0
    }
}