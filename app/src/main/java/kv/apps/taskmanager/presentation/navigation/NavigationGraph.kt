package kv.apps.taskmanager.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import kv.apps.taskmanager.presentation.screens.authScreens.ForgotPasswordScreen
import kv.apps.taskmanager.presentation.screens.authScreens.LoginScreen
import kv.apps.taskmanager.presentation.screens.authScreens.RegisterScreen
import kv.apps.taskmanager.presentation.screens.friendScreens.AddFriendScreen
import kv.apps.taskmanager.presentation.screens.friendScreens.FriendsScreen
import kv.apps.taskmanager.presentation.screens.projectScreens.AddProjectScreen
import kv.apps.taskmanager.presentation.screens.projectScreens.CompletedProjectsScreen
import kv.apps.taskmanager.presentation.screens.projectScreens.OngoingProjectsScreen
import kv.apps.taskmanager.presentation.screens.projectScreens.ProjectDetailScreen
import kv.apps.taskmanager.presentation.screens.projectScreens.ProjectListScreen
import kv.apps.taskmanager.presentation.screens.projectScreens.ProjectMembers
import kv.apps.taskmanager.presentation.screens.taskScreens.AddTaskScreen
import kv.apps.taskmanager.presentation.screens.taskScreens.TaskDetailScreen
import kv.apps.taskmanager.presentation.screens.utilScreens.GetStartedScreen
import kv.apps.taskmanager.presentation.screens.utilScreens.NotificationsScreen
import kv.apps.taskmanager.presentation.screens.utilScreens.SplashScreen
import kv.apps.taskmanager.presentation.viewmodel.auth.AuthViewModel
import kv.apps.taskmanager.presentation.viewmodel.ProjectViewModel
import kv.apps.taskmanager.presentation.viewmodel.TaskViewModel
import kv.apps.taskmanager.presentation.viewmodel.userFriends.UserFriendsViewModel

@Composable
fun NavGraph(
    navController: NavHostController,
    authViewModel: AuthViewModel = hiltViewModel(),
    taskViewModel: TaskViewModel = hiltViewModel(),
    userFriendsViewModel: UserFriendsViewModel = hiltViewModel(),
    projectViewModel: ProjectViewModel = hiltViewModel()
) {
    val isLoggedIn = authViewModel.uiState.collectAsStateWithLifecycle().value.userId != null

    NavHost(
        navController = navController,
        startDestination = if (isLoggedIn == true) Screen.ProjectList.route else Screen.SplashScreen.route
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

        composable(
            route = Screen.ProjectDetail.route,
            arguments = listOf(
                navArgument("projectId") {
                    type = NavType.StringType
                    nullable = false
                }
            )
        ) { backStackEntry ->
            val projectId = backStackEntry.arguments?.getString("projectId") ?: run {
                navController.popBackStack()
                return@composable
            }

            ProjectDetailScreen(
                navController = navController,
                projectId = projectId,
                projectViewModel = projectViewModel,
                taskViewModel = taskViewModel,
                userFriendsViewModel = userFriendsViewModel,
                authViewModel = authViewModel
            )
        }

        composable(
            route = Screen.ProjectMembers.route,
            arguments = listOf(
                navArgument("projectId") {
                    type = NavType.StringType
                    nullable = false
                }
            )
        ) { backStackEntry ->
            val projectId = backStackEntry.arguments?.getString("projectId") ?: run {
                navController.popBackStack()
                return@composable
            }

            ProjectMembers(
                projectId = projectId,
                onBackPressed = { navController.popBackStack() },
                projectViewModel = projectViewModel,
                navController = navController
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
        composable(route = Screen.TaskDetail.route) {
            val taskId = it.arguments?.getString("taskId") ?: run {
                navController.popBackStack()
                return@composable
            }
            val projectId = it.arguments?.getString("projectId") ?: run {
                navController.popBackStack()
                return@composable
            }

            TaskDetailScreen(
                taskId = taskId,
                projectId = projectId,
                onBackPressed = { navController.popBackStack() },
                taskViewModel = taskViewModel,
                projectViewModel = projectViewModel,
                userFriendsViewModel = userFriendsViewModel,
                authViewModel = authViewModel,
                navController = navController
            )
        }

        composable(
            route = Screen.AddTask.route,
            arguments = listOf(
                navArgument("projectId") {
                    type = NavType.StringType
                    nullable = false
                }
            )
        ) { backStackEntry ->
            AddTaskScreen(
                navController = navController,
                projectId = backStackEntry.arguments?.getString("projectId") ?: "" ,
                taskViewModel = taskViewModel,
                authViewModel = authViewModel
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