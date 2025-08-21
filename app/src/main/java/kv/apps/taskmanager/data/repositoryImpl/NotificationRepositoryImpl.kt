package kv.apps.taskmanager.data.repositoryImpl

import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.functions.FirebaseFunctions
import kotlinx.coroutines.tasks.await
import kv.apps.taskmanager.domain.model.Notification
import kv.apps.taskmanager.domain.repository.NotificationRepository
import javax.inject.Inject

class NotificationRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val functions: FirebaseFunctions
) : NotificationRepository {

    override suspend fun sendFriendRequestNotification(
        recipientUserId: String,
        fromUserId: String,
        fromUserName: String,
        requestId: String
    ): Result<Unit> {
        return try {
            val data = hashMapOf(
                "recipientUserId" to recipientUserId,
                "fromUserId" to fromUserId,
                "fromUserName" to fromUserName,
                "requestId" to requestId
            )

            functions.getHttpsCallable("sendFriendRequestNotification")
                .call(data)
                .await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun sendProjectInvitationNotification(
        recipientUserId: String,
        projectId: String,
        projectName: String,
        fromUserId: String,
        fromUserName: String,
        invitationId: String
    ): Result<Unit> {
        return try {
            val data = hashMapOf(
                "recipientUserId" to recipientUserId,
                "projectId" to projectId,
                "projectName" to projectName,
                "fromUserId" to fromUserId,
                "fromUserName" to fromUserName,
                "invitationId" to invitationId
            )

            functions.getHttpsCallable("sendProjectInvitationNotification")
                .call(data)
                .await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun sendGeneralNotification(
        recipientUserId: String,
        title: String,
        body: String,
        imageUrl: String?
    ): Result<Unit> {
        return try {
            val data = hashMapOf(
                "userId" to recipientUserId,
                "title" to title,
                "body" to body
            )

            imageUrl?.let { data["imageUrl"] = it }

            functions.getHttpsCallable("sendGeneralNotification")
                .call(data)
                .await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getUnreadNotifications(userId: String): Result<List<Notification>> {
        return getNotifications(userId, unreadOnly = true)
    }

    override suspend fun getAllNotifications(userId: String): Result<List<Notification>> {
        return getNotifications(userId, unreadOnly = false)
    }

    private suspend fun getNotifications(userId: String, unreadOnly: Boolean): Result<List<Notification>> {
        return try {
            val query = firestore.collection("users")
                .document(userId)
                .collection("notifications")
                .let { collection ->
                    if (unreadOnly) collection.whereEqualTo("isRead", false)
                    else collection
                }
                .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING)

            val snapshot = query.get().await()

            val notifications = snapshot.documents.mapNotNull { doc ->
                val type = doc.getString("type") ?: return@mapNotNull null
                val timestamp = doc.getTimestamp("timestamp") ?: Timestamp.now()
                val isRead = doc.getBoolean("isRead") ?: false
                val recipientUserId = doc.getString("recipientUserId") ?: userId

                when (type) {
                    "friend_request" -> Notification.FriendRequestNotification(
                        id = doc.id,
                        recipientUserId = recipientUserId,
                        fromUserId = doc.getString("fromUserId") ?: "",
                        fromUserName = doc.getString("fromUserName") ?: "",
                        timestamp = timestamp,
                        isRead = isRead,
                        requestId = doc.getString("requestId")
                    )
                    "project_invitation" -> Notification.ProjectInvitationNotification(
                        id = doc.id,
                        recipientUserId = recipientUserId,
                        projectId = doc.getString("projectId") ?: "",
                        projectName = doc.getString("projectName") ?: "",
                        fromUserId = doc.getString("fromUserId") ?: "",
                        fromUserName = doc.getString("fromUserName") ?: "",
                        timestamp = timestamp,
                        isRead = isRead,
                        invitationId = doc.getString("invitationId")
                    )
                    "general" -> Notification.GeneralNotification(
                        id = doc.id,
                        recipientUserId = recipientUserId,
                        title = doc.getString("title") ?: "",
                        body = doc.getString("body") ?: "",
                        timestamp = timestamp,
                        isRead = isRead,
                        imageUrl = doc.getString("imageUrl")
                    )
                    else -> null
                }
            }

            Result.success(notifications)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun markAsRead(notificationId: String, userId: String): Result<Unit> {
        return try {
            firestore.collection("users")
                .document(userId)
                .collection("notifications")
                .document(notificationId)
                .update("isRead", true)
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun markMultipleAsRead(notificationIds: List<String>, userId: String): Result<Unit> {
        return try {
            val batch = firestore.batch()
            notificationIds.forEach { id ->
                val ref = firestore.collection("users")
                    .document(userId)
                    .collection("notifications")
                    .document(id)
                batch.update(ref, "isRead", true)
            }
            batch.commit().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteNotification(notificationId: String, userId: String): Result<Unit> {
        return try {
            firestore.collection("users")
                .document(userId)
                .collection("notifications")
                .document(notificationId)
                .delete()
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getNotificationPreferences(userId: String): Result<Map<String, Boolean>> {
        return try {
            val doc = firestore.collection("users")
                .document(userId)
                .collection("preferences")
                .document("notifications")
                .get()
                .await()

            val data = doc.data?.mapValues { (_, value) ->
                value as? Boolean ?: true
            } ?: defaultNotificationPreferences()

            Result.success(data)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateNotificationPreference(
        userId: String,
        type: String,
        enabled: Boolean
    ): Result<Unit> {
        return try {
            firestore.collection("users")
                .document(userId)
                .collection("preferences")
                .document("notifications")
                .set(mapOf(type to enabled), SetOptions.merge())
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun cleanupOldNotifications(userId: String, maxToKeep: Int): Result<Unit> {
        return try {
            val snapshot = firestore.collection("users")
                .document(userId)
                .collection("notifications")
                .orderBy("timestamp")
                .get()
                .await()

            if (snapshot.size() > maxToKeep) {
                val batch = firestore.batch()
                snapshot.documents
                    .take(snapshot.size() - maxToKeep)
                    .forEach { doc -> batch.delete(doc.reference) }
                batch.commit().await()
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun defaultNotificationPreferences(): Map<String, Boolean> {
        return mapOf(
            "friend_request" to true,
            "project_invitation" to true,
            "general" to true
        )
    }
}