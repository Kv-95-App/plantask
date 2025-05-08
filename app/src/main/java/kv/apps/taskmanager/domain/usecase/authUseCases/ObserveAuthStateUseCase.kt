package kv.apps.taskmanager.domain.usecase.authUseCases

import kotlinx.coroutines.flow.Flow
import kv.apps.taskmanager.domain.model.User
import kv.apps.taskmanager.domain.repository.AuthRepository
import javax.inject.Inject

class ObserveAuthStateUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(): Flow<User?> = authRepository.observeAuthState()
}