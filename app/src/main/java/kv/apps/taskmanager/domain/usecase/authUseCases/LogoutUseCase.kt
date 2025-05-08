package kv.apps.taskmanager.domain.usecase.authUseCases

import kv.apps.taskmanager.domain.repository.AuthRepository
import javax.inject.Inject

class LogoutUseCase @Inject constructor (
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke() {
        return authRepository.logout()
    }
}