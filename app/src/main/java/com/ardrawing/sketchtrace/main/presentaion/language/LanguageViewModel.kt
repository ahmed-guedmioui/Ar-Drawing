package com.ardrawing.sketchtrace.main.presentaion.language

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

/**
 * @author Ahmed Guedmioui
 */
@HiltViewModel
class LanguageViewModel @Inject constructor() : ViewModel() {

    private val _languageState = MutableStateFlow(LanguageState())
    val languageState = _languageState.asStateFlow()

    fun onEvent(languageUiEvent: LanguageUiEvent) {
        when (languageUiEvent) {
            is LanguageUiEvent.ChangeLanguage -> {
                _languageState.update {
                    it.copy(language = languageUiEvent.language)
                }
            }
        }
    }
}


























