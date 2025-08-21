package kv.apps.taskmanager.domain.usecase.userUseCases

import kv.apps.taskmanager.domain.model.User
import kv.apps.taskmanager.domain.repository.UserRepository
import javax.inject.Inject

class GetUserByEmailUseCase @Inject constructor(
    private val repository: UserRepository
) {
    suspend operator fun invoke(email: String): Result<User?> {
        return repository.getUserByEmail(email)
    }
}