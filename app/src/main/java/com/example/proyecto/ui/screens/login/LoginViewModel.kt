package com.example.proyecto.ui.screens.login

import androidx.lifecycle.ViewModel
import com.example.proyecto.data.model.AuthData
import androidx.lifecycle.viewModelScope
import com.example.proyecto.domain.model.Result
import com.example.proyecto.domain.usecase.LoginUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class LoginUiState {
    object Initial : LoginUiState()
    object Loading : LoginUiState()
    data class Success(val authData: AuthData) : LoginUiState()
    data class Error(val message: String) : LoginUiState()
}

class LoginViewModel(
    private val loginUseCase: LoginUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<LoginUiState>(LoginUiState.Initial)
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    fun login(email: String, password: String) {
        viewModelScope.launch {
            _uiState.value = LoginUiState.Loading

            when (val result = loginUseCase.invoke(email, password)) {
                is Result.Success -> {
                    result.data.data?.let { authData ->
                        _uiState.value = LoginUiState.Success(authData)
                    } ?: run {
                        _uiState.value = LoginUiState.Error("Error en los datos de autenticación")
                    }
                }
                is Result.Error -> {
                    _uiState.value = LoginUiState.Error(result.message)
                }
                Result.Loading -> {
                    _uiState.value = LoginUiState.Loading
                }
            }
        }
    }

    fun resetState() {
        _uiState.value = LoginUiState.Initial
    }
}