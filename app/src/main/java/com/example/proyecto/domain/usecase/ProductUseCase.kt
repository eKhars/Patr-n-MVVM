package com.example.proyecto.domain.usecase

import com.example.proyecto.data.model.ProductData
import com.example.proyecto.data.repository.ProductRepository
import com.example.proyecto.domain.model.Result
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MultipartBody

class ProductUseCase(private val repository: ProductRepository) {

    suspend fun getProducts(token: String): Result<List<ProductData>> {
        if (token.isBlank()) {
            return Result.Error("Token de autenticación inválido")
        }
        return repository.getAllProducts(token)
    }
    suspend fun createProduct(
        token: String,
        name: String,
        quantity: Int,
        imagePart: MultipartBody.Part?
    ): Result<ProductData> = withContext(Dispatchers.Default) {
        if (token.isBlank()) {
            return@withContext Result.Error("Token de autenticación inválido")
        }
        if (name.isBlank()) {
            return@withContext Result.Error("El nombre del producto es requerido")
        }
        if (quantity < 0) {
            return@withContext Result.Error("La cantidad debe ser mayor o igual a 0")
        }

        repository.createProduct(token, name, quantity, imagePart)
    }

    suspend fun updateProduct(
        token: String,
        productId: String,
        name: String,
        quantity: Int,
        imagePart: MultipartBody.Part?
    ): Result<ProductData> = withContext(Dispatchers.Default) {
        if (token.isBlank()) {
            return@withContext Result.Error("Token de autenticación inválido")
        }
        if (productId.isBlank()) {
            return@withContext Result.Error("ID de producto inválido")
        }
        if (name.isBlank()) {
            return@withContext Result.Error("El nombre del producto es requerido")
        }
        if (quantity < 0) {
            return@withContext Result.Error("La cantidad debe ser mayor o igual a 0")
        }

        repository.updateProduct(token, productId, name, quantity, imagePart)
    }

    suspend fun deleteProduct(token: String, productId: String): Result<Unit> = withContext(Dispatchers.Default) {
        if (token.isBlank()) {
            return@withContext Result.Error("Token de autenticación inválido")
        }
        if (productId.isBlank()) {
            return@withContext Result.Error("ID de producto inválido")
        }
        repository.deleteProduct(token, productId)
    }
}