package kv.apps.taskmanager.domain.model

import androidx.annotation.Keep
import com.google.firebase.firestore.PropertyName

@Keep
data class FriendRequest(
    @PropertyName("requestId") val requestId: String = "",
    @PropertyName("fromUserId") val fromUserId: String = "",
    @PropertyName("toUserId") val toUserId: String = "",
    @PropertyName("status") val status: FriendRequestStatus = FriendRequestStatus.PENDING,
    @PropertyName("timestamp") val timestamp: com.google.firebase.Timestamp = com.google.firebase.Timestamp.now()
) {
    constructor() : this("", "", "", FriendRequestStatus.PENDING, com.google.firebase.Timestamp.now())
}