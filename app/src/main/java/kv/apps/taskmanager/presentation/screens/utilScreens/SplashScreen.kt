package kv.apps.taskmanager.presentation.screens.utilScreens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import kotlinx.coroutines.delay
import kv.apps.taskmanager.presentation.navigation.Screen
import kv.apps.taskmanager.presentation.viewmodel.AuthViewModel
import kv.apps.taskmanager.theme.backgroundColor

@Composable
fun SplashScreen(
    navController: NavController,
    authViewModel: AuthViewModel) {
    val user = authViewModel.user.collectAsStateWithLifecycle().value
    val isKeepLoggedIn = authViewModel.isKeepLoggedIn.collectAsStateWithLifecycle().value

    LaunchedEffect(user, isKeepLoggedIn) {
        delay(500)
        val startDestination = if (user != null || isKeepLoggedIn == true) {
            Screen.ProjectList.route
        } else {
            Screen.GetStarted.route
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