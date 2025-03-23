package kv.apps.taskmanager.domain.usecase.userUseCases

import kv.apps.taskmanager.domain.repository.UserRepository
import javax.inject.Inject

class RejectFriendRequestUseCase @Inject constructor(
    private val userRepository: UserRepository
) {

    suspend operator fun invoke(currentUserId: String, senderEmail: String): Result<Unit> {
        return userRepository.rejectFriendRequest(currentUserId, senderEmail)
    }
}
