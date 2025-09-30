package kv.apps.taskmanager.presentation.screens.utils.screens

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import kotlinx.coroutines.delay
import kv.apps.taskmanager.presentation.navigation.Screen
import kv.apps.taskmanager.presentation.viewmodel.auth.AuthViewModel

@Composable
fun SplashScreen(
    navController: NavController,
    authViewModel: AuthViewModel
) {
    val uiState by authViewModel.uiState.collectAsStateWithLifecycle()
    val user = uiState.user
    val userId = uiState.userId
    val isLoggingOut = uiState.isLoggingOut

    LaunchedEffect(user, userId, isLoggingOut) {
        if (isLoggingOut) return@LaunchedEffect

        val isAuthenticated = (user != null && user.uid.isNotBlank()) || !userId.isNullOrBlank()
        if (isAuthenticated) {
            delay(1000)
            navController.navigate(Screen.ProjectList.route) {
                popUpTo(Screen.SplashScreen.route) { inclusive = true }
            }
        } else {
            delay(1000)
            navController.navigate(Screen.Login.route) {
                popUpTo(Screen.SplashScreen.route) { inclusive = true }
            }
        }
    }

    kv.apps.taskmanager.presentation.screens.utils.shared.uiComposables.LoadingOverlay()
}