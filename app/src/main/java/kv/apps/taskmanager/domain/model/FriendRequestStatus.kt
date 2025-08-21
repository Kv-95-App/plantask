package kv.apps.taskmanager.domain.model

import com.google.firebase.firestore.PropertyName

enum class FriendRequestStatus {
    @PropertyName("PENDING")
    PENDING,

    @PropertyName("ACCEPTED")
    ACCEPTED,

    @PropertyName("REJECTED")
    REJECTED
}