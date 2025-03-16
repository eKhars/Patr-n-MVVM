package com.example.proyecto

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.proyecto.data.repository.UserRepository
import com.example.proyecto.data.repository.ProductRepository
import com.example.proyecto.domain.usecase.*
import com.example.proyecto.ui.screens.home.HomeScreen
import com.example.proyecto.ui.screens.home.HomeViewModel
import com.example.proyecto.ui.screens.login.LoginScreen
import com.example.proyecto.ui.screens.login.LoginViewModel
import com.example.proyecto.ui.screens.profile.EditProfileScreen
import com.example.proyecto.ui.screens.profile.EditProfileViewModel
import com.example.proyecto.ui.screens.register.RegisterScreen
import com.example.proyecto.ui.screens.register.RegisterViewModel
import com.example.proyecto.ui.screens.product.ProductScreen
import com.example.proyecto.ui.theme.ProyectoTheme
import com.example.proyecto.utils.Constants.Routes
import com.example.proyecto.ui.screens.product.ProductViewModel
import com.example.proyecto.ui.screens.notifications.NotificationPreferencesScreen
import com.example.proyecto.utils.rememberNotificationPermissionState
import com.example.proyecto.utils.FCMTopicManager
import com.example.proyecto.utils.EncryptedPreferencesManager
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class MainActivity : ComponentActivity() {
    companion object {
        private const val TAG = "MainActivity"

        // StateFlow para compartir el estado del tema oscuro a través de la aplicación
        private val _darkModeState = MutableStateFlow(false)
        val darkModeState = _darkModeState.asStateFlow()

        // StateFlow para compartir el idioma preferido
        private val _languageState = MutableStateFlow(0)
        val languageState = _languageState.asStateFlow()

        // Función para actualizar el tema oscuro
        fun updateDarkMode(isDark: Boolean) {
            _darkModeState.value = isDark
        }

        // Función para actualizar el idioma
        fun updateLanguage(languageIndex: Int) {
            _languageState.value = languageIndex
        }
    }

    private lateinit var preferencesManager: EncryptedPreferencesManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Inicializar el gestor de preferencias encriptadas
        preferencesManager = EncryptedPreferencesManager(applicationContext)

        // Inicializar la ubicación predeterminada si es necesario
        preferencesManager.initializeDefaultLocationIfNeeded()

        // Reiniciar el contador de tiempo de la sesión actual
        preferencesManager.resetSessionTime()

        // Actualizar la última hora de acceso
        preferencesManager.updateLastAccess()

        // Inicializar el estado del tema oscuro con el valor guardado
        _darkModeState.value = preferencesManager.getDarkMode()

        // Inicializar el estado del idioma con el valor guardado
        _languageState.value = preferencesManager.getLanguage()

        // Solicitar y guardar el token de FCM
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (!task.isSuccessful) {
                Log.w(TAG, "Error al obtener token de FCM", task.exception)
                return@addOnCompleteListener
            }

            // Obtener el nuevo token
            val token = task.result
            Log.d(TAG, "Token FCM: $token")

            // Aquí puedes enviar el token a tu backend si lo necesitas

            // Suscribir al usuario a tópicos por defecto
            FCMTopicManager.subscribeToDefaultTopics()
        }

        // Repositorios
        val userRepository = UserRepository()
        val productRepository = ProductRepository()

        // Casos de uso
        val loginUseCase = LoginUseCase(userRepository)
        val registerUseCase = RegisterUseCase(userRepository)
        val profileUseCase = ProfileUseCase(userRepository)
        val productUseCase = ProductUseCase(productRepository)

        // ViewModels
        val loginViewModel = LoginViewModel(loginUseCase)
        val registerViewModel = RegisterViewModel(registerUseCase)
        val homeViewModel = HomeViewModel(profileUseCase)
        val editProfileViewModel = EditProfileViewModel(profileUseCase)
        val productViewModel = ProductViewModel(productUseCase)

        setContent {
            // Observar el estado del tema oscuro
            val isDarkMode by darkModeState.collectAsState()

            ProyectoTheme(darkTheme = isDarkMode) {
                // Verificar permisos de notificación
                val hasNotificationPermission = rememberNotificationPermissionState()

                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        // Aplicación principal
                        AppNavigation(
                            loginViewModel = loginViewModel,
                            registerViewModel = registerViewModel,
                            homeViewModel = homeViewModel,
                            editProfileViewModel = editProfileViewModel,
                            productViewModel = productViewModel
                        )

                        // Banner de notificación (en la parte superior)
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .align(Alignment.TopCenter)
                        ) {
                            com.example.proyecto.ui.components.NotificationBanner()
                        }
                    }
                }
            }
        }
    }

    override fun onPause() {
        super.onPause()

        // Actualizar el tiempo de uso total antes de pausar la aplicación
        preferencesManager.updateUsageTime()
    }

    override fun onResume() {
        super.onResume()

        // Reiniciar el contador de tiempo cuando la aplicación se reanuda
        preferencesManager.resetSessionTime()
    }

    override fun onStop() {
        // Asegurarnos de guardar el tiempo de uso
        preferencesManager.updateUsageTime()
        super.onStop()
    }

    override fun onDestroy() {
        // Asegurarse de guardar el tiempo de uso antes de destruir la actividad
        preferencesManager.updateUsageTime()
        super.onDestroy()
    }
}

