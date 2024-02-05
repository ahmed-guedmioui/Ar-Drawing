package com.ardrawing.sketchtrace.splash.presentation.splash

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ardrawing.sketchtrace.image_list.domain.repository.ImageCategoriesRepository
import com.ardrawing.sketchtrace.splash.domain.repository.AppDataRepository
import com.ardrawing.sketchtrace.splash.domain.usecase.ShouldShowUpdateDialog
import com.ardrawing.sketchtrace.util.Resource
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


    private val _updateDialogState = MutableStateFlow(-1)
    val updateDialogState = _updateDialogState.asStateFlow()

    private val _continueAppChannel = Channel<Boolean>()
    val continueAppChannel = _continueAppChannel.receiveAsFlow()

    private val _showErrorToastChannel = Channel<Boolean>()
    val showErrorToastChannel = _showErrorToastChannel.receiveAsFlow()

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

            is SplashUiEvent.ContinueApp -> {
                viewModelScope.launch {
                    _continueAppChannel.send(true)
                }
            }

            SplashUiEvent.AlreadySubscribed -> {
                viewModelScope.launch {
                    appDataRepository.setAdsVisibilityForUser()
                    imageCategoriesRepository.setUnlockedImages()
                    imageCategoriesRepository.setNativeItems()
                }
            }
        }
    }

    private fun getData() {
        viewModelScope.launch {

            appDataRepository.getAppData().collect { appDataResult ->
                when (appDataResult) {
                    is Resource.Error -> {
                        _showErrorToastChannel.send(true)
                    }

                    is Resource.Loading -> {
                    }

                    is Resource.Success -> {

                        when (ShouldShowUpdateDialog().invoke()) {
                            1 -> {
                                _updateDialogState.update { 1 }
                            }

                            2 -> {
                                _updateDialogState.update { 2 }
                            }

                            0 -> {
                                _updateDialogState.update { 0 }

                                if (splashState.value.areImagesLoaded) {

                                    appDataRepository.setAdsVisibilityForUser()
                                    imageCategoriesRepository.setUnlockedImages()
                                    imageCategoriesRepository.setNativeItems()
                                    imageCategoriesRepository.setGalleryAndCameraItems()

                                    _continueAppChannel.send(true)
                                }
                            }
                        }

                        _splashState.update {
                            it.copy(isAppDataLoaded = true)
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
                        _showErrorToastChannel.send(true)
                    }

                    is Resource.Loading -> {
                    }

                    is Resource.Success -> {
                        _splashState.update {
                            it.copy(areImagesLoaded = true)
                        }

                        if (splashState.value.isAppDataLoaded && updateDialogState.value == 0) {

                            appDataRepository.setAdsVisibilityForUser()
                            imageCategoriesRepository.setUnlockedImages()
                            imageCategoriesRepository.setNativeItems()
                            imageCategoriesRepository.setGalleryAndCameraItems()

                            _continueAppChannel.send(true)
                        }

                    }
                }
            }
        }
    }
}


























