package kv.apps.taskmanager.presentation.screens.taskScreens

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import kv.apps.taskmanager.presentation.navigation.Screen
import kv.apps.taskmanager.presentation.shared.uiComposables.AppDrawer
import kv.apps.taskmanager.presentation.shared.uiComposables.BottomNavigationBar
import kv.apps.taskmanager.presentation.shared.uiComposables.TopBar
import kv.apps.taskmanager.presentation.viewmodel.auth.AuthViewModel
import kv.apps.taskmanager.presentation.viewmodel.project.ProjectViewModel
import kv.apps.taskmanager.presentation.viewmodel.task.TaskViewModel
import kv.apps.taskmanager.presentation.viewmodel.userFriends.UserFriendsViewModel
import kv.apps.taskmanager.theme.backgroundColor

@Composable
fun TaskDetailScreen(
    taskId: String,
    projectId: String,
    onBackPressed: () -> Unit,
    taskViewModel: TaskViewModel,
    projectViewModel: ProjectViewModel,
    userFriendsViewModel: UserFriendsViewModel,
    authViewModel: AuthViewModel,
    navController: NavController
) {
    val isLoggingOut = authViewModel.uiState.collectAsState().value.isLoggingOut

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val coroutineScope = rememberCoroutineScope()
    AppDrawer(
        onProfileClicked = { navController.navigate(Screen.Profile.route) },
        onLogoutClicked = {
            authViewModel.logout()
            navController.navigate(Screen.Login.route) {
                popUpTo("login") { inclusive = true }
            }
        },
        drawerState = drawerState,
        isLoggingOut = isLoggingOut
    ) {
        Scaffold(
            topBar = {
                TopBar(
                    navController = navController,
                    onMenuClicked = { /* Open drawer */ },
                    showBackArrow = false,
                    isLoggingOut = isLoggingOut,
                    modifier = Modifier.padding(top = 24.dp)
                )
            },
            bottomBar = { BottomNavigationBar(navController) },
            containerColor = backgroundColor
        ) { paddingValues ->
            Surface(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                color = backgroundColor
            ) {

            }
        }
    }
}