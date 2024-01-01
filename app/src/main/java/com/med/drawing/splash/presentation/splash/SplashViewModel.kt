package com.med.drawing.splash.presentation.splash

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.med.drawing.splash.domain.repository.AppDataRepository
import com.med.drawing.image_list.domain.repository.ImageCategoriesRepository
import com.med.drawing.splash.domain.usecase.ShouldShowUpdateDialog
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


    private val _continueAppChannel = Channel<Boolean>()
    val continueAppChannel = _continueAppChannel.receiveAsFlow()

    private val _showUpdateDialogChannel = Channel<Boolean>()
    val showUpdateDialogChannel = _showUpdateDialogChannel.receiveAsFlow()

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

            SplashUiEvent.HideDialog -> {
                Log.d("tag_splash", "HideDialog")
                viewModelScope.launch {
                    Log.d("tag_splash", "_continueAppChannel")
                    _continueAppChannel.send(true)
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
                                _splashState.update {
                                    it.copy(
                                        updateDialogState = 1, isDialogShowing = true
                                    )
                                }
                                Log.d("tag_splash", "updateDialogState = 1")
                                Log.d("tag_splash", "_showUpdateDialogChannel")
                                _showUpdateDialogChannel.send(true)
                            }

                            2 -> {
                                _splashState.update {
                                    it.copy(
                                        updateDialogState = 2,
                                        isDialogShowing = true
                                    )
                                }
                                Log.d("tag_splash", "updateDialogState = 2")
                                Log.d("tag_splash", "_showUpdateDialogChannel")
                                _showUpdateDialogChannel.send(true)
                            }

                            0 -> {
                                _splashState.update {
                                    it.copy(updateDialogState = 0)
                                }


                                Log.d("tag_splash", "updateDialogState = 0")

                                if (splashState.value.areImagesLoaded) {
                                    Log.d(
                                        "tag_splash",
                                        "areImagesLoaded = true -> _continueAppChannel"
                                    )
                                    _continueAppChannel.send(true)
                                }
                            }
                        }

                        Log.d("tag_splash", "isAppDataLoaded = true")
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

                        Log.d("tag_splash", "areImagesLoaded = true")
                        _splashState.update {
                            it.copy(areImagesLoaded = true)
                        }

                        if (splashState.value.isAppDataLoaded
                            && splashState.value.updateDialogState == 0
                        ) {
                            Log.d(
                                "tag_splash",
                                "isAppDataLoaded = true, updateDialogState = 0 -> _continueAppChannel"
                            )
                            _continueAppChannel.send(true)
                        }
                    }
                }
            }
        }
    }
}


























