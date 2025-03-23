package kv.apps.taskmanager.domain.repository

import kv.apps.taskmanager.domain.model.Friend
import kv.apps.taskmanager.domain.model.User

interface UserRepository {
    suspend fun getUserDetails(): Result<User>
    suspend fun saveUserDetails(user: User): Result<Unit>
    suspend fun getUserByEmail(email: String): Result<User?>
    suspend fun addFriend(currentUserId: String, friendEmail: String): Result<String>
    suspend fun getFriends(userId: String): Result<List<Friend>>
    suspend fun getPendingFriendRequests(userId: String): Result<List<User>>
    suspend fun acceptFriendRequest(currentUserId: String, senderEmail: String): Result<Unit>
    suspend fun rejectFriendRequest(currentUserId: String, senderEmail: String): Result<Unit>
    suspend fun deleteFriend(currentUserId: String, friendId: String): Result<Unit>
}