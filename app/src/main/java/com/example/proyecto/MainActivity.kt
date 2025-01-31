package com.example.proyecto

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.proyecto.data.repository.UserRepository
import com.example.proyecto.domain.usecase.*
import com.example.proyecto.ui.screens.home.HomeScreen
import com.example.proyecto.ui.screens.home.HomeViewModel
import com.example.proyecto.ui.screens.login.LoginScreen
import com.example.proyecto.ui.screens.login.LoginViewModel
import com.example.proyecto.ui.screens.profile.EditProfileScreen
import com.example.proyecto.ui.screens.profile.EditProfileViewModel
import com.example.proyecto.ui.screens.register.RegisterScreen
import com.example.proyecto.ui.screens.register.RegisterViewModel
import com.example.proyecto.ui.theme.ProyectoTheme
import com.example.proyecto.utils.Constants.Routes

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val userRepository = UserRepository()
        val loginUseCase = LoginUseCase(userRepository)
        val registerUseCase = RegisterUseCase(userRepository)
        val profileUseCase = ProfileUseCase(userRepository)

        val loginViewModel = LoginViewModel(loginUseCase)
        val registerViewModel = RegisterViewModel(registerUseCase)
        val homeViewModel = HomeViewModel(profileUseCase)
        val editProfileViewModel = EditProfileViewModel(profileUseCase)

        setContent {
            ProyectoTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    App(
                        loginViewModel = loginViewModel,
                        registerViewModel = registerViewModel,
                        homeViewModel = homeViewModel,
                        editProfileViewModel = editProfileViewModel
                    )
                }
            }
        }
    }
}

@Composable
fun App(
    loginViewModel: LoginViewModel,
    registerViewModel: RegisterViewModel,
    homeViewModel: HomeViewModel,
    editProfileViewModel: EditProfileViewModel
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
    }
}