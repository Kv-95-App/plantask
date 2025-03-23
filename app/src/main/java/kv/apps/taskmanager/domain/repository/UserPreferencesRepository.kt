package kv.apps.taskmanager.domain.repository

import kotlinx.coroutines.flow.Flow

interface UserPreferencesRepository {
    suspend fun saveUserSession(userId: String)
    fun getUserSession(): Flow<String?>
    suspend fun clearUserSession()
    suspend fun saveKeepLoggedIn(keepLoggedIn: Boolean)
    fun isKeepLoggedIn(): Flow<Boolean>
}
