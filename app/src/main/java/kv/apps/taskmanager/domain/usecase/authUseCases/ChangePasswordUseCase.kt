package kv.apps.taskmanager.domain.usecase.authUseCases

import kv.apps.taskmanager.domain.repository.AuthRepository
import javax.inject.Inject

class ChangePasswordUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {

    suspend operator fun invoke(currentPassword: String, newPassword: String): Result<Unit> {
        return authRepository.changePassword(currentPassword, newPassword)
    }
}