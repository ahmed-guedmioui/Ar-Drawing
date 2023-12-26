package com.med.drawing.my_creation.presentation.my_creation_details

/**
 * @author Ahmed Guedmioui
 */
sealed interface MyCreationDetailsUiEvent {
    data class DeleteCreation(
        val creationUri: String
    ) : MyCreationDetailsUiEvent
}