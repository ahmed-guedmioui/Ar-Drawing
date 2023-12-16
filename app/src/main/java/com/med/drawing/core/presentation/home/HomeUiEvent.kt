package com.med.drawing.core.presentation.home

import com.med.drawing.core.presentation.settings.SettingsUiEvent

/**
 * @author Ahmed Guedmioui
 */
sealed interface HomeUiEvent {
    object BackPressed : HomeUiEvent
    object ShowHideHelperDialog : HomeUiEvent
}