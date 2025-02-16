package com.example.proyecto.domain.usecase

import com.example.proyecto.data.model.ProductData
import com.example.proyecto.data.repository.ProductRepository
import com.example.proyecto.domain.model.Result

class ProductUseCase(private val repository: ProductRepository) {
    suspend fun getProducts(token: String): Result<List<ProductData>> {
        return repository.getAllProducts(token)
    }

    suspend fun createProduct(token: String, name: String, quantity: Int): Result<ProductData> {
        if (name.isBlank()) {
            return Result.Error("El nombre del producto es requerido")
        }
        if (quantity < 0) {
            return Result.Error("La cantidad debe ser mayor o igual a 0")
        }
        return repository.createProduct(token, name, quantity)
    }

    suspend fun updateProduct(token: String, productId: String, name: String, quantity: Int): Result<ProductData> {
        if (name.isBlank()) {
            return Result.Error("El nombre del producto es requerido")
        }
        if (quantity < 0) {
            return Result.Error("La cantidad debe ser mayor o igual a 0")
        }
        return repository.updateProduct(token, productId, name, quantity)
    }

    suspend fun deleteProduct(token: String, productId: String): Result<Unit> {
        if (productId.isBlank()) {
            return Result.Error("ID de producto inválido")
        }
        return repository.deleteProduct(token, productId)
    }
}