package kv.apps.taskmanager.domain.repository

import kv.apps.taskmanager.domain.model.Notification

interface NotificationRepository {
    suspend fun sendFriendRequestNotification(
        recipientUserId: String,
        fromUserId: String,
        fromUserName: String,
        requestId: String
    ): Result<Unit>

    suspend fun sendProjectInvitationNotification(
        recipientUserId: String,
        projectId: String,
        projectName: String,
        fromUserId: String,
        fromUserName: String,
        invitationId: String
    ): Result<Unit>

    suspend fun sendGeneralNotification(
        recipientUserId: String,
        title: String,
        body: String,
        imageUrl: String? = null
    ): Result<Unit>

    suspend fun getUnreadNotifications(userId: String): Result<List<Notification>>
    suspend fun getAllNotifications(userId: String): Result<List<Notification>>
    suspend fun markAsRead(notificationId: String, userId: String): Result<Unit>
    suspend fun markMultipleAsRead(notificationIds: List<String>, userId: String): Result<Unit>
    suspend fun deleteNotification(notificationId: String, userId: String): Result<Unit>

    suspend fun getNotificationPreferences(userId: String): Result<Map<String, Boolean>>
    suspend fun updateNotificationPreference(
        userId: String,
        type: String,
        enabled: Boolean
    ): Result<Unit>

    suspend fun cleanupOldNotifications(userId: String, maxToKeep: Int = 100): Result<Unit>
}