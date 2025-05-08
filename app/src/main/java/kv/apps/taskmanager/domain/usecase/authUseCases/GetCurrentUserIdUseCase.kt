package kv.apps.taskmanager.domain.usecase.authUseCases

import kv.apps.taskmanager.domain.repository.AuthRepository
import javax.inject.Inject

class GetCurrentUserIdUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {

    suspend operator fun invoke(): String? {
        return authRepository.getCurrentUserId()
    }
}
