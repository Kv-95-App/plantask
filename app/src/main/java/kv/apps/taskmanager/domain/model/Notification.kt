package kv.apps.taskmanager.domain.model

import com.google.firebase.Timestamp

sealed class Notification {
    abstract val id: String
    abstract val type: String
    abstract val timestamp: Timestamp
    abstract val isRead: Boolean
    abstract val recipientUserId: String

    data class FriendRequestNotification(
        override val id: String,
        override val recipientUserId: String,
        val fromUserId: String,
        val fromUserName: String,
        override val timestamp: Timestamp,
        override val isRead: Boolean = false,
        val requestId: String? = null
    ) : Notification() {
        override val type: String = "friend_request"
    }

    data class ProjectInvitationNotification(
        override val id: String,
        override val recipientUserId: String,
        val projectId: String,
        val projectName: String,
        val fromUserId: String,
        val fromUserName: String,
        override val timestamp: Timestamp,
        override val isRead: Boolean = false,
        val invitationId: String? = null
    ) : Notification() {
        override val type: String = "project_invitation"
    }

    data class GeneralNotification(
        override val id: String,
        override val recipientUserId: String,
        val title: String,
        val body: String,
        override val timestamp: Timestamp,
        override val isRead: Boolean = false,
        val imageUrl: String? = null
    ) : Notification() {
        override val type: String = "general"
    }
}