package kv.apps.taskmanager.presentation.viewmodel.userFriends

import kv.apps.taskmanager.domain.model.User

sealed class UserFriendsEvent {
    data class Error(val type: UserFriendsErrorType, val message: String) : UserFriendsEvent()
    data class FriendAdded(val message: String) : UserFriendsEvent()
    data class FriendDeleted(val friendId: String) : UserFriendsEvent()
    data class FriendRequestAccepted(val senderEmail: String) : UserFriendsEvent()
    data class FriendRequestRejected(val senderEmail: String) : UserFriendsEvent()
    data class UserFetched(val user: User) : UserFriendsEvent()
}