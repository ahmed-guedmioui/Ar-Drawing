package com.med.drawing.my_creation.presentation.my_creation_details

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.med.drawing.my_creation.domian.repository.CreationRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
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
                    creationRepository.deleteCreation(
                        myCreationDetailsUiEvent.creationUri
                    )
                }
            }
        }
    }
}


























