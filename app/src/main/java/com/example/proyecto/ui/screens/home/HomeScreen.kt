package com.example.proyecto.ui.screens.home

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    userId: String,
    token: String,
    onLogout: () -> Unit,
    onEditProfile: () -> Unit
) {
    LaunchedEffect(Unit) {
        viewModel.loadUserProfile(userId, token)
    }

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        when (val state = uiState) {
            is HomeUiState.Loading -> {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
            }
            is HomeUiState.Success -> {
                Text(
                    text = "¡Bienvenido, ${state.userData.firstName}!",
                    style = MaterialTheme.typography.headlineMedium,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = state.userData.email,
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = onEditProfile,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Editar Perfil")
                }

                Button(
                    onClick = {
                        viewModel.logout()
                        onLogout()
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Cerrar sesión")
                }
            }
            is HomeUiState.Error -> {
                Text(
                    text = state.message,
                    color = MaterialTheme.colorScheme.error,
                    textAlign = TextAlign.Center
                )
                Button(
                    onClick = { viewModel.loadUserProfile(userId, token) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Reintentar")
                }
            }
            is HomeUiState.Initial -> {
            }
        }
    }
}