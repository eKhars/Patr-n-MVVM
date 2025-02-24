package com.example.proyecto.ui.screens.notifications

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.proyecto.utils.FCMTopicManager
import com.example.proyecto.utils.rememberNotificationPermissionState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationPreferencesScreen(
    onBackClick: () -> Unit
) {
    val hasNotificationPermission = rememberNotificationPermissionState()

    var generalNotifications by remember { mutableStateOf(true) }
    var promotionNotifications by remember { mutableStateOf(false) }
    var updateNotifications by remember { mutableStateOf(true) }
    var productNotifications by remember { mutableStateOf(false) }

    var showSnackbar by remember { mutableStateOf(false) }
    var snackbarMessage by remember { mutableStateOf("") }
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(showSnackbar) {
        if (showSnackbar) {
            snackbarHostState.showSnackbar(
                message = snackbarMessage,
                actionLabel = "OK",
                duration = SnackbarDuration.Short
            )
            showSnackbar = false
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Preferencias de Notificaciones") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Volver"
                        )
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (!hasNotificationPermission.value) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            "Se requiere permiso para enviar notificaciones",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = {
                                // Esto normalmente abriría la configuración del sistema
                                // pero por simplicidad solo mostramos un mensaje
                                snackbarMessage = "Debes habilitar los permisos en la configuración del sistema"
                                showSnackbar = true
                            }
                        ) {
                            Text("Habilitar permisos")
                        }
                    }
                }
            }

            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        "Tipos de notificaciones",
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                "Notificaciones generales",
                                style = MaterialTheme.typography.bodyLarge
                            )
                            Text(
                                "Información importante y anuncios",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = generalNotifications,
                            onCheckedChange = { isChecked ->
                                generalNotifications = isChecked
                                if (isChecked) {
                                    FCMTopicManager.subscribeTopic(FCMTopicManager.Topics.GENERAL)
                                } else {
                                    FCMTopicManager.unsubscribeTopic(FCMTopicManager.Topics.GENERAL)
                                }
                            },
                            enabled = hasNotificationPermission.value
                        )
                    }

                    Divider()

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                "Notificaciones de promociones",
                                style = MaterialTheme.typography.bodyLarge
                            )
                            Text(
                                "Ofertas especiales y descuentos",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = promotionNotifications,
                            onCheckedChange = { isChecked ->
                                promotionNotifications = isChecked
                                if (isChecked) {
                                    FCMTopicManager.subscribeTopic(FCMTopicManager.Topics.PROMOTIONS)
                                } else {
                                    FCMTopicManager.unsubscribeTopic(FCMTopicManager.Topics.PROMOTIONS)
                                }
                            },
                            enabled = hasNotificationPermission.value
                        )
                    }

                    Divider()

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                "Notificaciones de actualizaciones",
                                style = MaterialTheme.typography.bodyLarge
                            )
                            Text(
                                "Nuevas características y mejoras",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = updateNotifications,
                            onCheckedChange = { isChecked ->
                                updateNotifications = isChecked
                                if (isChecked) {
                                    FCMTopicManager.subscribeTopic(FCMTopicManager.Topics.UPDATES)
                                } else {
                                    FCMTopicManager.unsubscribeTopic(FCMTopicManager.Topics.UPDATES)
                                }
                            },
                            enabled = hasNotificationPermission.value
                        )
                    }

                    Divider()

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                "Notificaciones de productos",
                                style = MaterialTheme.typography.bodyLarge
                            )
                            Text(
                                "Nuevos productos y cambios de inventario",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = productNotifications,
                            onCheckedChange = { isChecked ->
                                productNotifications = isChecked
                                if (isChecked) {
                                    FCMTopicManager.subscribeTopic(FCMTopicManager.Topics.PRODUCTS)
                                } else {
                                    FCMTopicManager.unsubscribeTopic(FCMTopicManager.Topics.PRODUCTS)
                                }
                            },
                            enabled = hasNotificationPermission.value
                        )
                    }
                }
            }
        }
    }
}