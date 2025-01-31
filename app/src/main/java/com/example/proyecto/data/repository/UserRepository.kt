package com.example.proyecto.data.repository

import com.example.proyecto.data.model.AuthResponse
import com.example.proyecto.data.model.RegisterUser
import com.example.proyecto.data.model.User
import com.example.proyecto.data.model.UserData
import com.example.proyecto.data.remote.ApiClient
import com.example.proyecto.domain.model.Result
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class UserRepository {
    private val api = ApiClient.api

    suspend fun login(email: String, password: String): Result<AuthResponse> = withContext(Dispatchers.IO) {
        try {
            val response = api.login(User(email, password))
            if (response.isSuccessful) {
                response.body()?.let {
                    Result.Success(it)
                } ?: Result.Error("Respuesta vacía del servidor")
            } else {
                when (response.code()) {
                    400 -> Result.Error("Credenciales inválidas")
                    401 -> Result.Error("No autorizado")
                    else -> Result.Error("Error: ${response.code()} - ${response.message()}")
                }
            }
        } catch (e: Exception) {
            Result.Error("Error de conexión: ${e.message}")
        }
    }

    suspend fun register(registerUser: RegisterUser): Result<AuthResponse> = withContext(Dispatchers.IO) {
        try {
            val response = api.register(registerUser)
            if (response.isSuccessful) {
                response.body()?.let {
                    Result.Success(it)
                } ?: Result.Error("Respuesta vacía del servidor")
            } else {
                Result.Error("Error: ${response.code()} - ${response.message()}")
            }
        } catch (e: Exception) {
            Result.Error("Error de conexión: ${e.message}")
        }
    }

    suspend fun getProfile(userId: String, token: String): Result<UserData> = withContext(Dispatchers.IO) {
        try {
            val response = api.getClientProfile(userId, "token=$token")
            if (response.isSuccessful) {
                response.body()?.let {
                    Result.Success(it)
                } ?: Result.Error("Respuesta vacía del servidor")
            } else {
                Result.Error("Error: ${response.code()} - ${response.message()}")
            }
        } catch (e: Exception) {
            Result.Error("Error de conexión: ${e.message}")
        }
    }

    suspend fun updateProfile(
        userId: String,
        token: String,
        firstName: String,
        lastName: String
    ): Result<UserData> = withContext(Dispatchers.IO) {
        try {
            val response = api.updateClientProfile(
                userId,
                "token=$token",
                mapOf(
                    "firstName" to firstName,
                    "lastName" to lastName
                )
            )
            if (response.isSuccessful) {
                response.body()?.let {
                    Result.Success(it)
                } ?: Result.Error("Respuesta vacía del servidor")
            } else {
                Result.Error("Error: ${response.code()} - ${response.message()}")
            }
        } catch (e: Exception) {
            Result.Error("Error de conexión: ${e.message}")
        }
    }
}