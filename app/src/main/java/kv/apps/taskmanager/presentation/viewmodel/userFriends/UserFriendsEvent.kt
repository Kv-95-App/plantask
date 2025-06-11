package kv.apps.taskmanager.presentation.viewmodel.userFriends

sealed class UserFriendsEvent {
    data class Error(val type: UserFriendsErrorType, val message: String) : UserFriendsEvent()
    data class FriendAdded(val message: String) : UserFriendsEvent()
    data class FriendDeleted(val friendId: String) : UserFriendsEvent()
    data class FriendRequestAccepted(val senderEmail: String) : UserFriendsEvent()
    data class FriendRequestRejected(val senderEmail: String) : UserFriendsEvent()
}