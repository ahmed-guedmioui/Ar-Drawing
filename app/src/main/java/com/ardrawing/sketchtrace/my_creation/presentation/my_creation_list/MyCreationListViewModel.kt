package com.ardrawing.sketchtrace.my_creation.presentation.my_creation_list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ardrawing.sketchtrace.my_creation.domian.repository.CreationRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * @author Ahmed Guedmioui
 */
@HiltViewModel
class MyCreationListViewModel @Inject constructor(
    private val creationRepository: CreationRepository
) : ViewModel() {

    private val _myCreationListState = MutableStateFlow(MyCreationListState())
    val myCreationState = _myCreationListState.asStateFlow()

    init {
        getCreationList()
    }

    private fun getCreationList() {
        viewModelScope.launch {
            creationRepository.getCreationList().collect { creationList ->
                _myCreationListState.update {
                    it.copy(creationList = creationList)
                }
            }
        }
    }
}


























