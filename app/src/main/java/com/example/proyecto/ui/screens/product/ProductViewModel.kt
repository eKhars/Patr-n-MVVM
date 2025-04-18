package com.example.proyecto.ui.screens.product

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.proyecto.data.model.ProductData
import com.example.proyecto.domain.model.Result
import com.example.proyecto.domain.usecase.ProductUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File

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

    fun createProduct(
        token: String,
        name: String,
        quantity: Int,
        imageUri: Uri?,
        context: Context
    ) {
        viewModelScope.launch {
            _uiState.value = ProductUiState.Loading
            try {
                // Procesar la imagen en un hilo de IO
                val imagePart = imageUri?.let { uri ->
                    withContext(Dispatchers.IO) {
                        val file = getFileFromUri(uri, context)
                        val requestBody = file.asRequestBody("image/*".toMediaTypeOrNull())
                        MultipartBody.Part.createFormData("image", file.name, requestBody)
                    }
                }

                when (val result = productUseCase.createProduct(token, name, quantity, imagePart)) {
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
                _uiState.value = ProductUiState.Error("Error al crear producto: ${e.message}")
            }
        }
    }

    fun updateProduct(
        token: String,
        productId: String,
        name: String,
        quantity: Int,
        imageUri: Uri?,
        context: Context
    ) {
        viewModelScope.launch {
            _uiState.value = ProductUiState.Loading
            try {
                // Procesar la imagen en un hilo de IO
                val imagePart = imageUri?.let { uri ->
                    withContext(Dispatchers.IO) {
                        val file = getFileFromUri(uri, context)
                        val requestBody = file.asRequestBody("image/*".toMediaTypeOrNull())
                        MultipartBody.Part.createFormData("image", file.name, requestBody)
                    }
                }

                when (val result = productUseCase.updateProduct(
                    token,
                    productId,
                    name,
                    quantity,
                    imagePart
                )) {
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
                _uiState.value = ProductUiState.Error("Error al actualizar producto: ${e.message}")
            }
        }
    }

    fun deleteProduct(token: String, productId: String) {
        viewModelScope.launch {
            _uiState.value = ProductUiState.Loading
            try {
                if (productId.isBlank()) {
                    _uiState.value = ProductUiState.Error("ID de producto inválido")
                    return@launch
                }

                when (val result = productUseCase.deleteProduct(token, productId)) {
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
                _uiState.value = ProductUiState.Error("Error al eliminar producto: ${e.message}")
            }
        }
    }

    private suspend fun getFileFromUri(uri: Uri, context: Context): File = withContext(Dispatchers.IO) {
        val inputStream = context.contentResolver.openInputStream(uri)
        val file = File(context.cacheDir, "temp_image_${System.currentTimeMillis()}.jpg")
        inputStream?.use { input ->
            file.outputStream().use { output ->
                input.copyTo(output)
            }
        }
        file
    }

    fun resetState() {
        _uiState.value = ProductUiState.Initial
    }
}