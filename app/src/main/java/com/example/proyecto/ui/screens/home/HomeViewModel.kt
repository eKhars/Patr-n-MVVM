package com.example.proyecto.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.proyecto.data.model.UserData
import com.example.proyecto.domain.usecase.ProfileUseCase
import com.example.proyecto.domain.model.Result
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class HomeViewModel(
    private val profileUseCase: ProfileUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<HomeUiState>(HomeUiState.Initial)
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    fun loadUserProfile(userId: String, token: String) {
        viewModelScope.launch {
            _uiState.value = HomeUiState.Loading
            when (val result = profileUseCase.getProfile(userId, token)) {
                is Result.Success -> {
                    _uiState.value = HomeUiState.Success(result.data)
                }
                is Result.Error -> {
                    _uiState.value = HomeUiState.Error(result.message)
                }
                is Result.Loading -> {
                    _uiState.value = HomeUiState.Loading
                }
            }
        }
    }

    fun logout() {
        _uiState.value = HomeUiState.Initial
    }
}

sealed class HomeUiState {
    object Initial : HomeUiState()
    object Loading : HomeUiState()
    data class Success(val userData: UserData) : HomeUiState()
    data class Error(val message: String) : HomeUiState()
}