@Composable
fun AppNavigation(
    loginViewModel: LoginViewModel,
    registerViewModel: RegisterViewModel,
    homeViewModel: HomeViewModel,
    editProfileViewModel: EditProfileViewModel,
    productViewModel: ProductViewModel
) {
    val navController = rememberNavController()
    var authToken by remember { mutableStateOf<String?>(null) }
    var userId by remember { mutableStateOf<String?>(null) }

    NavHost(
        navController = navController,
        startDestination = Routes.LOGIN
    ) {
        composable(Routes.LOGIN) {
            LoginScreen(
                viewModel = loginViewModel,
                onLoginSuccess = { token, id ->
                    authToken = token
                    userId = id
                    navController.navigate(Routes.HOME) {
                        popUpTo(Routes.LOGIN) { inclusive = true }
                    }
                },
                onRegisterClick = {
                    navController.navigate(Routes.REGISTER)
                }
            )
        }

        composable(Routes.REGISTER) {
            RegisterScreen(
                viewModel = registerViewModel,
                onRegisterSuccess = {
                    navController.navigate(Routes.LOGIN) {
                        popUpTo(Routes.REGISTER) { inclusive = true }
                    }
                },
                onBackClick = {
                    navController.navigateUp()
                }
            )
        }

        composable(Routes.HOME) {
            HomeScreen(
                viewModel = homeViewModel,
                userId = userId ?: "",
                token = authToken ?: "",
                onLogout = {
                    authToken = null
                    userId = null
                    navController.navigate(Routes.LOGIN) {
                        popUpTo(Routes.HOME) { inclusive = true }
                    }
                },
                onEditProfile = {
                    navController.navigate(Routes.EDIT_PROFILE)
                },
                onProductsClick = {
                    navController.navigate(Routes.PRODUCTS)
                },
                onNotificationsClick = {
                    navController.navigate(Routes.NOTIFICATIONS)
                }
            )
        }

        composable(Routes.EDIT_PROFILE) {
            EditProfileScreen(
                viewModel = editProfileViewModel,
                userId = userId ?: "",
                token = authToken ?: "",
                onUpdateSuccess = {
                    navController.navigateUp()
                },
                onBackClick = {
                    navController.navigateUp()
                }
            )
        }

        composable(Routes.PRODUCTS) {
            ProductScreen(
                viewModel = productViewModel,
                token = authToken ?: "",
                onBackClick = {
                    navController.navigateUp()
                }
            )
        }

        composable(Routes.NOTIFICATIONS) {
            NotificationPreferencesScreen(
                onBackClick = {
                    navController.navigateUp()
                }
            )
        }
    }
}