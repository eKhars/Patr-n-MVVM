package com.example.proyecto.ui.screens.home

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.proyecto.utils.EncryptedPreferencesManager

@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    userId: String,
    token: String,
    onLogout: () -> Unit,
    onEditProfile: () -> Unit,
    onProductsClick: () -> Unit,
    onNotificationsClick: () -> Unit
) {
    val context = LocalContext.current
    val preferencesManager = remember { EncryptedPreferencesManager(context) }

    var userName by remember { mutableStateOf(preferencesManager.getUserName()) }

    LaunchedEffect(Unit) {
        viewModel.loadUserProfile(userId, token)
        preferencesManager.updateLastAccess()
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
                if (userName.isEmpty() && state.userData.firstName.isNotEmpty()) {
                    userName = "${state.userData.firstName} ${state.userData.lastName}"
                    preferencesManager.saveUserName(userName)
                }

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

                ElevatedCard(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Acciones",
                            style = MaterialTheme.typography.titleLarge
                        )

                        Button(
                            onClick = onEditProfile,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Editar Perfil")
                        }

                        Button(
                            onClick = onProductsClick,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Gestionar Productos")
                        }

                        Button(
                            onClick = onNotificationsClick,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Preferencias")
                        }
                    }
                }

                ElevatedCard(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Información de Uso",
                            style = MaterialTheme.typography.titleLarge
                        )

                        Text("Último acceso: ${preferencesManager.getLastAccess()}")

                        val totalUsageMillis = preferencesManager.getTotalUsageTime()
                        val hours = totalUsageMillis / (1000 * 60 * 60)
                        val minutes = (totalUsageMillis % (1000 * 60 * 60)) / (1000 * 60)
                        val seconds = (totalUsageMillis % (1000 * 60)) / 1000

                        Text("Tiempo total de uso: ${String.format("%02d:%02d:%02d", hours, minutes, seconds)}")
                    }
                }

                Spacer(modifier = Modifier.weight(1f))

                OutlinedButton(
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