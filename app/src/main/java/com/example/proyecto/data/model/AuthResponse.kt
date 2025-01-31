package com.example.proyecto.data.model

data class AuthResponse(
    val message: String?,
    val data: AuthData?
)

data class AuthData(
    val token: String,
    val user: UserData
)