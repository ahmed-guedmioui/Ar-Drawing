package com.med.drawing.main.presentaion.home

import com.med.drawing.main.presentaion.settings.SettingsUiEvent

/**
 * @author Ahmed Guedmioui
 */
sealed interface HomeUiEvent {
    object BackPressed : HomeUiEvent
    object ShowHideHelperDialog : HomeUiEvent
}