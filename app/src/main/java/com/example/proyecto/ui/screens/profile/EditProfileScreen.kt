package com.example.proyecto.ui.screens.profile

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun EditProfileScreen(
    viewModel: EditProfileViewModel,
    userId: String,
    token: String,
    onUpdateSuccess: () -> Unit,
    onBackClick: () -> Unit
) {
    var firstName by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.loadProfile(userId, token)
    }

    LaunchedEffect(uiState) {
        when (uiState) {
            is EditProfileUiState.Loaded -> {
                val userData = (uiState as EditProfileUiState.Loaded).userData
                firstName = userData.firstName
                lastName = userData.lastName
            }
            is EditProfileUiState.UpdateSuccess -> {
                onUpdateSuccess()
                viewModel.resetState()
            }
            else -> {}
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Editar Perfil",
            style = MaterialTheme.typography.headlineMedium
        )

        when (uiState) {
            is EditProfileUiState.Loading -> {
                CircularProgressIndicator(
                    modifier = Modifier.padding(16.dp)
                )
            }
            is EditProfileUiState.Error -> {
                Text(
                    text = (uiState as EditProfileUiState.Error).message,
                    color = MaterialTheme.colorScheme.error,
                    textAlign = TextAlign.Center
                )
                Button(
                    onClick = { viewModel.loadProfile(userId, token) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Reintentar")
                }
            }
            else -> {
                OutlinedTextField(
                    value = firstName,
                    onValueChange = { firstName = it },
                    label = { Text("Nombre") },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = uiState !is EditProfileUiState.Loading
                )

                OutlinedTextField(
                    value = lastName,
                    onValueChange = { lastName = it },
                    label = { Text("Apellido") },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = uiState !is EditProfileUiState.Loading
                )

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        viewModel.updateProfile(userId, token, firstName, lastName)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = uiState !is EditProfileUiState.Loading
                ) {
                    if (uiState is EditProfileUiState.Loading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    } else {
                        Text("Actualizar Perfil")
                    }
                }

                Button(
                    onClick = onBackClick,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Volver")
                }
            }
        }
    }
}