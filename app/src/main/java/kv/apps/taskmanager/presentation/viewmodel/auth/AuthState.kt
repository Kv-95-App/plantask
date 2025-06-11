package kv.apps.taskmanager.presentation.viewmodel.auth

import kv.apps.taskmanager.domain.model.User

data class AuthUiState(
    val user: User? = null,
    val userId: String? = null,
    val isKeepLoggedIn: Boolean = false,
    val isLoading: Boolean = false,
    val isLoggingOut: Boolean = false
)

