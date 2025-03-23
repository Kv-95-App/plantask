package kv.apps.taskmanager.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kv.apps.taskmanager.domain.model.User
import kv.apps.taskmanager.domain.repository.UserPreferencesRepository
import kv.apps.taskmanager.domain.usecase.authUseCases.LoginUseCase
import kv.apps.taskmanager.domain.usecase.authUseCases.RegisterUseCase
import kv.apps.taskmanager.domain.usecase.authUseCases.ResetPasswordUseCase
import kv.apps.taskmanager.domain.usecase.userUseCases.FetchUserDetailsUseCase
import kv.apps.taskmanager.domain.usecase.authUseCases.SessionUseCase
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val loginUseCase: LoginUseCase,
    private val registerUseCase: RegisterUseCase,
    private val fetchUserDetailsUseCase: FetchUserDetailsUseCase,
    private val resetPasswordUseCase: ResetPasswordUseCase,
    private val sessionUseCase: SessionUseCase,
    private val userPreferencesRepository: UserPreferencesRepository
) : ViewModel() {

    // State for the current user
    private val _user = MutableStateFlow<User?>(null)
    val user: StateFlow<User?> = _user.asStateFlow()

    // State for the current user ID
    private val _currentUserId = MutableStateFlow<String?>(null)
    val currentUserId: StateFlow<String?> = _currentUserId.asStateFlow()

    // State for "keep logged in" preference
    private val _isKeepLoggedIn = MutableStateFlow(false)
    val isKeepLoggedIn: StateFlow<Boolean> = _isKeepLoggedIn.asStateFlow()

    // State for loading
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    // State for error type
    private val _errorType = MutableStateFlow<AuthErrorType?>(null)
    val errorType: StateFlow<AuthErrorType?> = _errorType.asStateFlow()

    // State for error message
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    init {
        checkKeepLoggedIn()
        viewModelScope.launch {
            userPreferencesRepository.getUserSession().collect { userId ->
                _currentUserId.value = userId
            }
        }
    }

    // Check if "keep logged in" is enabled and validate the session
    private fun checkKeepLoggedIn() {
        viewModelScope.launch {
            sessionUseCase.isKeepLoggedIn().collect { keepLoggedIn ->
                _isKeepLoggedIn.value = keepLoggedIn
                if (keepLoggedIn) checkUserSession()
            }
        }
    }

    // Login with email and password
    fun login(email: String, password: String, keepLoggedIn: Boolean) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorType.value = null
            _errorMessage.value = null

            val result = loginUseCase(email, password)
            result.fold(
                onSuccess = { user ->
                    _user.value = user
                    _currentUserId.value = user.uid
                    userPreferencesRepository.saveUserSession(user.uid)
                    if (keepLoggedIn) userPreferencesRepository.saveKeepLoggedIn(true)
                    _isLoading.value = false
                },
                onFailure = { error ->
                    _errorType.value = AuthErrorType.LOGIN_ERROR
                    _errorMessage.value = error.message ?: "Login failed"
                    _isLoading.value = false
                }
            )
        }
    }

    // Logout the current user
    fun logout() {
        viewModelScope.launch {
            userPreferencesRepository.clearUserSession()
            _user.value = null
            _currentUserId.value = null
            _isLoading.value = false
            _errorType.value = null
            _errorMessage.value = null
        }
    }

    // Check the current user session
    fun checkUserSession() {
        viewModelScope.launch {
            sessionUseCase.getUserSession().collect { userId ->
                _currentUserId.value = userId
                if (userId != null) fetchUserDetails()
            }
        }
    }

    // Fetch details of the current user
    fun fetchUserDetails() {
        viewModelScope.launch {
            _isLoading.value = true
            _errorType.value = null
            _errorMessage.value = null

            val result = fetchUserDetailsUseCase()
            result.fold(
                onSuccess = { user ->
                    _user.value = user
                    _currentUserId.value = user.uid
                    _isLoading.value = false
                },
                onFailure = { error ->
                    _errorType.value = AuthErrorType.FETCH_USER_ERROR
                    _errorMessage.value = error.message ?: "Failed to fetch user details"
                    _isLoading.value = false
                }
            )
        }
    }

    // Register a new user
    fun register(firstName: String, lastName: String, birthday: String, email: String, password: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorType.value = null
            _errorMessage.value = null

            val result = registerUseCase(firstName, lastName, birthday, email, password)
            result.fold(
                onSuccess = { _isLoading.value = false },
                onFailure = { error ->
                    _errorType.value = AuthErrorType.REGISTER_ERROR
                    _errorMessage.value = error.message ?: "Registration failed"
                    _isLoading.value = false
                }
            )
        }
    }

    // Reset password for a user
    fun resetPassword(email: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorType.value = null
            _errorMessage.value = null

            val result = resetPasswordUseCase(email)
            result.fold(
                onSuccess = { _isLoading.value = false },
                onFailure = { error ->
                    _errorType.value = AuthErrorType.RESET_PASSWORD_ERROR
                    _errorMessage.value = error.message ?: "Failed to reset password"
                    _isLoading.value = false
                }
            )
        }
    }
}