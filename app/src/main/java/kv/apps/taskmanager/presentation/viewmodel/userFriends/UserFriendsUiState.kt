package kv.apps.taskmanager.presentation.viewmodel.userFriends

import kv.apps.taskmanager.domain.model.Friend
import kv.apps.taskmanager.domain.model.User

data class UserFriendsUiState(
    val friends: Result<List<Friend>>? = null,
    val pendingFriendRequests: Result<List<User>>? = null,
    val addFriendState: Result<String>? = null,
    val deleteFriendState: Result<Unit>? = null,
    val acceptFriendRequestState: Result<Unit>? = null,
    val rejectFriendRequestState: Result<Unit>? = null,
    val isLoading: Boolean = false,
    val isLoadingFriends: Boolean = false,
    val isLoadingPendingRequests: Boolean = false,
    val error: String? = null
)