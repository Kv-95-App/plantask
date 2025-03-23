package kv.apps.taskmanager.domain.usecase.userUseCases

import kv.apps.taskmanager.domain.model.User
import kv.apps.taskmanager.domain.repository.UserRepository
import javax.inject.Inject

class GetPendingFriendRequestsUseCase @Inject constructor(
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(userId: String): Result<List<User>> {
        return userRepository.getPendingFriendRequests(userId)
    }
}