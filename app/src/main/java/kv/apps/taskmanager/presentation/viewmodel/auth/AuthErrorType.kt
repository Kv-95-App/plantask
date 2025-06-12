package kv.apps.taskmanager.presentation.viewmodel.auth

sealed class AuthErrorType(val message: String) {
    object InvalidCredentials : AuthErrorType("Invalid email or password")
    object UserNotFound : AuthErrorType("Account not found")
    object EmailInUse : AuthErrorType("Email already in use")
    object NetworkError : AuthErrorType("Network connection failed")
    object ServerError : AuthErrorType("Server error occurred")
    object UnknownError : AuthErrorType("An unknown error occurred")
    object TimeoutError : AuthErrorType("Request timed out")
    object InvalidEmailFormat : AuthErrorType("Invalid email format")
    object WeakPassword : AuthErrorType("Password is too weak")
    object UserDisabled : AuthErrorType("Account is disabled")
    object TooManyAttempts : AuthErrorType("Too many login attempts")
    object RegisterError : AuthErrorType("Registration failed")
    object ResetPasswordError : AuthErrorType("Failed to reset password")
    object FetchUserError : AuthErrorType("Failed to load user data")
    object LoginError : AuthErrorType("Login failed")
    object PasswordMismatch : AuthErrorType("Passwords do not match")
}