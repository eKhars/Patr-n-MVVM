package com.example.proyecto.utils

import android.util.Log
import com.google.firebase.messaging.FirebaseMessaging

object FCMTopicManager {
    private const val TAG = "FCMTopicManager"

    object Topics {
        const val GENERAL = "general"
        const val PROMOTIONS = "promotions"
        const val UPDATES = "updates"
        const val PRODUCTS = "products"
    }

    fun subscribeTopic(topic: String, onSuccess: () -> Unit = {}, onFailure: (Exception) -> Unit = {}) {
        FirebaseMessaging.getInstance().subscribeToTopic(topic)
            .addOnSuccessListener {
                Log.d(TAG, "Suscrito al tópico: $topic")
                onSuccess()
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Error al suscribirse al tópico $topic: ${e.message}")
                onFailure(e)
            }
    }

    fun unsubscribeTopic(topic: String, onSuccess: () -> Unit = {}, onFailure: (Exception) -> Unit = {}) {
        FirebaseMessaging.getInstance().unsubscribeFromTopic(topic)
            .addOnSuccessListener {
                Log.d(TAG, "Cancelada suscripción al tópico: $topic")
                onSuccess()
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Error al cancelar suscripción al tópico $topic: ${e.message}")
                onFailure(e)
            }
    }

    fun subscribeToDefaultTopics() {
        subscribeTopic(Topics.GENERAL)
    }
}