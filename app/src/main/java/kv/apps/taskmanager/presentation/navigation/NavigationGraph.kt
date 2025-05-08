package kv.apps.taskmanager.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
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
import kv.apps.taskmanager.presentation.screens.taskScreens.AddTaskScreen
import kv.apps.taskmanager.presentation.screens.projectScreens.CompletedProjectsScreen
import kv.apps.taskmanager.presentation.screens.projectScreens.OngoingProjectsScreen
import kv.apps.taskmanager.presentation.screens.projectScreens.ProjectDetailScreen
import kv.apps.taskmanager.presentation.screens.projectScreens.ProjectListScreen
import kv.apps.taskmanager.presentation.screens.utilScreens.GetStartedScreen
import kv.apps.taskmanager.presentation.screens.utilScreens.NotificationsScreen
import kv.apps.taskmanager.presentation.screens.utilScreens.SplashScreen
import kv.apps.taskmanager.presentation.viewmodel.AuthViewModel
import kv.apps.taskmanager.presentation.viewmodel.ProjectViewModel
import kv.apps.taskmanager.presentation.viewmodel.TaskViewModel
import kv.apps.taskmanager.presentation.viewmodel.UserFriendsViewModel

@Composable
fun NavGraph(
    navController: NavHostController,
    startDestination: String,
    authViewModel: AuthViewModel = hiltViewModel(),
    taskViewModel: TaskViewModel = hiltViewModel(),
    userFriendsViewModel: UserFriendsViewModel = hiltViewModel(),
    projectViewModel: ProjectViewModel = hiltViewModel()
) {
    val isLoggedIn = authViewModel.isLoggedIn.collectAsState()

    NavHost(
        navController = navController,
        startDestination = if (isLoggedIn.value == true) Screen.ProjectList.route else Screen.SplashScreen.route
    ) {
        // Authentication Screens
        composable(route = Screen.Login.route) {
            LoginScreen(
                navController = navController,
                onLoginSuccess = {
                    navController.navigate(Screen.ProjectList.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                                 },
                authViewModel = authViewModel
            )
        }
        composable(route = Screen.Register.route) {
            RegisterScreen(
                navController = navController,
                authViewModel = authViewModel
            )
        }

        composable(route = Screen.ForgotPassword.route) {
            ForgotPasswordScreen(navController = navController, authViewModel = authViewModel)
        }

        // Project Screens
        composable(route = Screen.ProjectList.route) {
            ProjectListScreen(
                navController = navController,
                projectViewModel = projectViewModel,
                taskViewModel = taskViewModel,
                authViewModel = authViewModel,
                onAddProjectClicked = { navController.navigate(Screen.AddProject.route) },
                )
        }

        composable(route = Screen.AddProject.route) {
            AddProjectScreen(
                navController = navController,
                projectViewModel = projectViewModel,
                authViewModel = authViewModel,
                userFriendsViewModel = userFriendsViewModel
            )
        }

        composable(route = Screen.ProjectDetail.route) { backStackEntry ->
            val projectId = backStackEntry.arguments?.getString("projectId") ?: return@composable
            ProjectDetailScreen(
                navController = navController,
                projectId = projectId,
                taskViewModel = taskViewModel,
                projectViewModel = projectViewModel,
                userFriendsViewModel = userFriendsViewModel,
                authViewModel = authViewModel
            )
        }

        composable(route = Screen.OngoingProjects.route) {
            OngoingProjectsScreen(
                navController = navController,
                projectViewModel = projectViewModel,
                taskViewModel = taskViewModel,
                authViewModel = authViewModel,
                onAddProjectClicked = { navController.navigate(Screen.AddProject.route) }
            )
        }

        composable(route = Screen.CompletedProjects.route) {
            CompletedProjectsScreen(
                navController = navController,
                projectViewModel = projectViewModel,
                taskViewModel = taskViewModel,
                authViewModel = authViewModel
            )
        }

        // Task Screens
        composable(route = Screen.TaskDetail.route) {}

        composable(route = Screen.AddTask.route) {
            AddTaskScreen(
                navController = navController,
                projectViewModel = projectViewModel
            )
        }

        // Friend Screens
        composable(route = Screen.Friends.route) {
            FriendsScreen(
                navController = navController,
                userFriendsViewModel = userFriendsViewModel,
                authViewModel = authViewModel
            )
        }

        composable(route = Screen.AddFriend.route) {
            AddFriendScreen(
                navController = navController,
                userFriendsViewModel = userFriendsViewModel,
                authViewModel = authViewModel
            )
        }


        // Utility Screens
        composable(route = Screen.SplashScreen.route) {
            SplashScreen(
                navController = navController,
                authViewModel = authViewModel
            )
        }

        composable(route = Screen.GetStarted.route) {
            GetStartedScreen(
                navController = navController,

            )
        }

        composable(route = Screen.Profile.route) {

        }

        composable(route = Screen.Notifications.route) {
            NotificationsScreen(
                navController = navController,
                authViewModel = authViewModel,
                projectViewModel = projectViewModel
            )
        }


    }
}
