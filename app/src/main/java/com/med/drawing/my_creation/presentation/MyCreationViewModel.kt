package com.med.drawing.my_creation.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.med.drawing.my_creation.domian.repository.CreationRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * @author Ahmed Guedmioui
 */
@HiltViewModel
class MyCreationViewModel @Inject constructor(
    creationRepository: CreationRepository
) : ViewModel() {

    private val _myCreationState = MutableStateFlow(MyCreationState())
    val myCreationState = _myCreationState.asStateFlow()

    init {
        viewModelScope.launch {
            creationRepository.getCreationList().collect { creationList ->
                _myCreationState.update {
                    it.copy(creationList = creationList)
                }
            }
        }
    }
}


























