package kv.apps.taskmanager.presentation.viewmodel.auth

sealed class AuthErrorType {
    object LoginError : AuthErrorType()
    object RegisterError : AuthErrorType()
    object ResetPasswordError : AuthErrorType()
    object FetchUserError : AuthErrorType()
}