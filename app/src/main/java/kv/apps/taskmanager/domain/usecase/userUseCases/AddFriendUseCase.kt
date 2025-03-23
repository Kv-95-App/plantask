package kv.apps.taskmanager.domain.usecase.userUseCases

import kv.apps.taskmanager.domain.repository.UserRepository
import javax.inject.Inject

class AddFriendUseCase @Inject constructor(
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(currentUserId: String, friendEmail: String): Result<String> {
        return userRepository.addFriend(currentUserId, friendEmail)
    }
}