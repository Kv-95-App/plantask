package kv.apps.taskmanager.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
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
import kv.apps.taskmanager.presentation.screens.friendSection.screens.AddFriendScreen
import kv.apps.taskmanager.presentation.screens.friendSection.screens.FriendsScreen
import kv.apps.taskmanager.presentation.screens.profileScreens.Profile
import kv.apps.taskmanager.presentation.screens.profileScreens.TargetProfileScreen
import kv.apps.taskmanager.presentation.screens.projectSection.screens.AddProjectScreen
import kv.apps.taskmanager.presentation.screens.projectSection.screens.CompletedProjectDetailScreen
import kv.apps.taskmanager.presentation.screens.projectSection.screens.CompletedProjectsScreen
import kv.apps.taskmanager.presentation.screens.projectSection.screens.OngoingProjectsScreen
import kv.apps.taskmanager.presentation.screens.projectSection.screens.ProjectDetailScreen
import kv.apps.taskmanager.presentation.screens.projectSection.screens.ProjectListScreen
import kv.apps.taskmanager.presentation.screens.projectSection.screens.ProjectMembers
import kv.apps.taskmanager.presentation.screens.taskSection.screens.AddTaskScreen
import kv.apps.taskmanager.presentation.screens.taskSection.screens.CompletedTaskDetailScreen
import kv.apps.taskmanager.presentation.screens.taskSection.screens.TaskDetailScreen
import kv.apps.taskmanager.presentation.screens.utils.screens.GetStartedScreen
import kv.apps.taskmanager.presentation.screens.utils.screens.NotificationsScreen
import kv.apps.taskmanager.presentation.screens.utils.screens.SplashScreen
import kv.apps.taskmanager.presentation.viewmodel.auth.AuthViewModel
import kv.apps.taskmanager.presentation.viewmodel.project.ProjectViewModel
import kv.apps.taskmanager.presentation.viewmodel.task.TaskViewModel
import kv.apps.taskmanager.presentation.viewmodel.userFriends.UserFriendsViewModel

@Composable
fun NavGraph(
    navController: NavHostController,
    authViewModel: AuthViewModel = hiltViewModel(),
    taskViewModel: TaskViewModel = hiltViewModel(),
    userFriendsViewModel: UserFriendsViewModel = hiltViewModel(),
    projectViewModel: ProjectViewModel = hiltViewModel(),
    onGoogleSignInClicked: () -> Unit
) {
    val authUiState by authViewModel.uiState.collectAsStateWithLifecycle()


    LaunchedEffect(authUiState.isLoggingOut) {
        if (authUiState.isLoggingOut) {
            navController.navigate(Screen.SplashScreen.route) {
                popUpTo(navController.graph.id) { inclusive = true }
            }
        }
    }

    NavHost(
        navController = navController,
        startDestination = Screen.SplashScreen.route
    ) {
        // Authentication Screens
        composable(route = Screen.Login.route) {
            LoginScreen(
                navController = navController,
                authViewModel = authViewModel,
                onLoginSuccess = {
                    navController.navigate(Screen.ProjectList.route) {
                        popUpTo("login") { inclusive = true }
                    }
                },
                onGoogleSignInClicked = onGoogleSignInClicked
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
                authViewModel = authViewModel,
                userFriendsViewModel = userFriendsViewModel
            )
        }

        composable(
            route = Screen.CompletedProjectDetail.route,
            arguments = listOf(navArgument("projectId") { type = NavType.StringType })
        ) { backStackEntry ->
            val projectId = backStackEntry.arguments?.getString("projectId") ?: ""
            CompletedProjectDetailScreen(
                navController = navController,
                projectId = projectId,
                projectViewModel = projectViewModel,
                taskViewModel = taskViewModel,
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
                projectViewModel = projectViewModel,
                navController = navController,
                authViewModel = authViewModel,
                showInvites = false
            )
        }

        composable(route = Screen.OngoingProjects.route) {
            OngoingProjectsScreen(
                navController = navController,
                projectViewModel = projectViewModel,
                authViewModel = authViewModel,
                onAddProjectClicked = { navController.navigate(Screen.AddProject.route) }
            )
        }

        composable(route = Screen.CompletedProjects.route) {
            CompletedProjectsScreen(
                navController = navController,
                projectViewModel = projectViewModel,
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
                authViewModel = authViewModel,
                projectViewModel = projectViewModel
            )
        }

        composable(
            route = Screen.CompletedTaskDetail.route,
            arguments = listOf(
                navArgument("taskId") { type = NavType.StringType },
                navArgument("projectId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val taskId = backStackEntry.arguments?.getString("taskId") ?: ""
            val projectId = backStackEntry.arguments?.getString("projectId") ?: ""

            CompletedTaskDetailScreen(
                taskId = taskId,
                projectId = projectId,
                taskViewModel = taskViewModel,
                authViewModel = authViewModel,
                navController = navController
            )
        }

        composable(route = Screen.Friends.route) {
            FriendsScreen(
                navController = navController,
                userFriendsViewModel = userFriendsViewModel,
                authViewModel = authViewModel,
                startWithRequestsTab = false
            )
        }

        composable(route = Screen.AddFriend.route) {
            AddFriendScreen(
                navController = navController,
                userFriendsViewModel = userFriendsViewModel,
                authViewModel = authViewModel
            )
        }
        composable(route = Screen.FriendRequests.route) {
            FriendsScreen(
                navController = navController,
                userFriendsViewModel = userFriendsViewModel,
                authViewModel = authViewModel,
                startWithRequestsTab = true
            )
        }
        composable(
            route = Screen.ProjectInvites.route,
            arguments = listOf(navArgument("projectId") { type = NavType.StringType })
        ) { backStackEntry ->
            val projectId = backStackEntry.arguments?.getString("projectId") ?: ""
            ProjectMembers(
                projectId = projectId,
                projectViewModel = projectViewModel,
                navController = navController,
                authViewModel = authViewModel,
                showInvites = true
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
                navController = navController
            )
        }

        composable(route = Screen.Profile.route) {
            Profile(
                navController = navController,
                authViewModel = authViewModel
            )
        }

        composable(route = Screen.Notifications.route) {
            NotificationsScreen(
                navController = navController,
                authViewModel = authViewModel,
                projectViewModel = projectViewModel
            )
        }

        composable(
            route = Screen.TargetProfile.route,
            arguments = listOf(navArgument("userId") { type = NavType.StringType })
        ) { backStackEntry ->
            val userId = backStackEntry.arguments?.getString("userId") ?: ""
            TargetProfileScreen(
                navController = navController,
                userId = userId,
                userFriendsViewModel = userFriendsViewModel,
                authViewModel = authViewModel
            )
        }
    }
}