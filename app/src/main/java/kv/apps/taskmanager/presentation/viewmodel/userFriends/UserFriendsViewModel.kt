package kv.apps.taskmanager.presentation.viewmodel.userFriends

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kv.apps.taskmanager.domain.model.Friend
import kv.apps.taskmanager.domain.model.User
import kv.apps.taskmanager.domain.usecase.userUseCases.AcceptFriendRequestUseCase
import kv.apps.taskmanager.domain.usecase.userUseCases.AddFriendUseCase
import kv.apps.taskmanager.domain.usecase.userUseCases.DeleteFriendUseCase
import kv.apps.taskmanager.domain.usecase.userUseCases.GetFriendsUseCase
import kv.apps.taskmanager.domain.usecase.userUseCases.GetPendingFriendRequestsUseCase
import kv.apps.taskmanager.domain.usecase.userUseCases.RejectFriendRequestUseCase
import javax.inject.Inject

@HiltViewModel
class UserFriendsViewModel @Inject constructor(
    private val addFriendUseCase: AddFriendUseCase,
    private val getFriendsUseCase: GetFriendsUseCase,
    private val acceptFriendRequestUseCase: AcceptFriendRequestUseCase,
    private val rejectFriendRequestUseCase: RejectFriendRequestUseCase,
    private val getPendingFriendRequestsUseCase: GetPendingFriendRequestsUseCase,
    private val deleteFriendUseCase: DeleteFriendUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(UserFriendsUiState())
    val uiState: StateFlow<UserFriendsUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<UserFriendsEvent>()
    val events: SharedFlow<UserFriendsEvent> = _events.asSharedFlow()

    fun addFriend(currentUserId: String, friendEmail: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                addFriendUseCase(currentUserId, friendEmail)
                    .fold(
                        onSuccess = { message ->
                            _uiState.update {
                                it.copy(
                                    addFriendState = Result.success(message),
                                    error = null
                                )
                            }
                            getFriends(currentUserId)
                            _events.emit(UserFriendsEvent.FriendAdded(message))
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
                                    error = null
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
                                    error = null,
                                    isLoadingFriends = false
                                )
                            }
                        },
                        onFailure = { e ->
                            emitError(UserFriendsErrorType.FetchFriendsError, "Failed to load friends: ${e.message}")
                            _uiState.update { it.copy(isLoadingFriends = false) }
                        }
                    )
            } catch (e: Exception) {
                emitError(UserFriendsErrorType.FetchFriendsError, "Unexpected error: ${e.message}")
                _uiState.update { it.copy(isLoadingFriends = false) }
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
                                    error = null
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
                                    error = null
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

    fun getPendingFriendRequests(userId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingPendingRequests = true) }
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

    fun resetState(stateType: UserFriendsStateType) {
        _uiState.update { currentState ->
            when (stateType) {
                UserFriendsStateType.ADD_FRIEND -> currentState.copy(addFriendState = null)
                UserFriendsStateType.DELETE_FRIEND -> currentState.copy(deleteFriendState = null)
                UserFriendsStateType.ACCEPT_REQUEST -> currentState.copy(acceptFriendRequestState = null)
                UserFriendsStateType.REJECT_REQUEST -> currentState.copy(rejectFriendRequestState = null)
            }
        }
    }

    private suspend fun emitError(type: UserFriendsErrorType, message: String) {
        _events.emit(UserFriendsEvent.Error(type, message))
        _uiState.update { it.copy(error = message) }
    }
}
