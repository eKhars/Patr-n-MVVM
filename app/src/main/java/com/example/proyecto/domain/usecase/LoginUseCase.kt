package com.example.proyecto.domain.usecase

import com.example.proyecto.data.model.AuthResponse
import com.example.proyecto.data.repository.UserRepository
import com.example.proyecto.domain.model.Result

class LoginUseCase(private val repository: UserRepository) {
    suspend operator fun invoke(email: String, password: String): Result<AuthResponse> {
        if (email.isBlank() || password.isBlank()) {
            return Result.Error("Por favor complete todos los campos")
        }

        if (!isValidEmail(email)) {
            return Result.Error("Por favor ingrese un email válido")
        }

        return repository.login(email, password)
    }

    private fun isValidEmail(email: String): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }
}