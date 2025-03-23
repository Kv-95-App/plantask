package kv.apps.taskmanager.domain.model

import com.google.firebase.Timestamp

data class FriendRequest(
    val fromUserId: String = "",
    val toUserId: String = "",
    val status: FriendRequestStatus = FriendRequestStatus.PENDING,
    val timestamp: Timestamp = Timestamp.now(),
    val requestId: String = ""
)