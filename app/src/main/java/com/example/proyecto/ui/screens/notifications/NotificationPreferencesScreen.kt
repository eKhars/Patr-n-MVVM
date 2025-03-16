package com.example.proyecto.ui.screens.notifications

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.proyecto.MainActivity
import com.example.proyecto.utils.EncryptedPreferencesManager
import com.example.proyecto.utils.FCMTopicManager
import com.example.proyecto.utils.rememberNotificationPermissionState
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationPreferencesScreen(
    onBackClick: () -> Unit
) {
    val context = LocalContext.current
    val preferencesManager = remember { EncryptedPreferencesManager(context) }
    val hasNotificationPermission = rememberNotificationPermissionState()

    var generalNotifications by remember { mutableStateOf(true) }
    var promotionNotifications by remember { mutableStateOf(false) }
    var updateNotifications by remember { mutableStateOf(true) }
    var productNotifications by remember { mutableStateOf(false) }

    var notificationVolume by remember {
        mutableStateOf(preferencesManager.getNotificationVolume())
    }

    var selectedLanguageIndex by remember {
        mutableStateOf(preferencesManager.getLanguage())
    }


    var isDarkMode by remember {
        mutableStateOf(preferencesManager.getDarkMode())
    }

    var showSnackbar by remember { mutableStateOf(false) }
    var snackbarMessage by remember { mutableStateOf("") }
    val snackbarHostState = remember { SnackbarHostState() }

    var isLanguageMenuExpanded by remember { mutableStateOf(false) }

    var tickCounter by remember { mutableStateOf(0) }

    LaunchedEffect(Unit) {
        preferencesManager.updateLastAccess()
        preferencesManager.resetSessionTime()
    }

    LaunchedEffect(Unit) {
        while(true) {
            delay(1000)
            tickCounter++
        }
    }

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
                title = { Text("Preferencias") },
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
                                snackbarMessage = "Debes habilitar los permisos en la configuración del sistema"
                                showSnackbar = true
                            }
                        ) {
                            Text("Habilitar permisos")
                        }
                    }
                }
            }

            // Configuración del Tema
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        "Apariencia",
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
                        Text(
                            "Tema oscuro",
                            style = MaterialTheme.typography.bodyLarge
                        )

                        Switch(
                            checked = isDarkMode,
                            onCheckedChange = {
                                isDarkMode = it
                                preferencesManager.saveDarkMode(it)
                                // Actualizar el estado global del tema oscuro
                                MainActivity.updateDarkMode(it)
                                snackbarMessage = "Tema actualizado"
                                showSnackbar = true
                            }
                        )
                    }
                }
            }

            // Configuración de Idioma
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        "Idioma",
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    val languages = listOf("Español", "Inglés", "Francés", "Alemán")

                    Spacer(modifier = Modifier.height(8.dp))

                    ExposedDropdownMenuBox(
                        expanded = isLanguageMenuExpanded,
                        onExpandedChange = { isLanguageMenuExpanded = it },
                    ) {
                        OutlinedTextField(
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor(),
                            readOnly = true,
                            value = languages[selectedLanguageIndex],
                            onValueChange = { },
                            label = { Text("Idioma preferido") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isLanguageMenuExpanded) },
                            colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors()
                        )

                        DropdownMenu(
                            expanded = isLanguageMenuExpanded,
                            onDismissRequest = { isLanguageMenuExpanded = false },
                            modifier = Modifier.exposedDropdownSize()
                        ) {
                            languages.forEachIndexed { index, language ->
                                DropdownMenuItem(
                                    text = { Text(language) },
                                    onClick = {
                                        selectedLanguageIndex = index
                                        isLanguageMenuExpanded = false
                                        preferencesManager.saveLanguage(index)
                                        // Actualizar el estado global del idioma
                                        MainActivity.updateLanguage(index)
                                        snackbarMessage = "Idioma actualizado a $language"
                                        showSnackbar = true
                                    }
                                )
                            }
                        }
                    }
                }
            }

            // Configuración de Notificaciones
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        "Notificaciones",
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

                    Divider()

                    // Control deslizante para el volumen de notificaciones
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 16.dp)
                    ) {
                        Text(
                            "Volumen de notificaciones: $notificationVolume%",
                            style = MaterialTheme.typography.bodyLarge
                        )

                        Slider(
                            value = notificationVolume.toFloat(),
                            onValueChange = {
                                notificationVolume = it.toInt()
                                preferencesManager.saveNotificationVolume(notificationVolume)
                            },
                            valueRange = 0f..100f,
                            enabled = hasNotificationPermission.value
                        )
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
                        "Información de la Sesión",
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    if (tickCounter >= 0) {  }

                    LaunchedEffect(tickCounter) {
                        preferencesManager.updateUsageTime()
                        preferencesManager.resetSessionTime()
                    }

                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Último acceso: ${preferencesManager.getLastAccess()}")
                        Text("Última ubicación: ${preferencesManager.getLastLocation()}")

                        val timeFormatted = preferencesManager.getFormattedTotalUsageTime()
                        Text("Tiempo total de uso: $timeFormatted")
                    }
                }
            }
        }
    }
}