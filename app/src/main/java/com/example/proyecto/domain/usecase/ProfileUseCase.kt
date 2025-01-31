package com.example.proyecto.domain.usecase

import com.example.proyecto.data.model.UserData
import com.example.proyecto.data.repository.UserRepository
import com.example.proyecto.domain.model.Result

class ProfileUseCase(private val repository: UserRepository) {
    suspend fun getProfile(userId: String, token: String): Result<UserData> {
        if (userId.isBlank() || token.isBlank()) {
            return Result.Error("Credenciales de autenticación inválidas")
        }
        return repository.getProfile(userId, token)
    }

    suspend fun updateProfile(
        userId: String,
        token: String,
        firstName: String,
        lastName: String
    ): Result<UserData> {
        when {
            userId.isBlank() || token.isBlank() -> {
                return Result.Error("Credenciales de autenticación inválidas")
            }
            firstName.isBlank() || lastName.isBlank() -> {
                return Result.Error("Por favor complete todos los campos")
            }
            firstName.length < 2 -> {
                return Result.Error("El nombre debe tener al menos 2 caracteres")
            }
            lastName.length < 2 -> {
                return Result.Error("El apellido debe tener al menos 2 caracteres")
            }
        }

        return repository.updateProfile(userId, token, firstName, lastName)
    }
}