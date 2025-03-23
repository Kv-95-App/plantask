package kv.apps.taskmanager.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
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

    private val _friendsState = MutableStateFlow<Result<List<Friend>>?>(null)
    val friendsState: StateFlow<Result<List<Friend>>?> get() = _friendsState

    private val _addFriendState = MutableStateFlow<Result<String>?>(null)
    val addFriendState: StateFlow<Result<String>?> = _addFriendState.asStateFlow()

    private val _deleteFriendState = MutableStateFlow<Result<Unit>?>(null)
    val deleteFriendState: StateFlow<Result<Unit>?> = _deleteFriendState.asStateFlow()

    private val _acceptFriendRequestState = MutableStateFlow<Result<Unit>?>(null)
    val acceptFriendRequestState: StateFlow<Result<Unit>?> = _acceptFriendRequestState.asStateFlow()

    private val _rejectFriendRequestState = MutableStateFlow<Result<Unit>?>(null)
    val rejectFriendRequestState: StateFlow<Result<Unit>?> = _rejectFriendRequestState.asStateFlow()

    private val _pendingFriendRequestsState = MutableStateFlow<Result<List<User>>?>(null)
    val pendingFriendRequestsState: StateFlow<Result<List<User>>?> =
        _pendingFriendRequestsState.asStateFlow()

    private val _isLoadingFriends = MutableStateFlow(false)
    val isLoadingFriends: StateFlow<Boolean> get() = _isLoadingFriends

    fun addFriend(currentUserId: String, friendEmail: String) {
        viewModelScope.launch {
            val result = addFriendUseCase(currentUserId, friendEmail)
            _addFriendState.value = result

            if (result.isSuccess) {
                getFriends(currentUserId)
            }
        }
    }

    fun deleteFriend(currentUserId: String, friendId: String) {
        viewModelScope.launch {
            val result = deleteFriendUseCase(currentUserId, friendId)
            _deleteFriendState.value = result

            if (result.isSuccess) {
                getFriends(currentUserId)
            }
        }
    }

    fun getFriends(userId: String) {
        viewModelScope.launch {
            _isLoadingFriends.value = true
            try {
                val result = getFriendsUseCase(userId)
                _friendsState.value = result
            } catch (e: Exception) {
                _friendsState.value = Result.failure(e)
            } finally {
                _isLoadingFriends.value = false
            }
        }
    }

    fun acceptFriendRequest(currentUserId: String, senderEmail: String) {
        viewModelScope.launch {
            val result = acceptFriendRequestUseCase(currentUserId, senderEmail)
            _acceptFriendRequestState.value = result

            if (result.isSuccess) {
                getPendingFriendRequests(currentUserId)
                getFriends(currentUserId)
            }
        }
    }

    fun rejectFriendRequest(currentUserId: String, senderEmail: String) {
        viewModelScope.launch {
            val result = rejectFriendRequestUseCase(currentUserId, senderEmail)
            _rejectFriendRequestState.value = result

            if (result.isSuccess) {
                getPendingFriendRequests(currentUserId)
            }
        }
    }

    fun getPendingFriendRequests(userId: String) {
        viewModelScope.launch {
            try {
                val result = getPendingFriendRequestsUseCase(userId)
                _pendingFriendRequestsState.value = result
            } catch (e: Exception) {
                _pendingFriendRequestsState.value = Result.failure(e)
            }
        }
    }

    fun resetState(state: String) {
        when (state) {
            "addFriend" -> _addFriendState.value = null
            "deleteFriend" -> _deleteFriendState.value = null
            "acceptFriendRequest" -> _acceptFriendRequestState.value = null
            "rejectFriendRequest" -> _rejectFriendRequestState.value = null
        }
    }
}
