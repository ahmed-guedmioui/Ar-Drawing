package com.med.drawing.splash.presentation.splash

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
class SplashViewModel @Inject constructor(
    private val appDataRepository: AppDataRepository,
    private val imageCategoriesRepository: ImageCategoriesRepository
) : ViewModel() {

    private val _splashState = MutableStateFlow(SplashState())
    val splashState = _splashState.asStateFlow()

    private val _areBothImagesAndDataLoadedChannel = Channel<Boolean>()
    val areBothImagesAndDataLoadedChannel = _areBothImagesAndDataLoadedChannel.receiveAsFlow()

    init {
        getData()
        getImages()
    }

    fun onEvent(splashUiEvent: SplashUiEvent) {
        when (splashUiEvent) {
            SplashUiEvent.TryAgain -> {
                getData()
                getImages()
            }
        }
    }

    private fun getData() {
        viewModelScope.launch {

            appDataRepository.getAppData().collect { appDataResult ->
                when (appDataResult) {
                    is Resource.Error -> {
                        _areBothImagesAndDataLoadedChannel.send(false)
                    }

                    is Resource.Loading -> {
                    }

                    is Resource.Success -> {
                        _splashState.update {
                            it.copy(isAppDataLoaded = true)
                        }
                        if (splashState.value.areImagesLoaded) {
                            imageCategoriesRepository.setGalleryAndCameraAndNativeItems()
                            _areBothImagesAndDataLoadedChannel.send(true)
                        }
                    }
                }
            }
        }
    }

    private fun getImages() {
        viewModelScope.launch {
            imageCategoriesRepository.getImageCategoryList().collect { imagesResult ->
                when (imagesResult) {
                    is Resource.Error -> {
                        _areBothImagesAndDataLoadedChannel.send(false)
                    }

                    is Resource.Loading -> {
                    }

                    is Resource.Success -> {
                        _splashState.update {
                            it.copy(areImagesLoaded = true)
                        }
                        if (splashState.value.isAppDataLoaded) {
                            imageCategoriesRepository.setGalleryAndCameraAndNativeItems()
                            _areBothImagesAndDataLoadedChannel.send(true)
                        }
                    }
                }
            }
        }
    }
}


























