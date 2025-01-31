package com.example.proyecto.ui.screens.register

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.proyecto.domain.model.Result
import com.example.proyecto.domain.usecase.RegisterUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class RegisterViewModel(
    private val registerUseCase: RegisterUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<RegisterUiState>(RegisterUiState.Initial)
    val uiState: StateFlow<RegisterUiState> = _uiState.asStateFlow()

    fun register(
        firstName: String,
        lastName: String,
        email: String,
        password: String,
        confirmPassword: String
    ) {
        viewModelScope.launch {
            _uiState.value = RegisterUiState.Loading

            when (val result = registerUseCase(firstName, lastName, email, password, confirmPassword)) {
                is Result.Success -> {
                    _uiState.value = RegisterUiState.Success
                }
                is Result.Error -> {
                    _uiState.value = RegisterUiState.Error(result.message)
                }
                Result.Loading -> {
                    _uiState.value = RegisterUiState.Loading
                }
            }
        }
    }

    fun resetState() {
        _uiState.value = RegisterUiState.Initial
    }
}

sealed class RegisterUiState {
    object Initial : RegisterUiState()
    object Loading : RegisterUiState()
    object Success : RegisterUiState()
    data class Error(val message: String) : RegisterUiState()
}