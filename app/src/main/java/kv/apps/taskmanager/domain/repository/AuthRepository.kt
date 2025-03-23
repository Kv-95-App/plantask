package kv.apps.taskmanager.domain.repository

import kv.apps.taskmanager.domain.model.User


interface AuthRepository {
    suspend fun login(email: String, password: String): Result<User>
    suspend fun register(
        firstName: String,
        lastName: String,
        birthday: String,
        email: String,
        password: String
    ): Result<Unit>
    suspend fun logout()
    suspend fun resetPassword(email: String): Result<Unit>
}
