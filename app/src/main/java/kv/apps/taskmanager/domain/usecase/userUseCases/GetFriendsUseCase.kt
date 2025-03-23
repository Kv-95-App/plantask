package kv.apps.taskmanager.domain.usecase.userUseCases


import kv.apps.taskmanager.domain.model.Friend
import kv.apps.taskmanager.domain.model.User
import kv.apps.taskmanager.domain.repository.UserRepository
import javax.inject.Inject

class GetFriendsUseCase @Inject constructor(
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(userId: String): Result<List<Friend>> {
    return userRepository.getFriends(userId)
    }
}
