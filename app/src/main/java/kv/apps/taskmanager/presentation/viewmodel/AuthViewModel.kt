package kv.apps.taskmanager.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kv.apps.taskmanager.domain.model.User
import kv.apps.taskmanager.domain.repository.UserPreferencesRepository
import kv.apps.taskmanager.domain.usecase.authUseCases.*
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
    private val logoutUseCase: LogoutUseCase
) : ViewModel() {

    private val _user = MutableStateFlow<User?>(null)
    val user: StateFlow<User?> = _user.asStateFlow()

    private val _userId = MutableStateFlow<String?>(null)
    val userId: StateFlow<String?> = _userId.asStateFlow()

    val currentUserId: StateFlow<String?> = _userId.asStateFlow()

    private val _isKeepLoggedIn = MutableStateFlow(false)
    val isKeepLoggedIn: StateFlow<Boolean> = _isKeepLoggedIn.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorType = MutableStateFlow<AuthErrorType?>(null)
    val errorType: StateFlow<AuthErrorType?> = _errorType.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    init {
        checkKeepLoggedIn()
        observeAuthState()
        loadCurrentUser()
    }

    private fun loadCurrentUser() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                _userId.value = getCurrentUserIdUseCase()
            } catch (e: Exception) {
                _errorMessage.value = "Failed to load user: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    private fun observeAuthState() {
        viewModelScope.launch {
            observeAuthStateUseCase().collectLatest { user ->
                _user.value = user
                _userId.value = user?.uid
            }
        }
    }

    private fun checkKeepLoggedIn() {
        viewModelScope.launch {
            sessionUseCase.isKeepLoggedIn().collect { keepLoggedIn ->
                _isKeepLoggedIn.value = keepLoggedIn
                if (keepLoggedIn) checkUserSession()
            }
        }
    }

    fun login(email: String, password: String, keepLoggedIn: Boolean) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorType.value = null
            _errorMessage.value = null

            val result = loginUseCase(email, password)
            result.fold(
                onSuccess = { user ->
                    _user.value = user
                    _userId.value = user.uid
                    userPreferencesRepository.saveUserSession(user.uid)
                    if (keepLoggedIn) userPreferencesRepository.saveKeepLoggedIn(true)
                },
                onFailure = { error ->
                    _errorType.value = AuthErrorType.LOGIN_ERROR
                    _errorMessage.value = error.message ?: "Login failed"
                }
            )
            _isLoading.value = false
        }
    }

    fun logout(navController: NavController? = null) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                logoutUseCase()
                _user.value = null
                _userId.value = null
                _isKeepLoggedIn.value = false
                _errorType.value = null
                _errorMessage.value = null

                navController?.navigate("login") {
                    popUpTo(0)
                }

                delay(500)

            } catch (e: Exception) {
                _errorType.value = AuthErrorType.LOGOUT_ERROR
                _errorMessage.value = "Logout failed: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }


    fun checkUserSession() {
        viewModelScope.launch {
            sessionUseCase.getUserSession().collect { userId ->
                _userId.value = userId
                if (userId != null) fetchUserDetails()
            }
        }
    }

    fun fetchUserDetails() {
        viewModelScope.launch {
            _isLoading.value = true
            _errorType.value = null
            _errorMessage.value = null

            val result = fetchUserDetailsUseCase()
            result.fold(
                onSuccess = { user ->
                    _user.value = user
                    _userId.value = user.uid
                },
                onFailure = { error ->
                    _errorType.value = AuthErrorType.FETCH_USER_ERROR
                    _errorMessage.value = error.message ?: "Failed to fetch user details"
                }
            )
            _isLoading.value = false
        }
    }

    // Rest of your functions remain exactly the same
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