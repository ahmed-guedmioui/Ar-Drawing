package com.med.drawing.my_creation.presentation.my_creation_details

import androidx.lifecycle.SavedStateHandle
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
class MyCreationDetailsViewModel @Inject constructor(
    private val creationRepository: CreationRepository
) : ViewModel() {

    fun onEvent(myCreationDetailsUiEvent: MyCreationDetailsUiEvent) {
        when (myCreationDetailsUiEvent) {
            is MyCreationDetailsUiEvent.DeleteCreation -> {
                viewModelScope.launch {
                    creationRepository.deleteCreation(myCreationDetailsUiEvent.creationUri)
                }
            }
        }
    }
}


























