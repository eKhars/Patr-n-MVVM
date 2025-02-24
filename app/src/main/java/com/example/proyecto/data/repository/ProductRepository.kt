package com.example.proyecto.data.repository

import com.example.proyecto.data.model.ProductData
import com.example.proyecto.data.remote.ApiClient
import com.example.proyecto.domain.model.Result
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.HttpException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

class ProductRepository {
    private val api = ApiClient.api

    suspend fun getAllProducts(token: String): Result<List<ProductData>> = withContext(Dispatchers.IO) {
        try {
            val response = api.getProducts("token=$token")
            if (response.isSuccessful) {
                response.body()?.let { products ->
                    Result.Success(products)
                } ?: Result.Error("Respuesta vacía del servidor")
            } else {
                handleHttpError(response.code())
            }
        } catch (e: Exception) {
            handleException(e)
        }
    }

    suspend fun createProduct(
        token: String,
        name: String,
        quantity: Int,
        imagePart: MultipartBody.Part?
    ): Result<ProductData> = withContext(Dispatchers.IO) {
        try {
            val namePart = name.toRequestBody("text/plain".toMediaTypeOrNull())
            val quantityPart = quantity.toString().toRequestBody("text/plain".toMediaTypeOrNull())

            val response = api.createProduct(
                token = "token=$token",
                name = namePart,
                quantity = quantityPart,
                image = imagePart
            )

            if (response.isSuccessful) {
                response.body()?.let {
                    Result.Success(it)
                } ?: Result.Error("Respuesta vacía del servidor")
            } else {
                handleHttpError(response.code())
            }
        } catch (e: Exception) {
            handleException(e)
        }
    }

    suspend fun updateProduct(
        token: String,
        productId: String,
        name: String,
        quantity: Int,
        imagePart: MultipartBody.Part?
    ): Result<ProductData> = withContext(Dispatchers.IO) {
        try {
            val response = api.updateProduct(
                id = productId,
                token = "token=$token",
                name = name.toRequestBody("text/plain".toMediaTypeOrNull()),
                quantity = quantity.toString().toRequestBody("text/plain".toMediaTypeOrNull()),
                image = imagePart
            )

            if (response.isSuccessful) {
                response.body()?.let {
                    Result.Success(it)
                } ?: Result.Error("Respuesta vacía del servidor")
            } else {
                handleHttpError(response.code())
            }
        } catch (e: Exception) {
            handleException(e)
        }
    }

    suspend fun deleteProduct(token: String, productId: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val response = api.deleteProduct(productId, "token=$token")
            if (response.isSuccessful) {
                Result.Success(Unit)
            } else {
                handleHttpError(response.code())
            }
        } catch (e: Exception) {
            handleException(e)
        }
    }

    private fun handleHttpError(code: Int): Result.Error {
        return when (code) {
            400 -> Result.Error("Datos inválidos")
            401 -> Result.Error("No autorizado")
            403 -> Result.Error("Acceso denegado")
            404 -> Result.Error("Producto no encontrado")
            409 -> Result.Error("Ya existe un producto con ese nombre")
            500 -> Result.Error("Error interno del servidor")
            else -> Result.Error("Error desconocido: $code")
        }
    }

    private fun handleException(e: Exception): Result.Error {
        return when (e) {
            is HttpException -> handleHttpError(e.code())
            is UnknownHostException -> Result.Error("No hay conexión a internet")
            is SocketTimeoutException -> Result.Error("Tiempo de espera agotado")
            else -> Result.Error("Error: ${e.message}")
        }
    }
}