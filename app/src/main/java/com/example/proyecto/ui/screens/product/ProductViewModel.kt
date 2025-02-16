package com.example.proyecto.ui.screens.product

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.proyecto.data.model.ProductData
import com.example.proyecto.domain.model.Result
import com.example.proyecto.domain.usecase.ProductUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class ProductUiState {
    object Initial : ProductUiState()
    object Loading : ProductUiState()
    data class Success(val products: List<ProductData>) : ProductUiState()
    data class Error(val message: String) : ProductUiState()
}

class ProductViewModel(
    private val productUseCase: ProductUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<ProductUiState>(ProductUiState.Initial)
    val uiState: StateFlow<ProductUiState> = _uiState.asStateFlow()

    fun loadProducts(token: String) {
        viewModelScope.launch {
            _uiState.value = ProductUiState.Loading
            try {
                when (val result = productUseCase.getProducts(token)) {
                    is Result.Success -> {
                        _uiState.value = ProductUiState.Success(result.data)
                    }
                    is Result.Error -> {
                        _uiState.value = ProductUiState.Error(result.message)
                    }
                    Result.Loading -> {
                        _uiState.value = ProductUiState.Loading
                    }
                }
            } catch (e: Exception) {
                _uiState.value = ProductUiState.Error("Error al cargar productos: ${e.message}")
            }
        }
    }

    fun createProduct(token: String, name: String, quantity: Int) {
        viewModelScope.launch {
            _uiState.value = ProductUiState.Loading
            try {
                when (val result = productUseCase.createProduct(token, name, quantity)) {
                    is Result.Success -> {
                        loadProducts(token) // Recargar la lista después de crear
                    }
                    is Result.Error -> {
                        _uiState.value = ProductUiState.Error(result.message)
                    }
                    Result.Loading -> {
                        _uiState.value = ProductUiState.Loading
                    }
                }
            } catch (e: Exception) {
                _uiState.value = ProductUiState.Error("Error al crear producto: ${e.message}")
            }
        }
    }

    fun updateProduct(token: String, productId: String, name: String, quantity: Int) {
        viewModelScope.launch {
            try {
                _uiState.value = ProductUiState.Loading
                when (val result = productUseCase.updateProduct(token, productId, name, quantity)) {
                    is Result.Success -> {
                        loadProducts(token)
                    }
                    is Result.Error -> {
                        _uiState.value = ProductUiState.Error(result.message)
                    }
                    Result.Loading -> {
                        _uiState.value = ProductUiState.Loading
                    }
                }
            } catch (e: Exception) {
                _uiState.value = ProductUiState.Error("Error al actualizar: ${e.message}")
            }
        }
    }

    fun deleteProduct(token: String, productId: String) {
        viewModelScope.launch {
            _uiState.value = ProductUiState.Loading
            try {
                when (val result = productUseCase.deleteProduct(token, productId)) {
                    is Result.Success -> {
                        loadProducts(token) // Recargar la lista después de eliminar
                    }
                    is Result.Error -> {
                        _uiState.value = ProductUiState.Error(result.message)
                    }
                    Result.Loading -> {
                        _uiState.value = ProductUiState.Loading
                    }
                }
            } catch (e: Exception) {
                _uiState.value = ProductUiState.Error("Error al eliminar producto: ${e.message}")
            }
        }
    }

    fun resetState() {
        _uiState.value = ProductUiState.Initial
    }
}
