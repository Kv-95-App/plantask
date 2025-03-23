package kv.apps.taskmanager.domain.usecase.userUseCases

import kv.apps.taskmanager.domain.repository.UserRepository
import javax.inject.Inject

class AcceptFriendRequestUseCase @Inject constructor(
    private val userRepository: UserRepository
) {

    suspend operator fun invoke(currentUserId: String, senderEmail: String): Result<Unit> {
        return userRepository.acceptFriendRequest(currentUserId, senderEmail)
    }
}
