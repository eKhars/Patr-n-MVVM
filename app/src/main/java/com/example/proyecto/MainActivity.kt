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
import com.google.firebase.messaging.FirebaseMessaging

class MainActivity : ComponentActivity() {
    companion object {
        private const val TAG = "MainActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

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
            ProyectoTheme {
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