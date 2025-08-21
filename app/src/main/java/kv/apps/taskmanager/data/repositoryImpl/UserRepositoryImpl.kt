package kv.apps.taskmanager.data.repositoryImpl

import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.functions.functions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import kv.apps.taskmanager.data.remote.FirestoreListenerManager
import kv.apps.taskmanager.domain.model.Friend
import kv.apps.taskmanager.domain.model.FriendRequest
import kv.apps.taskmanager.domain.model.FriendRequestStatus
import kv.apps.taskmanager.domain.model.User
import kv.apps.taskmanager.domain.repository.UserRepository
import javax.inject.Inject

class UserRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth,
    private val listenerManager: FirestoreListenerManager
) : UserRepository {

    private val currentUserId: String?
        get() = auth.currentUser?.uid

    override suspend fun getUserDetails(): Result<User> = withContext(Dispatchers.IO) {
        val userId = currentUserId
        if (userId != null) {
            try {
                val doc = firestore.collection("users").document(userId).get().await()
                val user = doc.toObject(User::class.java)
                if (user != null) Result.success(user)
                else Result.failure(Exception("User not found"))
            } catch (e: Exception) {
                Result.failure(e)
            }
        } else {
            Result.failure(Exception("User not authenticated"))
        }
    }

    override suspend fun saveUserDetails(user: User): Result<Unit> = withContext(Dispatchers.IO) {
        val userId = currentUserId
        if (userId != null) {
            try {
                firestore.collection("users").document(userId).set(user).await()
                Result.success(Unit)
            } catch (e: Exception) {
                Result.failure(e)
            }
        } else {
            Result.failure(Exception("User not authenticated"))
        }
    }

    override suspend fun getUserByEmail(email: String): Result<User?> = withContext(Dispatchers.IO) {
        try {
            val querySnapshot = firestore.collection("users")
                .whereEqualTo("email", email.lowercase())
                .get()
                .await()

            if (querySnapshot.isEmpty) Result.success(null)
            else {
                val user = querySnapshot.documents[0].toObject(User::class.java)
                Result.success(user)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getUserById(userId: String): Result<User?> = withContext(Dispatchers.IO) {
        try {
            val doc = firestore.collection("users").document(userId).get().await()
            if (doc.exists()) {
                val user = doc.toObject(User::class.java)
                Result.success(user)
            } else {
                Result.success(null)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getInitialsById(userId: String): Result<String> = withContext(Dispatchers.IO) {
        try {
            val doc = firestore.collection("users").document(userId).get().await()
            if (doc.exists()) {
                val firstName = doc.getString("firstName") ?: ""
                val lastName = doc.getString("lastName") ?: ""
                val initials = "${firstName.firstOrNull()?.uppercase() ?: ""}${lastName.firstOrNull()?.uppercase() ?: ""}"
                Result.success(initials)
            } else {
                Result.failure(Exception("User not found"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun addFriend(currentUserId: String, friendEmail: String): Result<String> = withContext(Dispatchers.IO) {
        try {
            if (friendEmail.lowercase() == getCurrentUserEmail(currentUserId)) {
                return@withContext Result.failure(Exception("You cannot send a friend request to yourself"))
            }

            val querySnapshot = firestore.collection("users")
                .whereEqualTo("email", friendEmail.lowercase())
                .get()
                .await()

            if (querySnapshot.isEmpty) {
                return@withContext Result.failure(Exception("No user found with this email"))
            }

            val friendUserDoc = querySnapshot.documents[0]
            val friendUserId = friendUserDoc.id

            if (currentUserId == friendUserId) {
                return@withContext Result.failure(Exception("You cannot send a friend request to yourself"))
            }

            val isAlreadyFriend = firestore.collection("users")
                .document(currentUserId)
                .collection("friends")
                .whereEqualTo("friendId", friendUserId)
                .get()
                .await()
                .isEmpty
                .not()

            if (isAlreadyFriend) {
                return@withContext Result.failure(Exception("This user is already on your friend list"))
            }
            val pendingRequestQuery = firestore.collection("users")
                .document(friendUserId)
                .collection("friendRequests")
                .whereEqualTo("fromUserId", currentUserId)
                .whereEqualTo("status", FriendRequestStatus.PENDING.name)
                .get()
                .await()

            if (!pendingRequestQuery.isEmpty) {
                return@withContext Result.failure(Exception("You already have a pending friend request to this user"))
            }

            val friendRequest = FriendRequest(
                fromUserId = currentUserId,
                toUserId = friendUserId,
                status = FriendRequestStatus.PENDING,
                timestamp = com.google.firebase.Timestamp.now(),
                requestId = ""
            )

            val documentReference = firestore.collection("users")
                .document(friendUserId)
                .collection("friendRequests")
                .add(friendRequest)
                .await()

            val requestId = documentReference.id
            documentReference.update("requestId", requestId).await()

            val senderName = getSenderName(currentUserId)
            sendFriendRequestNotification(currentUserId, friendUserId, requestId, senderName)

            Result.success(requestId)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private suspend fun getSenderName(userId: String): String {
        return try {
            val userDoc = firestore.collection("users").document(userId).get().await()
            val firstName = userDoc.getString("firstName") ?: ""
            val lastName = userDoc.getString("lastName") ?: ""
            val fullName = "$firstName $lastName".trim()
            fullName.ifEmpty { userDoc.getString("email")?.split("@")?.first() ?: "A user" }
        } catch (_: Exception) {
            "A user"
        }
    }

    private suspend fun sendFriendRequestNotification(
        senderUserId: String,
        recipientUserId: String,
        requestId: String,
        senderName: String
    ) {
        try {
            val data = hashMapOf(
                "recipientUserId" to recipientUserId,
                "fromUserId" to senderUserId,
                "fromUserName" to senderName,
                "requestId" to requestId
            )

            Firebase.functions("europe-west1").getHttpsCallable("sendFriendRequestNotification")
                .call(data)
                .await()
        } catch (_: Exception) {
        }
    }

    private suspend fun getCurrentUserEmail(userId: String): String = withContext(Dispatchers.IO) {
        val userDoc = firestore.collection("users")
            .document(userId)
            .get()
            .await()
        userDoc.getString("email") ?: throw Exception("Current user email not found")
    }

    override suspend fun getFriends(userId: String): Result<List<Friend>> = withContext(Dispatchers.IO) {
        try {
            val snapshot = firestore.collection("users")
                .document(userId)
                .collection("friends")
                .get()
                .await()
            val friends = snapshot.documents.mapNotNull { doc ->
                doc.toObject(Friend::class.java)
            }
            Result.success(friends)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getPendingFriendRequests(userId: String): Result<List<User>> = withContext(Dispatchers.IO) {
        try {
            listenerManager.addActiveQuery("collectionGroup:friendRequests for user: $userId")
            val requestDocs = firestore.collectionGroup("friendRequests")
                .whereEqualTo("toUserId", userId)
                .whereEqualTo("status", "PENDING")
                .get()
                .await()
                .documents

            if (requestDocs.isEmpty()) {
                return@withContext Result.success(emptyList())
            }

            val pendingRequests = mutableListOf<User>()
            for (doc in requestDocs) {
                try {
                    val fromUserId = doc.getString("fromUserId")
                        ?: throw Exception("Missing fromUserId in request ${doc.id}")
                    val userDoc = firestore.collection("users")
                        .document(fromUserId)
                        .get()
                        .await()
                    if (!userDoc.exists()) {
                        continue
                    }
                    userDoc.toObject(User::class.java)?.let { user ->
                        pendingRequests.add(user.copy(uid = fromUserId))
                    }
                } catch (_: Exception) {
                    continue
                }
            }
            Result.success(pendingRequests)
        } catch (e: Exception) {
            Result.failure(Exception("Failed to load pending requests: ${e.message ?: "Unknown error"}"))
        }
    }

    override suspend fun acceptFriendRequest(currentUserId: String, senderEmail: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val senderUserQuery = firestore.collection("users")
                .whereEqualTo("email", senderEmail.lowercase())
                .get()
                .await()

            if (senderUserQuery.isEmpty) return@withContext Result.failure(Exception("Sender not found"))

            val senderUserDoc = senderUserQuery.documents[0]
            val senderUserId = senderUserDoc.id

            val currentUserDoc = firestore.collection("users").document(currentUserId).get().await()
            if (!currentUserDoc.exists()) return@withContext Result.failure(Exception("Current user not found"))

            val requestQuery = firestore.collection("users")
                .document(currentUserId)
                .collection("friendRequests")
                .whereEqualTo("fromUserId", senderUserId)
                .whereEqualTo("status", FriendRequestStatus.PENDING.name)
                .get()
                .await()

            if (requestQuery.isEmpty) return@withContext Result.failure(Exception("Friend request not found"))

            val requestDoc = requestQuery.documents[0]

            val senderFirstName = senderUserDoc.getString("firstName") ?: ""
            val senderLastName = senderUserDoc.getString("lastName") ?: ""
            val senderEmail = senderUserDoc.getString("email") ?: ""
            val senderDisplayName = if (senderFirstName.isNotEmpty() || senderLastName.isNotEmpty()) {
                "$senderFirstName $senderLastName".trim()
            } else {
                senderEmail.takeIf { it.isNotBlank() } ?: senderUserId.take(8)
            }

            val currentFirstName = currentUserDoc.getString("firstName") ?: ""
            val currentLastName = currentUserDoc.getString("lastName") ?: ""
            val currentEmail = currentUserDoc.getString("email") ?: ""
            val currentDisplayName = if (currentFirstName.isNotEmpty() || currentLastName.isNotEmpty()) {
                "$currentFirstName $currentLastName".trim()
            } else {
                currentEmail.takeIf { it.isNotBlank() } ?: currentUserId.take(8)
            }

            val batch = firestore.batch()

            val currentUserFriendRef = firestore.collection("users")
                .document(currentUserId)
                .collection("friends")
                .document(senderUserId)

            batch.set(currentUserFriendRef, mapOf(
                "friendId" to senderUserId,
                "displayName" to senderDisplayName,
                "email" to senderEmail,
                "addedAt" to FieldValue.serverTimestamp()
            ))

            val senderFriendRef = firestore.collection("users")
                .document(senderUserId)
                .collection("friends")
                .document(currentUserId)

            batch.set(senderFriendRef, mapOf(
                "friendId" to currentUserId,
                "displayName" to currentDisplayName,
                "email" to currentEmail,
                "addedAt" to FieldValue.serverTimestamp()
            ))

            val requestRef = firestore.collection("users")
                .document(currentUserId)
                .collection("friendRequests")
                .document(requestDoc.id)
            batch.delete(requestRef)

            batch.commit().await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(Exception("Error accepting friend request: ${e.localizedMessage}", e))
        }
    }

    override suspend fun rejectFriendRequest(currentUserId: String, senderEmail: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val senderUserQuery = firestore.collection("users")
                .whereEqualTo("email", senderEmail.lowercase())
                .get()
                .await()

            if (senderUserQuery.isEmpty) return@withContext Result.failure(Exception("Sender not found"))

            val senderUserDoc = senderUserQuery.documents[0]
            val senderUserId = senderUserDoc.id

            val requestQuery = firestore.collection("users")
                .document(currentUserId)
                .collection("friendRequests")
                .whereEqualTo("fromUserId", senderUserId)
                .whereEqualTo("status", FriendRequestStatus.PENDING.name)
                .get()
                .await()

            if (requestQuery.isEmpty) return@withContext Result.failure(Exception("Friend request not found"))

            val requestDoc = requestQuery.documents[0]

            firestore.collection("users").document(currentUserId)
                .collection("friendRequests")
                .document(requestDoc.id)
                .delete()
                .await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(Exception("Error rejecting friend request: ${e.localizedMessage}", e))
        }
    }

    override suspend fun deleteFriend(currentUserId: String, friendId: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            firestore.collection("users").document(currentUserId)
                .collection("friends")
                .document(friendId)
                .delete()
                .await()

            firestore.collection("users").document(friendId)
                .collection("friends")
                .document(currentUserId)
                .delete()
                .await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(Exception("Error deleting friend: ${e.localizedMessage}", e))
        }
    }
}