package com.ardrawing.sketchtrace.core.presentation.language

/**
 * @author Ahmed Guedmioui
 */
sealed interface LanguageUiEvent {
    data class ChangeLanguage(val language: String) : LanguageUiEvent
}