package kv.apps.taskmanager.presentation.viewmodel.auth

import android.content.Intent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kv.apps.taskmanager.domain.repository.UserPreferencesRepository
import kv.apps.taskmanager.domain.usecase.authUseCases.ChangePasswordUseCase
import kv.apps.taskmanager.domain.usecase.authUseCases.GetCurrentUserIdUseCase
import kv.apps.taskmanager.domain.usecase.authUseCases.GetGoogleSignInIntentUseCase
import kv.apps.taskmanager.domain.usecase.authUseCases.HandleGoogleSignInResultUseCase
import kv.apps.taskmanager.domain.usecase.authUseCases.LoginUseCase
import kv.apps.taskmanager.domain.usecase.authUseCases.LogoutUseCase
import kv.apps.taskmanager.domain.usecase.authUseCases.ObserveAuthStateUseCase
import kv.apps.taskmanager.domain.usecase.authUseCases.RegisterUseCase
import kv.apps.taskmanager.domain.usecase.authUseCases.ResetPasswordUseCase
import kv.apps.taskmanager.domain.usecase.authUseCases.SessionUseCase
import kv.apps.taskmanager.domain.usecase.userUseCases.FetchUserDetailsUseCase
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val loginUseCase: LoginUseCase,
    private val registerUseCase: RegisterUseCase,
    private val fetchUserDetailsUseCase: FetchUserDetailsUseCase,
    private val resetPasswordUseCase: ResetPasswordUseCase,
    private val sessionUseCase: SessionUseCase,
    private val userPreferencesRepository: UserPreferencesRepository,
    private val getCurrentUserIdUseCase: GetCurrentUserIdUseCase,
    private val observeAuthStateUseCase: ObserveAuthStateUseCase,
    private val logoutUseCase: LogoutUseCase,
    private val changePasswordUseCase: ChangePasswordUseCase,
    private val getGoogleSignInIntentUseCase: GetGoogleSignInIntentUseCase,
    private val handleGoogleSignInResultUseCase: HandleGoogleSignInResultUseCase
) : ViewModel() {


    sealed class ChangePasswordState {
        object Idle : ChangePasswordState()
        object Loading : ChangePasswordState()
        data class Success(val message: String) : ChangePasswordState()
        data class Error(val message: String) : ChangePasswordState()
    }

    private val _changePasswordState = MutableStateFlow<ChangePasswordState>(ChangePasswordState.Idle)
    val changePasswordState: StateFlow<ChangePasswordState> = _changePasswordState.asStateFlow()

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<AuthEvent>()
    val events: SharedFlow<AuthEvent> = _events.asSharedFlow()

    private var sessionTimer: Job? = null
    private val sessionTimeout = 30 * 60 * 1000L

    init {
        checkKeepLoggedIn()
        observeAuthState()
        loadCurrentUser()
    }

    fun getGoogleSignInIntent(callback: (Intent?) -> Unit) {
        viewModelScope.launch {
            callback(getGoogleSignInIntentUseCase())
        }
    }

    fun handleGoogleSignInResult(data: Intent?, keepLoggedIn: Boolean = false) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val result = handleGoogleSignInResultUseCase(data)
                result.fold(
                    onSuccess = { user ->
                        _uiState.update {
                            it.copy(
                                user = user,
                                userId = user.uid,
                                isLoading = false,
                                isKeepLoggedIn = keepLoggedIn
                            )
                        }
                        sessionUseCase.saveUserSession(user.uid)
                        if (keepLoggedIn) {
                            sessionUseCase.saveKeepLoggedIn(true)
                            sessionTimer?.cancel()
                        } else {
                            startSessionTimer()
                        }
                        _events.emit(AuthEvent.NavigateToHome)
                    },
                    onFailure = { error ->
                        _uiState.update { it.copy(isLoading = false) }
                        emitError(AuthErrorType.LoginError, error.message ?: "Google sign in failed")
                    }
                )
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false) }
                emitError(AuthErrorType.LoginError, e.message ?: "Google sign in failed")
            }
        }
    }


    private fun startSessionTimer() {
        sessionTimer?.cancel()
        sessionTimer = viewModelScope.launch {
            delay(sessionTimeout)
            if (!uiState.value.isKeepLoggedIn) logout()
        }
    }


    fun loadCurrentUser() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val userId = getCurrentUserIdUseCase()
                _uiState.update { it.copy(userId = userId) }
            } catch (e: Exception) {
                emitError(AuthErrorType.FetchUserError, "Failed to load user: ${e.message}")
            } finally {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    private fun observeAuthState() {
        viewModelScope.launch {
            observeAuthStateUseCase()
                .filter { !_uiState.value.isLoggingOut }
                .collectLatest { user ->
                    _uiState.update {
                        it.copy(
                            user = user,
                            userId = user?.uid
                        )
                    }
                }
        }
    }

    private fun checkKeepLoggedIn() {
        viewModelScope.launch {
            sessionUseCase.isKeepLoggedIn().collect { keepLoggedIn ->
                _uiState.update { it.copy(isKeepLoggedIn = keepLoggedIn) }
                if (keepLoggedIn) checkUserSession()
            }
        }
    }

    fun login(email: String, password: String, keepLoggedIn: Boolean) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val result = loginUseCase(email, password)
                result.fold(
                    onSuccess = { user ->
                        _uiState.update {
                            it.copy(
                                user = user,
                                userId = user.uid,
                                isLoading = false,
                                isKeepLoggedIn = keepLoggedIn,
                                navigationEvent = AuthEvent.NavigateToHome
                            )
                        }
                        sessionUseCase.saveUserSession(user.uid)
                        if (keepLoggedIn) {
                            sessionUseCase.saveKeepLoggedIn(true)
                            sessionTimer?.cancel()
                        } else {
                            startSessionTimer()
                        }
                    },
                    onFailure = { error ->
                        _uiState.update { it.copy(isLoading = false) }
                        emitError(AuthErrorType.LoginError, error.message ?: "Login failed")
                    }
                )
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false) }
                emitError(AuthErrorType.LoginError, e.message ?: "Login failed")
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoggingOut = true) }

            try {
                delay(100)

                logoutUseCase()
                userPreferencesRepository.clearUserSession()
                sessionTimer?.cancel()

                delay(500)

                _uiState.update {
                    it.copy(
                        user = null,
                        userId = null,
                        isKeepLoggedIn = false,
                        isLoggingOut = false,
                        navigationEvent = AuthEvent.NavigateToLogin
                    )
                }

            } catch (e: Exception) {
                _uiState.update { it.copy(isLoggingOut = false) }
            }
        }
    }

    private fun checkUserSession() {
        viewModelScope.launch {
            sessionUseCase.getUserSession().collect { userId ->
                _uiState.update { it.copy(userId = userId) }
                if (userId != null) fetchUserDetails()
            }
        }
    }

    fun changePassword(currentPassword: String, newPassword: String) {
        viewModelScope.launch {
            _changePasswordState.value = ChangePasswordState.Loading
            try {
                val result = changePasswordUseCase(currentPassword, newPassword)
                result.fold(
                    onSuccess = {
                        _changePasswordState.value = ChangePasswordState.Success("Password changed successfully")
                    },
                    onFailure = { error ->
                        _changePasswordState.value = ChangePasswordState.Error(
                            error.message ?: "Failed to change password"
                        )
                    }
                )
            } catch (e: Exception) {
                _changePasswordState.value = ChangePasswordState.Error(
                    e.message ?: "An unexpected error occurred"
                )
            }
        }
    }

    private fun fetchUserDetails() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val result = fetchUserDetailsUseCase(_uiState.value.userId ?: "")
                result.fold(
                    onSuccess = { user ->
                        _uiState.update {
                            it.copy(
                                user = user,
                                userId = user.uid,
                                isLoading = false
                            )
                        }
                    },
                    onFailure = { error ->
                        emitError(AuthErrorType.FetchUserError, error.message ?: "Failed to fetch user details")
                    }
                )
            } finally {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    fun register(firstName: String, lastName: String, birthday: String, email: String, password: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val result = registerUseCase(firstName, lastName, birthday, email, password)
                result.fold(
                    onSuccess = {
                        _events.emit(AuthEvent.RegistrationSuccess)
                    },
                    onFailure = { error ->
                        emitError(AuthErrorType.RegisterError, error.message ?: "Registration failed")
                    }
                )
            } finally {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    fun resetPassword(email: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val result = resetPasswordUseCase(email)
                result.fold(
                    onSuccess = {
                        _events.emit(AuthEvent.ResetPasswordSuccess)
                    },
                    onFailure = { error ->
                        emitError(AuthErrorType.ResetPasswordError, error.message ?: "Failed to reset password")
                    }
                )
            } finally {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    private suspend fun emitError(type: AuthErrorType, message: String) {
        _events.emit(AuthEvent.Error(type, message))
    }

    sealed class AuthEvent {
        object NavigateToLogin : AuthEvent()
        object NavigateToHome : AuthEvent()
        data class Error(val type: AuthErrorType, val message: String) : AuthEvent()
        object RegistrationSuccess : AuthEvent()
        object ResetPasswordSuccess : AuthEvent()
    }
}