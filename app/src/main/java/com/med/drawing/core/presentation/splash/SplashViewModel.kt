package com.med.drawing.core.presentation.splash

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.med.drawing.core.domain.repository.AppDataRepository
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
    private val appDataRepository: AppDataRepository
) : ViewModel() {

    private val _splashState = MutableStateFlow(SplashState())
    val splashState = _splashState.asStateFlow()



    private val _resourceChannel = Channel<Resource<Unit>>()
    val appDataResultChannel = _resourceChannel.receiveAsFlow()

    init {
       getData()
    }

    fun onEvent(splashUiEvent: SplashUiEvent) {
        when (splashUiEvent) {
            SplashUiEvent.TryAgain -> {
                getData()
            }
        }
    }

    private fun getData() {
        viewModelScope.launch {
            _splashState.update {
                it.copy(isLoading = true)
            }

            appDataRepository.getAppData().collect {
                _resourceChannel.send(it)
            }

            _splashState.update {
                it.copy(isLoading = false)
            }
        }
    }
}


























