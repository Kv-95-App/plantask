package kv.apps.taskmanager.presentation.viewmodel.auth

sealed class AuthErrorType(val message: String) {
    object InvalidEmailFormat : AuthErrorType("Invalid email format")
    object WeakPassword : AuthErrorType("Password is too weak")
    object RegisterError : AuthErrorType("Registration failed")
    object ResetPasswordError : AuthErrorType("Failed to reset password")
    object FetchUserError : AuthErrorType("Failed to load user data")
    object LoginError : AuthErrorType("Login failed")
    object PasswordMismatch : AuthErrorType("Passwords do not match")
    object NetworkError : AuthErrorType("Network error occurred")
    object LogoutError : AuthErrorType("Logout failed")
}