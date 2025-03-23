package kv.apps.taskmanager.data.repositoryImpl

import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.tasks.await
import kv.apps.taskmanager.domain.model.Friend
import kv.apps.taskmanager.domain.model.FriendRequest
import kv.apps.taskmanager.domain.model.FriendRequestStatus
import kv.apps.taskmanager.domain.model.User
import kv.apps.taskmanager.domain.repository.UserRepository
import javax.inject.Inject

class UserRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
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

    override suspend fun addFriend(currentUserId: String, friendEmail: String): Result<String> = withContext(Dispatchers.IO) {
        try {
            if (friendEmail.lowercase() == getCurrentUserEmail(currentUserId)) {
                return@withContext Result.failure(Exception("You cannot send a friend request to yourself"))
            }

            val querySnapshot = firestore.collection("users")
                .whereEqualTo("email", friendEmail.lowercase())
                .get()
                .await()

            if (querySnapshot.isEmpty) return@withContext Result.failure(Exception("No user found with this email"))

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
                timestamp = Timestamp.now(),
                requestId = ""
            )

            val documentReference = firestore.collection("users")
                .document(friendUserId)
                .collection("friendRequests")
                .add(friendRequest)
                .await()

            val requestId = documentReference.id
            documentReference.update("requestId", requestId).await()

            Result.success(requestId)
        } catch (e: Exception) {
            Result.failure(e)
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
            val snapshot = firestore.collection("users")
                .document(userId)
                .collection("friendRequests")
                .whereEqualTo("toUserId", userId)
                .whereEqualTo("status", FriendRequestStatus.PENDING.name)
                .get()
                .await()

            val pendingRequests = snapshot.documents.mapNotNull { doc ->
                val friendRequest = doc.toObject(FriendRequest::class.java)
                if (friendRequest != null) {
                    val senderDoc = firestore.collection("users")
                        .document(friendRequest.fromUserId)
                        .get()
                        .await()

                    senderDoc.toObject(User::class.java)
                } else {
                    null
                }
            }

            Result.success(pendingRequests)
        } catch (e: Exception) {
            Result.failure(e)
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

            val senderFullName = "${senderUserDoc.getString("firstName")} ${senderUserDoc.getString("lastName")}".trim()
            firestore.collection("users").document(currentUserId)
                .collection("friends")
                .document(senderUserId)
                .set(Friend(senderUserId, senderFullName, senderUserDoc.getString("email") ?: ""))
                .await()

            val currentUserFullName = "${currentUserDoc.getString("firstName")} ${currentUserDoc.getString("lastName")}".trim()
            firestore.collection("users").document(senderUserId)
                .collection("friends")
                .document(currentUserId)
                .set(Friend(currentUserId, currentUserFullName, currentUserDoc.getString("email") ?: ""))
                .await()

            firestore.collection("users").document(currentUserId)
                .collection("friendRequests")
                .document(requestDoc.id)
                .delete()
                .await()

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