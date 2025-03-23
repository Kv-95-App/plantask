package kv.apps.taskmanager.domain.usecase.authUseCases

import kotlinx.coroutines.flow.Flow
import kv.apps.taskmanager.domain.repository.UserPreferencesRepository
import javax.inject.Inject

class SessionUseCase @Inject constructor(
    private val userPreferencesRepository: UserPreferencesRepository
) {
    suspend fun saveUserSession(userId: String) {
        userPreferencesRepository.saveUserSession(userId)
    }

    fun getUserSession(): Flow<String?> {
        return userPreferencesRepository.getUserSession()
    }

    suspend fun clearUserSession() {
        userPreferencesRepository.clearUserSession()
    }

    // New functions
    suspend fun saveKeepLoggedIn(keepLoggedIn: Boolean) {
        userPreferencesRepository.saveKeepLoggedIn(keepLoggedIn)
    }

    fun isKeepLoggedIn(): Flow<Boolean> {
        return userPreferencesRepository.isKeepLoggedIn()
    }
}