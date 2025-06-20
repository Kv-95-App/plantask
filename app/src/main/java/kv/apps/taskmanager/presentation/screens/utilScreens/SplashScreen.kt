package kv.apps.taskmanager.presentation.screens.utilScreens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import kotlinx.coroutines.delay
import kv.apps.taskmanager.presentation.navigation.Screen
import kv.apps.taskmanager.presentation.viewmodel.auth.AuthViewModel
import kv.apps.taskmanager.theme.backgroundColor

@Composable
fun SplashScreen(
    navController: NavController,
    authViewModel: AuthViewModel
) {
    val uiState = authViewModel.uiState.collectAsStateWithLifecycle()
    val user = uiState.value.user
    val isKeepLoggedIn = uiState.value.isKeepLoggedIn
    val isLoggedIn = uiState.value.userId != null

    LaunchedEffect(user, isKeepLoggedIn, isLoggedIn) {
        delay(500)
        val startDestination = if (user != null || isKeepLoggedIn == true || isLoggedIn == true) {
            Screen.ProjectList.route
        } else {
            Screen.Login.route
        }
        navController.navigate(startDestination) {
            popUpTo(Screen.SplashScreen.route) { inclusive = true }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }
}