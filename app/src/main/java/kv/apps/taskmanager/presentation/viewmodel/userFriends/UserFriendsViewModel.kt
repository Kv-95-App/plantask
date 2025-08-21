package kv.apps.taskmanager.presentation.viewmodel.userFriends

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kv.apps.taskmanager.domain.model.FriendRequest
import kv.apps.taskmanager.domain.model.FriendRequestStatus
import kv.apps.taskmanager.domain.usecase.userUseCases.AcceptFriendRequestUseCase
import kv.apps.taskmanager.domain.usecase.userUseCases.AddFriendUseCase
import kv.apps.taskmanager.domain.usecase.userUseCases.DeleteFriendUseCase
import kv.apps.taskmanager.domain.usecase.userUseCases.GetFriendsUseCase
import kv.apps.taskmanager.domain.usecase.userUseCases.GetPendingFriendRequestsUseCase
import kv.apps.taskmanager.domain.usecase.userUseCases.GetUserByEmailUseCase
import kv.apps.taskmanager.domain.usecase.userUseCases.GetUserByIdUseCase
import kv.apps.taskmanager.domain.usecase.userUseCases.RejectFriendRequestUseCase
import javax.inject.Inject

@HiltViewModel
class UserFriendsViewModel @Inject constructor(
    private val addFriendUseCase: AddFriendUseCase,
    private val getFriendsUseCase: GetFriendsUseCase,
    private val acceptFriendRequestUseCase: AcceptFriendRequestUseCase,
    private val rejectFriendRequestUseCase: RejectFriendRequestUseCase,
    private val getPendingFriendRequestsUseCase: GetPendingFriendRequestsUseCase,
    private val deleteFriendUseCase: DeleteFriendUseCase,
    private val getUserById: GetUserByIdUseCase,
    private val getUserByEmailUseCase: GetUserByEmailUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(UserFriendsUiState())
    val uiState: StateFlow<UserFriendsUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<UserFriendsEvent>()
    val events: SharedFlow<UserFriendsEvent> = _events.asSharedFlow()

    fun loadInitialData(userId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val friendsDeferred = async { getFriendsUseCase(userId) }
                val pendingRequestsDeferred = async { getPendingFriendRequestsUseCase(userId) }

                val friendsResult = friendsDeferred.await()
                val pendingRequestsResult = pendingRequestsDeferred.await()

                _uiState.update { it ->
                    it.copy(
                        friends = friendsResult.fold(
                            onSuccess = { Result.success(it) },
                            onFailure = { Result.failure(it) }
                        ),
                        pendingFriendRequests = pendingRequestsResult.fold(
                            onSuccess = { Result.success(it) },
                            onFailure = { Result.failure(it) }
                        ),
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        error = "Failed to load initial data: ${e.message}",
                        isLoading = false
                    )
                }
            }
        }
    }

    fun addFriend(currentUserId: String, friendEmail: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                addFriendUseCase(currentUserId, friendEmail)
                    .fold(
                        onSuccess = { requestId ->
                            val friendUser = getUserByEmailUseCase(friendEmail).getOrNull()

                            if (friendUser != null) {
                                val currentUser = getUserById(currentUserId).getOrNull()
                                val senderName = currentUser?.let {
                                    "${it.firstName} ${it.lastName}".takeIf { it.isNotBlank() } ?: it.email
                                } ?: "Someone"

                                val friendRequest = FriendRequest(
                                    requestId = requestId,
                                    fromUserId = currentUserId,
                                    toUserId = friendUser.uid,
                                    status = FriendRequestStatus.PENDING,
                                    timestamp = com.google.firebase.Timestamp.now()
                                )


                            }

                            _uiState.update {
                                it.copy(
                                    addFriendState = Result.success("Friend request sent"),
                                    error = null,
                                    isLoading = false
                                )
                            }
                            getFriends(currentUserId)
                            _events.emit(UserFriendsEvent.FriendAdded(friendEmail))
                        },
                        onFailure = { e ->
                            emitError(UserFriendsErrorType.AddFriendError, "Failed to add friend: ${e.message}")
                        }
                    )
            } finally {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    fun deleteFriend(currentUserId: String, friendId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                deleteFriendUseCase(currentUserId, friendId)
                    .fold(
                        onSuccess = {
                            _uiState.update {
                                it.copy(
                                    deleteFriendState = Result.success(Unit),
                                    error = null,
                                    isLoading = false
                                )
                            }
                            getFriends(currentUserId)
                            _events.emit(UserFriendsEvent.FriendDeleted(friendId))
                        },
                        onFailure = { e ->
                            emitError(UserFriendsErrorType.DeleteFriendError, "Failed to delete friend: ${e.message}")
                        }
                    )
            } finally {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    fun getFriends(userId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingFriends = true) }
            try {
                getFriendsUseCase(userId)
                    .fold(
                        onSuccess = { friends ->
                            _uiState.update {
                                it.copy(
                                    friends = Result.success(friends),
                                    isLoadingFriends = false
                                )
                            }
                        },
                        onFailure = { e ->
                            _uiState.update {
                                it.copy(
                                    friends = Result.failure(e),
                                    isLoadingFriends = false
                                )
                            }
                        }
                    )
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        friends = Result.failure(e),
                        isLoadingFriends = false
                    )
                }
            }
        }
    }

    fun getPendingFriendRequests(userId: String) {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isLoadingPendingRequests = true,
                    pendingFriendRequests = null
                )
            }
            try {
                getPendingFriendRequestsUseCase(userId)
                    .fold(
                        onSuccess = { pendingRequests ->
                            _uiState.update {
                                it.copy(
                                    pendingFriendRequests = Result.success(pendingRequests),
                                    error = null,
                                    isLoadingPendingRequests = false
                                )
                            }
                        },
                        onFailure = { e ->
                            emitError(UserFriendsErrorType.FetchPendingRequestsError, "Failed to load pending requests: ${e.message}")
                            _uiState.update { it.copy(isLoadingPendingRequests = false) }
                        }
                    )
            } catch (e: Exception) {
                emitError(UserFriendsErrorType.FetchPendingRequestsError, "Unexpected error: ${e.message}")
                _uiState.update { it.copy(isLoadingPendingRequests = false) }
            }
        }
    }

    fun acceptFriendRequest(currentUserId: String, senderEmail: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                acceptFriendRequestUseCase(currentUserId, senderEmail)
                    .fold(
                        onSuccess = {
                            _uiState.update {
                                it.copy(
                                    acceptFriendRequestState = Result.success(Unit),
                                    error = null,
                                    isLoading = false
                                )
                            }
                            getPendingFriendRequests(currentUserId)
                            getFriends(currentUserId)
                            _events.emit(UserFriendsEvent.FriendRequestAccepted(senderEmail))
                        },
                        onFailure = { e ->
                            emitError(UserFriendsErrorType.AcceptRequestError, "Failed to accept friend request: ${e.message}")
                        }
                    )
            } finally {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    fun rejectFriendRequest(currentUserId: String, senderEmail: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                rejectFriendRequestUseCase(currentUserId, senderEmail)
                    .fold(
                        onSuccess = {
                            _uiState.update {
                                it.copy(
                                    rejectFriendRequestState = Result.success(Unit),
                                    error = null,
                                    isLoading = false
                                )
                            }
                            getPendingFriendRequests(currentUserId)
                            _events.emit(UserFriendsEvent.FriendRequestRejected(senderEmail))
                        },
                        onFailure = { e ->
                            emitError(UserFriendsErrorType.RejectRequestError, "Failed to reject friend request: ${e.message}")
                        }
                    )
            } finally {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    fun resetState(stateType: UserFriendsStateType) {
        _uiState.update { currentState ->
            when (stateType) {
                UserFriendsStateType.ADD_FRIEND -> currentState.copy(
                    addFriendState = null,
                    isLoading = false,
                    error = null
                )
                UserFriendsStateType.DELETE_FRIEND -> currentState.copy(
                    deleteFriendState = null,
                    isLoading = false,
                    error = null
                )
                UserFriendsStateType.ACCEPT_REQUEST -> currentState.copy(
                    acceptFriendRequestState = null,
                    isLoading = false,
                    error = null
                )
                UserFriendsStateType.REJECT_REQUEST -> currentState.copy(
                    rejectFriendRequestState = null,
                    isLoading = false,
                    error = null
                )
                UserFriendsStateType.FRIENDS_LIST -> currentState.copy(
                    friends = null,
                    isLoadingFriends = false,
                    error = null
                )
                UserFriendsStateType.PENDING_REQUESTS -> currentState.copy(
                    pendingFriendRequests = null,
                    isLoadingPendingRequests = false,
                    error = null
                )
                UserFriendsStateType.ALL_STATES -> UserFriendsUiState()
            }
        }
    }
    fun fetchTargetUser(userId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingTargetUser = true) }
            try {
                getUserById(userId)
                    .fold(
                        onSuccess = { user ->
                            _uiState.update {
                                it.copy(
                                    targetUser = user,
                                    isLoadingTargetUser = false
                                )
                            }
                        },
                        onFailure = { e ->
                            _uiState.update {
                                it.copy(
                                    isLoadingTargetUser = false,
                                    error = "Failed to load user: ${e.message}"
                                )
                            }
                            _events.emit(UserFriendsEvent.Error(
                                UserFriendsErrorType.FETCH_USER_ERROR,
                                "Failed to load user profile"
                            ))
                        }
                    )
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoadingTargetUser = false,
                        error = "Unexpected error: ${e.message}"
                    )
                }
                _events.emit(UserFriendsEvent.Error(
                    UserFriendsErrorType.FETCH_USER_ERROR,
                    "Unexpected error loading user"
                ))
            }
        }
    }

    private suspend fun emitError(type: UserFriendsErrorType, message: String) {
        _events.emit(UserFriendsEvent.Error(type, message))
        _uiState.update { it.copy(error = message) }
        when (type) {
            UserFriendsErrorType.AddFriendError -> resetState(UserFriendsStateType.ADD_FRIEND)
            UserFriendsErrorType.DeleteFriendError -> resetState(UserFriendsStateType.DELETE_FRIEND)
            UserFriendsErrorType.AcceptRequestError -> resetState(UserFriendsStateType.ACCEPT_REQUEST)
            UserFriendsErrorType.RejectRequestError -> resetState(UserFriendsStateType.REJECT_REQUEST)
            UserFriendsErrorType.FetchFriendsError -> resetState(UserFriendsStateType.FRIENDS_LIST)
            UserFriendsErrorType.FetchPendingRequestsError -> resetState(UserFriendsStateType.PENDING_REQUESTS)
            UserFriendsErrorType.FETCH_USER_ERROR -> resetState(UserFriendsStateType.ALL_STATES)
        }
    }
}