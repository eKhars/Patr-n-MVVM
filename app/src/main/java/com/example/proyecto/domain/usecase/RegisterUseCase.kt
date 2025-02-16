package com.example.proyecto.domain.usecase

import com.example.proyecto.data.model.AuthResponse
import com.example.proyecto.data.model.RegisterUser
import com.example.proyecto.data.repository.UserRepository
import com.example.proyecto.domain.model.Result

class RegisterUseCase(private val repository: UserRepository) {
    suspend operator fun invoke(
        firstName: String,
        lastName: String,
        email: String,
        password: String,
        confirmPassword: String
    ): Result<AuthResponse> {
        when {
            firstName.isBlank() || lastName.isBlank() || email.isBlank() ||
                    password.isBlank() || confirmPassword.isBlank() -> {
                return Result.Error("Por favor complete todos los campos")
            }
            !isValidEmail(email) -> {
                return Result.Error("Por favor ingrese un email válido")
            }
            password != confirmPassword -> {
                return Result.Error("Las contraseñas no coinciden")
            }
            password.length < 6 -> {
                return Result.Error("La contraseña debe tener al menos 6 caracteres")
            }
        }

        val registerUser = RegisterUser(
            firstName = firstName,
            lastName = lastName,
            email = email,
            password = password
        )

        return repository.register(registerUser)
    }

    private fun isValidEmail(email: String): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }
}