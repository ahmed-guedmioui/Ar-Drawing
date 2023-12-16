package com.med.drawing.image_list.presentation

/**
 * @author Ahmed Guedmioui
 */
sealed interface ImageListUiEvent {
    object TryAgain : ImageListUiEvent
}