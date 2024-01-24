package com.ardrawing.sketchtrace.main.presentaion.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ardrawing.sketchtrace.image_list.domain.repository.ImageCategoriesRepository
import com.ardrawing.sketchtrace.splash.data.DataManager
import com.ardrawing.sketchtrace.splash.domain.repository.AppDataRepository
import com.ardrawing.sketchtrace.splash.presentation.splash.SplashUiEvent
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
class SettingsViewModel @Inject constructor(
    private val appDataRepository: AppDataRepository,
    private val imageCategoriesRepository: ImageCategoriesRepository
) : ViewModel() {

    private val _settingsState = MutableStateFlow(SettingsState())
    val settingsState = _settingsState.asStateFlow()

    fun onEvent(settingsUiEvent: SettingsUiEvent) {
        when (settingsUiEvent) {
            SettingsUiEvent.ShowHidePrivacyDialog -> {
                _settingsState.update {
                    it.copy(showPrivacyDialog = !settingsState.value.showPrivacyDialog)
                }
            }

            is SettingsUiEvent.Subscribe -> {

                if (settingsUiEvent.isSubscribed) {

                    settingsUiEvent.date?.let {
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


























