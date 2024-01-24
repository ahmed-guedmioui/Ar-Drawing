package com.ardrawing.sketchtrace.main.presentaion.get_started

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ardrawing.sketchtrace.BuildConfig
import com.ardrawing.sketchtrace.image_list.domain.repository.ImageCategoriesRepository
import com.ardrawing.sketchtrace.splash.data.DataManager
import com.ardrawing.sketchtrace.splash.domain.repository.AppDataRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Date
import javax.inject.Inject

/**
 * @author Ahmed Guedmioui
 */
@HiltViewModel
class GetStartedViewModel @Inject constructor(
    private val appDataRepository: AppDataRepository,
    private val imageCategoriesRepository: ImageCategoriesRepository
) : ViewModel() {

    private val _getStartedState = MutableStateFlow(GetStartedState())
    val getsStartedState = _getStartedState.asStateFlow()

    fun onEvent(getStartedUiEvent: GetStartedUiEvent) {
        when (getStartedUiEvent) {
            GetStartedUiEvent.ShowHidePrivacyDialog -> {
                _getStartedState.update {
                    it.copy(showPrivacyDialog = !getsStartedState.value.showPrivacyDialog)
                }
            }

            is GetStartedUiEvent.Subscribe -> {

                if (getStartedUiEvent.isSubscribed) {

                    getStartedUiEvent.date?.let {
                        if (it.after(Date())) {
                            DataManager.appData.isSubscribed = true

                            viewModelScope.launch {
                                appDataRepository.setAdsVisibilityForUser()
                                imageCategoriesRepository.setUnlockedImages(it)
                                imageCategoriesRepository.setNativeItems(it)
                            }
                        }
                    }

                } else {
                    viewModelScope.launch {
                        appDataRepository.setAdsVisibilityForUser()
                    }
                }
            }
        }
    }
}


























