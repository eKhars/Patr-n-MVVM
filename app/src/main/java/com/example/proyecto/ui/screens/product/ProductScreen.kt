package com.example.proyecto.ui.screens.product

import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.proyecto.data.model.ProductData
import com.example.proyecto.data.remote.ApiClient
import com.example.proyecto.ui.components.CameraView

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductScreen(
    viewModel: ProductViewModel,
    token: String,
    onBackClick: () -> Unit
) {
    var showAddDialog by remember { mutableStateOf(false) }
    var selectedProduct by remember { mutableStateOf<ProductData?>(null) }
    val context = LocalContext.current

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.loadProducts(token)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Productos") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Volver")
                    }
                },
                actions = {
                    Button(onClick = { showAddDialog = true }) {
                        Text("Agregar")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            when (val state = uiState) {
                is ProductUiState.Loading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
                is ProductUiState.Success -> {
                    if (state.products.isEmpty()) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "No hay productos disponibles",
                                style = MaterialTheme.typography.bodyLarge,
                                textAlign = TextAlign.Center
                            )
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(state.products) { product ->
                                ProductItem(
                                    product = product,
                                    onEdit = {
                                        selectedProduct = product
                                    },
                                    onDelete = {
                                        viewModel.deleteProduct(token, product.id)
                                    }
                                )
                            }
                        }
                    }
                }
                is ProductUiState.Error -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = state.message,
                                color = MaterialTheme.colorScheme.error,
                                textAlign = TextAlign.Center
                            )
                            Button(onClick = { viewModel.loadProducts(token) }) {
                                Text("Reintentar")
                            }
                        }
                    }
                }
                else -> {}
            }
        }
    }

    if (showAddDialog) {
        AddProductDialog(
            onDismiss = { showAddDialog = false },
            onConfirm = { name, quantity, imageUri ->
                viewModel.createProduct(token, name, quantity, imageUri, context)
                showAddDialog = false
            }
        )
    }

    selectedProduct?.let { product ->
        EditProductDialog(
            product = product,
            onDismiss = { selectedProduct = null },
            onConfirm = { name, quantity, imageUri ->
                viewModel.updateProduct(token, product.id, name, quantity, imageUri, context)
                selectedProduct = null
            }
        )
    }
}

@Composable
fun ProductItem(
    product: ProductData,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Mostrar imagen del producto si existe
            if (!product.imagePath.isNullOrEmpty()) {
                AsyncImage(
                    model = "${ApiClient.BASE_URL}${product.imagePath}",
                    contentDescription = "Imagen del producto",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    contentScale = ContentScale.Fit
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = product.name,
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = "Cantidad: ${product.quantity}",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    TextButton(onClick = onEdit) {
                        Text("Editar")
                    }
                    TextButton(
                        onClick = onDelete,
                        colors = ButtonDefaults.textButtonColors(
                            contentColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Text("Eliminar")
                    }
                }
            }
        }
    }
}

@Composable
fun AddProductDialog(
    onDismiss: () -> Unit,
    onConfirm: (name: String, quantity: Int, imageUri: Uri?) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var quantity by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }
    var showCamera by remember { mutableStateOf(false) }
    var imageUri by remember { mutableStateOf<Uri?>(null) }

    if (showCamera) {
        CameraView(
            onImageCaptured = { uri ->
                imageUri = uri
                showCamera = false
            },
            onError = {
                error = "Error al capturar la imagen"
                showCamera = false
            }
        )
    } else {
        AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text("Agregar Producto") },
            text = {
                Column {
                    OutlinedTextField(
                        value = name,
                        onValueChange = {
                            name = it
                            error = null
                        },
                        label = { Text("Nombre") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = quantity,
                        onValueChange = {
                            quantity = it.filter { char -> char.isDigit() }
                            error = null
                        },
                        label = { Text("Cantidad") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Previsualización de la imagen y botón para tomar foto
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        if (imageUri != null) {
                            AsyncImage(
                                model = imageUri,
                                contentDescription = "Imagen del producto",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Fit
                            )
                        }

                        Button(
                            onClick = { showCamera = true },
                            modifier = Modifier.align(Alignment.Center)
                        ) {
                            Text(if (imageUri == null) "Tomar Foto" else "Cambiar Foto")
                        }
                    }

                    error?.let {
                        Text(
                            text = it,
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        when {
                            name.isBlank() -> error = "El nombre es requerido"
                            quantity.isBlank() -> error = "La cantidad es requerida"
                            else -> {
                                val quantityInt = quantity.toIntOrNull()
                                when {
                                    quantityInt == null -> error = "La cantidad debe ser un número válido"
                                    quantityInt < 0 -> error = "La cantidad debe ser mayor o igual a 0"
                                    else -> onConfirm(name, quantityInt, imageUri)
                                }
                            }
                        }
                    }
                ) {
                    Text("Agregar")
                }
            },
            dismissButton = {
                TextButton(onClick = onDismiss) {
                    Text("Cancelar")
                }
            }
        )
    }
}

@Composable
fun EditProductDialog(
    product: ProductData,
    onDismiss: () -> Unit,
    onConfirm: (name: String, quantity: Int, imageUri: Uri?) -> Unit
) {
    var name by remember { mutableStateOf(product.name) }
    var quantity by remember { mutableStateOf(product.quantity.toString()) }
    var error by remember { mutableStateOf<String?>(null) }
    var showCamera by remember { mutableStateOf(false) }
    var imageUri by remember { mutableStateOf<Uri?>(null) }

    if (showCamera) {
        CameraView(
            onImageCaptured = { uri ->
                imageUri = uri
                showCamera = false
            },
            onError = {
                error = "Error al capturar la imagen"
                showCamera = false
            }
        )
    } else {
        AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text("Editar Producto") },
            text = {
                Column {
                    OutlinedTextField(
                        value = name,
                        onValueChange = {
                            name = it
                            error = null
                        },
                        label = { Text("Nombre") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = quantity,
                        onValueChange = {
                            if (it.isEmpty() || it.all { char -> char.isDigit() }) {
                                quantity = it
                                error = null
                            }
                        },
                        label = { Text("Cantidad") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Previsualización de la imagen actual o nueva
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        when {
                            imageUri != null -> {
                                AsyncImage(
                                    model = imageUri,
                                    contentDescription = "Nueva imagen del producto",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Fit
                                )
                            }
                            !product.imagePath.isNullOrEmpty() -> {
                                AsyncImage(
                                    model = "${ApiClient.BASE_URL}${product.imagePath}",
                                    contentDescription = "Imagen actual del producto",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Fit
                                )
                            }
                        }

                        Button(
                            onClick = { showCamera = true },
                            modifier = Modifier.align(Alignment.Center)
                        ) {
                            Text(if (imageUri == null && product.imagePath.isNullOrEmpty())
                                "Tomar Foto" else "Cambiar Foto")
                        }
                    }

                    error?.let {
                        Text(
                            text = it,
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        when {
                            name.isBlank() -> error = "El nombre es requerido"
                            quantity.isBlank() -> error = "La cantidad es requerida"
                            else -> {
                                try {
                                    val quantityInt = quantity.toInt()
                                    if (quantityInt >= 0) {
                                        onConfirm(name, quantityInt, imageUri)
                                    } else {
                                        error = "La cantidad debe ser mayor o igual a 0"
                                    }
                                } catch (e: NumberFormatException) {
                                    error = "Cantidad inválida"
                                }
                            }
                        }
                    }
                ) {
                    Text("Guardar")
                }
            },
            dismissButton = {
                TextButton(onClick = onDismiss) {
                    Text("Cancelar")
                }
            }
        )
    }
}