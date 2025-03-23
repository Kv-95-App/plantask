package kv.apps.taskmanager.domain.usecase.userUseCases

import kv.apps.taskmanager.domain.repository.UserRepository
import javax.inject.Inject

class DeleteFriendUseCase @Inject constructor(
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(currentUserId: String, friendId: String): Result<Unit> {
        return userRepository.deleteFriend(currentUserId, friendId)
    }
}