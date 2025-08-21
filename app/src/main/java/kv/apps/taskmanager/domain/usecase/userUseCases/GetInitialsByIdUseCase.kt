package kv.apps.taskmanager.domain.usecase.userUseCases

import kv.apps.taskmanager.domain.repository.UserRepository
import javax.inject.Inject

class GetInitialsByIdUseCase @Inject constructor(
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(userId: String): Result<String> {
        return userRepository.getInitialsById(userId)
    }
}