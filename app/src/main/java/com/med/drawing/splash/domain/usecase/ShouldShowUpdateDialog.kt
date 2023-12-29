package com.med.drawing.splash.domain.usecase

import android.os.Build
import com.med.drawing.splash.data.DataManager

/**
 * @author Ahmed Guedmioui
 */
class ShouldShowUpdateDialog {

    operator fun invoke(): Boolean {
        if (Build.VERSION.SDK_INT < DataManager.appData.appLatestVersion) {
            return true
        }

        return false
    }
}