package kv.apps.taskmanager.presentation.navigation

sealed class Screen(val route: String) {
    // Authentication Screens
    object Login : Screen("login")
    object Register : Screen("register")
    object ForgotPassword : Screen("forgot_password")
    // Project Screens
    object ProjectList : Screen("project_list")
    object ProjectDetail : Screen("project_detail/{projectId}") {
        fun createRoute(projectId: String) = "project_detail/$projectId" }
    object AddProject : Screen("add_project")
    object OngoingProjects : Screen("ongoing_projects")
    object CompletedProjects : Screen("completed_projects")
    // Task Screens
    object TaskDetail : Screen("task_detail/{taskId}") {
            fun createRoute(taskId: String) = "task_detail/$taskId" }
    object AddTask : Screen("add_task")
    // Friend Screens
    object Friends : Screen("friends")
    object AddFriend : Screen("add_friend")
    // Utility Screens
    object GetStarted : Screen("get_started")
    object SplashScreen : Screen("splash_screen")
    object Profile : Screen("profile")
    object Notifications : Screen("notifications")
}
