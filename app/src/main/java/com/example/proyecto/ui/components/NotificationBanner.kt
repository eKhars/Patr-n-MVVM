package com.example.proyecto.ui.components

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay

// Objeto para mantener estado global de notificaciones
object NotificationState {
    // Usamos mutableStateOf para que los cambios sean reactivos en Compose
    val message = mutableStateOf("")
    val isVisible = mutableStateOf(false)

    fun updateNotificationMessage(message: String) {
        this.message.value = message
        this.isVisible.value = true
    }

    fun clearNotification() {
        this.isVisible.value = false
    }
}

@Composable
fun NotificationBanner() {
    val isVisible by remember { NotificationState.isVisible }
    val message by remember { NotificationState.message }

    // Efecto para auto-ocultar la notificación después de unos segundos
    LaunchedEffect(message) {
        if (message.isNotEmpty()) {
            delay(5000) // Ocultar después de 5 segundos
            NotificationState.clearNotification()
        }
    }

    AnimatedVisibility(
        visible = isVisible && message.isNotEmpty(),
        enter = slideInVertically(initialOffsetY = { -it }) + fadeIn(),
        exit = slideOutVertically(targetOffsetY = { -it }) + fadeOut()
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(8.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primary
            )
        ) {
            Text(
                text = message,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                color = MaterialTheme.colorScheme.onPrimary,
                textAlign = TextAlign.Center
            )
        }
    }
}