package com.med.drawing.advanced_editing.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.med.drawing.splash.domain.repository.AppDataRepository
import com.med.drawing.image_list.domain.repository.ImageCategoriesRepository
import com.med.drawing.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * @author Ahmed Guedmioui
 */
@HiltViewModel
class AdvancedEditingViewModel @Inject constructor() : ViewModel() {

    private val _advancedEditingState = MutableStateFlow(AdvancedEditingState())
    val advancedEditingState = _advancedEditingState.asStateFlow()

    fun onEvent(advancedEditingUiEvent: AdvancedEditingUiEvent) {
        when (advancedEditingUiEvent) {
            is AdvancedEditingUiEvent.Select -> {

                if (advancedEditingState.value.selected == advancedEditingUiEvent.selected) {
                    _advancedEditingState.update {
                        it.copy(selected = 0)
                    }
                } else {
                    _advancedEditingState.update {
                        it.copy(
                            selected = advancedEditingUiEvent.selected
                        )
                    }
                }
            }

            is AdvancedEditingUiEvent.SetContrast -> {
                _advancedEditingState.update {
                    it.copy(
                        contrast = advancedEditingUiEvent.contrast
                    )
                }
            }

            is AdvancedEditingUiEvent.SetEdge -> {
                _advancedEditingState.update {
                    it.copy(
                        edge = advancedEditingUiEvent.edge
                    )
                }
            }

            is AdvancedEditingUiEvent.SetNoise -> {
                _advancedEditingState.update {
                    it.copy(
                        noise = advancedEditingUiEvent.noise
                    )
                }
            }

            is AdvancedEditingUiEvent.SetSharpness -> {
                _advancedEditingState.update {
                    it.copy(
                        sharpness = advancedEditingUiEvent.sharpness
                    )
                }
            }
        }
    }
}


























