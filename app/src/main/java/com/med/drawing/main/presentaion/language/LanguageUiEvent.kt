package com.med.drawing.main.presentaion.language

/**
 * @author Ahmed Guedmioui
 */
sealed interface LanguageUiEvent {
    data class ChangeLanguage(val language: String) : LanguageUiEvent
}