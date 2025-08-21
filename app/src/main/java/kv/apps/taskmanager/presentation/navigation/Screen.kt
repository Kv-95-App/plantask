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
    object ProjectMembers : Screen("project_members/{projectId}") {
        fun createRoute(projectId: String) = "project_members/$projectId"
    }
    object CompletedProjectDetail : Screen("completed_project_detail/{projectId}") {
        fun createRoute(projectId: String) = "completed_project_detail/$projectId"
    }
    // Task Screens
    object TaskDetail : Screen("task_detail/{taskId}/{projectId}") {
        fun createRoute(taskId: String, projectId: String) = "task_detail/$taskId/$projectId"
    }
    object CompletedTaskDetail : Screen("completed_task_detail/{taskId}/{projectId}") {
        fun createRoute(taskId: String, projectId: String) = "completed_task_detail/$taskId/$projectId"
    }
    object AddTask : Screen("add_task/{projectId}") {
        fun createRoute(projectId: String) = "add_task/$projectId"
    }
    // Friend Screens
    object Friends : Screen("friends")
    object AddFriend : Screen("add_friend")
    // Utility Screens
    object GetStarted : Screen("get_started")
    object SplashScreen : Screen("splash_screen")
    object Profile : Screen("profile")
    object Notifications : Screen("notifications")
    object TargetProfile : Screen("target_profile/{userId}") {
        fun createRoute(userId: String) = "target_profile/$userId"
    }
    object FriendRequests : Screen("friend_requests")
    object ProjectInvites : Screen("project_invites/{projectId}") {
        fun createRoute(projectId: String) = "project_invites/$projectId"
    }
}
