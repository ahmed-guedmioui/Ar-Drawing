package com.ardrawing.sketchtrace.paywall.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ardrawing.sketchtrace.image_list.domain.repository.ImageCategoriesRepository
import com.ardrawing.sketchtrace.splash.data.DataManager
import com.ardrawing.sketchtrace.splash.domain.repository.AppDataRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.util.Date
import javax.inject.Inject

/**
 * @author Ahmed Guedmioui
 */
@HiltViewModel
class PaywallViewModel @Inject constructor(
    private val appDataRepository: AppDataRepository,
    private val imageCategoriesRepository: ImageCategoriesRepository
) : ViewModel() {


    fun onEvent(paywallUiEvent: PaywallUiEvent) {
        when (paywallUiEvent) {

            is PaywallUiEvent.Subscribe -> {

                if (paywallUiEvent.isSubscribed) {

                    paywallUiEvent.date?.let {
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


























