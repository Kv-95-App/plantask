package kv.apps.taskmanager.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import kv.apps.taskmanager.presentation.screens.authScreens.ForgotPasswordScreen
import kv.apps.taskmanager.presentation.screens.authScreens.LoginScreen
import kv.apps.taskmanager.presentation.screens.authScreens.RegisterScreen
import kv.apps.taskmanager.presentation.screens.friendScreens.AddFriendScreen
import kv.apps.taskmanager.presentation.screens.friendScreens.FriendsScreen
import kv.apps.taskmanager.presentation.screens.projectScreens.AddProjectScreen
import kv.apps.taskmanager.presentation.screens.projectScreens.AddTaskScreen
import kv.apps.taskmanager.presentation.screens.projectScreens.ProjectDetailScreen
import kv.apps.taskmanager.presentation.screens.projectScreens.ProjectListScreen
import kv.apps.taskmanager.presentation.screens.utilScreens.GetStartedScreen
import kv.apps.taskmanager.presentation.screens.utilScreens.SplashScreen
import kv.apps.taskmanager.presentation.viewmodel.AuthViewModel

@Composable
fun NavGraph(
    navController: NavHostController,
    startDestination: String,
    authViewModel: AuthViewModel = hiltViewModel()
) {
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        // Get Started Screen
        composable(route = Screen.GetStarted.route) {
            GetStartedScreen(navController)
        }

        // Project List Screen
        composable(route = Screen.ProjectList.route) {
            ProjectListScreen(
                navController = navController,
                projectViewModel = hiltViewModel(),
                taskViewModel = hiltViewModel(),
                onAddProjectClicked = {
                    navController.navigate(Screen.AddProject.route)
                },
                onProjectSelected = { projectId ->
                    navController.navigate(Screen.ProjectDetail.route + "/$projectId")
                }
            )
        }

        // Add Project Screen
        composable(route = Screen.AddProject.route) {
            AddProjectScreen(navController)
        }

        // Project Detail Screen
        composable(route = Screen.ProjectDetail.route) { backStackEntry ->
            val projectId = backStackEntry.arguments?.getString("projectId") ?: return@composable
            ProjectDetailScreen(navController, projectId)
        }

        // Login Screen
        composable(route = Screen.Login.route) {
            LoginScreen(navController, onLoginSuccess = {
                navController.navigate(Screen.ProjectList.route) {
                    popUpTo(Screen.Login.route) { inclusive = true }
                }
            })
        }

        // Register Screen
        composable(route = Screen.Register.route) {
            RegisterScreen(navController, onRegisterSuccess = {
                navController.navigate(Screen.ProjectList.route) {
                    popUpTo(Screen.Register.route) { inclusive = true }
                }
            })
        }

        // Forgot Password Screen
        composable(route = Screen.ForgotPassword.route) {
            ForgotPasswordScreen(navController)
        }

        // Splash Screen
        composable(route = Screen.SplashScreen.route) {
            SplashScreen(navController = navController)
        }

        // Friends Screen
        composable(route = Screen.Friends.route) {
            FriendsScreen(
                navController = navController,
                userFriendsViewModel = hiltViewModel(),
                authViewModel = authViewModel
            )
        }

        // Add Friend Screen
        composable(route = Screen.AddFriend.route) {
            AddFriendScreen(
                navController = navController,
                userFriendsViewModel = hiltViewModel(),
                authViewModel = authViewModel
            )
        }

        // Add Task Screen
        composable(route = Screen.AddTask.route) {
            AddTaskScreen(navController)
        }
    }
}