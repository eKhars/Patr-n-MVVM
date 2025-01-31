package com.example.proyecto.ui.screens.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.proyecto.data.model.UserData
import com.example.proyecto.domain.model.Result
import com.example.proyecto.domain.usecase.ProfileUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class EditProfileViewModel(
    private val profileUseCase: ProfileUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<EditProfileUiState>(EditProfileUiState.Initial)
    val uiState: StateFlow<EditProfileUiState> = _uiState.asStateFlow()

    fun loadProfile(userId: String, token: String) {
        viewModelScope.launch {
            _uiState.value = EditProfileUiState.Loading
            when (val result = profileUseCase.getProfile(userId, token)) {
                is Result.Success -> {
                    _uiState.value = EditProfileUiState.Loaded(result.data)
                }
                is Result.Error -> {
                    _uiState.value = EditProfileUiState.Error(result.message)
                }
                Result.Loading -> {
                    _uiState.value = EditProfileUiState.Loading
                }
            }
        }
    }

    fun updateProfile(userId: String, token: String, firstName: String, lastName: String) {
        viewModelScope.launch {
            _uiState.value = EditProfileUiState.Loading
            when (val result = profileUseCase.updateProfile(userId, token, firstName, lastName)) {
                is Result.Success -> {
                    _uiState.value = EditProfileUiState.UpdateSuccess(result.data)
                }
                is Result.Error -> {
                    _uiState.value = EditProfileUiState.Error(result.message)
                }
                Result.Loading -> {
                    _uiState.value = EditProfileUiState.Loading
                }
            }
        }
    }

    fun resetState() {
        _uiState.value = EditProfileUiState.Initial
    }
}

sealed class EditProfileUiState {
    object Initial : EditProfileUiState()
    object Loading : EditProfileUiState()
    data class Loaded(val userData: UserData) : EditProfileUiState()
    data class UpdateSuccess(val userData: UserData) : EditProfileUiState()
    data class Error(val message: String) : EditProfileUiState()
}