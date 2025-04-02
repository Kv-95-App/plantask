package kv.apps.taskmanager.presentation.navigation

sealed class Screen(val route: String) {
    object ProjectList : Screen("project_list")
    object ProjectDetail : Screen("project_detail/{projectId}") {
        fun createRoute(projectId: String) = "project_detail/$projectId"
    }
    object AddProject : Screen("add_project")
    object GetStarted : Screen("get_started")
    object Login : Screen("login")
    object Register : Screen("register")
    object ForgotPassword : Screen("forgot_password")
    object SplashScreen : Screen("splash_screen")
    object Friends : Screen("friends")
    object AddFriend : Screen("add_friend")
    object AddTask : Screen("add_task")
    object Notifications : Screen("notifications")
}
