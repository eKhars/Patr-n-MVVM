package com.example.proyecto.utils

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class EncryptedPreferencesManager(private val context: Context) {

    companion object {
        private const val PREFERENCES_FILE = "encrypted_user_prefs"
        private const val KEY_USER_NAME = "user_name"
        private const val KEY_DARK_MODE = "dark_mode"
        private const val KEY_LANGUAGE = "language"
        private const val KEY_NOTIFICATION_VOLUME = "notification_volume"
        private const val KEY_LAST_ACCESS = "last_access"
        private const val KEY_LAST_LOCATION = "last_location"
        private const val KEY_TOTAL_USAGE_TIME = "total_usage_time"
        private const val DEFAULT_USER_NAME = ""
        private const val DEFAULT_DARK_MODE = false
        private const val DEFAULT_LANGUAGE = 0
        private const val DEFAULT_NOTIFICATION_VOLUME = 50
        private const val DEFAULT_LAST_ACCESS = "Nunca"
        private const val DEFAULT_LAST_LOCATION = "Desconocida"
        private const val DEFAULT_TOTAL_USAGE_TIME = 0L
    }

    private val masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)

    private val encryptedSharedPreferences: SharedPreferences by lazy {
        EncryptedSharedPreferences.create(
            PREFERENCES_FILE,
            masterKeyAlias,
            context,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    private var sessionStartTime: Long = System.currentTimeMillis()
    private var sessionAccumulatedTime: Long = 0

    fun saveUserName(userName: String) {
        encryptedSharedPreferences.edit().putString(KEY_USER_NAME, userName).apply()
    }

    fun getUserName(): String {
        return encryptedSharedPreferences.getString(KEY_USER_NAME, DEFAULT_USER_NAME) ?: DEFAULT_USER_NAME
    }

    fun saveDarkMode(isDarkMode: Boolean) {
        encryptedSharedPreferences.edit().putBoolean(KEY_DARK_MODE, isDarkMode).apply()
    }

    fun getDarkMode(): Boolean {
        return encryptedSharedPreferences.getBoolean(KEY_DARK_MODE, DEFAULT_DARK_MODE)
    }

    fun saveLanguage(languageIndex: Int) {
        encryptedSharedPreferences.edit().putInt(KEY_LANGUAGE, languageIndex).apply()
    }

    fun getLanguage(): Int {
        return encryptedSharedPreferences.getInt(KEY_LANGUAGE, DEFAULT_LANGUAGE)
    }

    fun saveNotificationVolume(volume: Int) {
        encryptedSharedPreferences.edit().putInt(KEY_NOTIFICATION_VOLUME, volume).apply()
    }

    fun getNotificationVolume(): Int {
        return encryptedSharedPreferences.getInt(KEY_NOTIFICATION_VOLUME, DEFAULT_NOTIFICATION_VOLUME)
    }

    fun updateLastAccess() {
        val currentDateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault())
        val currentDate = currentDateFormat.format(Date())
        encryptedSharedPreferences.edit().putString(KEY_LAST_ACCESS, currentDate).apply()
    }

    fun getLastAccess(): String {
        return encryptedSharedPreferences.getString(KEY_LAST_ACCESS, DEFAULT_LAST_ACCESS) ?: DEFAULT_LAST_ACCESS
    }

    fun saveLastLocation(latitude: Double, longitude: Double) {
        val locationText = "Lat: $latitude, Long: $longitude"
        encryptedSharedPreferences.edit().putString(KEY_LAST_LOCATION, locationText).apply()
    }

    fun getLastLocation(): String {
        return encryptedSharedPreferences.getString(KEY_LAST_LOCATION, DEFAULT_LAST_LOCATION) ?: DEFAULT_LAST_LOCATION
    }

    fun initializeDefaultLocationIfNeeded() {
        if (getLastLocation() == DEFAULT_LAST_LOCATION) {
            saveLastLocation(19.4326, -99.1332) // Ubicación predeterminada (por ejemplo, CDMX)
        }
    }

    fun saveTotalUsageTime(timeMillis: Long) {
        encryptedSharedPreferences.edit().putLong(KEY_TOTAL_USAGE_TIME, timeMillis).apply()
    }

    fun getTotalUsageTime(): Long {
        return encryptedSharedPreferences.getLong(KEY_TOTAL_USAGE_TIME, DEFAULT_TOTAL_USAGE_TIME)
    }

    fun resetSessionTime() {
        val currentTime = System.currentTimeMillis()
        if (sessionStartTime > 0) {
            sessionAccumulatedTime += (currentTime - sessionStartTime)
        }
        sessionStartTime = currentTime
    }

    fun updateUsageTime() {
        val currentTime = System.currentTimeMillis()
        val sessionTime = sessionAccumulatedTime + (currentTime - sessionStartTime)

        if (sessionTime > 0) {
            val totalTimeMillis = getTotalUsageTime() + sessionTime
            saveTotalUsageTime(totalTimeMillis)

            sessionStartTime = currentTime
            sessionAccumulatedTime = 0
        }
    }

    fun getCurrentSessionTime(): Long {
        val currentTime = System.currentTimeMillis()
        return sessionAccumulatedTime + (currentTime - sessionStartTime)
    }

    fun getFormattedTotalUsageTime(): String {
        val totalMillis = getTotalUsageTime() + getCurrentSessionTime()
        val hours = totalMillis / (1000 * 60 * 60)
        val minutes = (totalMillis % (1000 * 60 * 60)) / (1000 * 60)
        val seconds = (totalMillis % (1000 * 60)) / 1000

        return String.format("%02d:%02d:%02d", hours, minutes, seconds)
    }
}