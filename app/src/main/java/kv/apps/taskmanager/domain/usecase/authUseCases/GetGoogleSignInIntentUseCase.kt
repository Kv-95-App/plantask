package kv.apps.taskmanager.domain.usecase.authUseCases

import android.content.Intent
import kv.apps.taskmanager.domain.model.User
import kv.apps.taskmanager.domain.repository.AuthRepository
import javax.inject.Inject

class GetGoogleSignInIntentUseCase @Inject constructor(
    private val repository: AuthRepository
) {
    operator fun invoke(): Intent? = repository.getGoogleSignInIntent()
}

class HandleGoogleSignInResultUseCase @Inject constructor(
    private val repository: AuthRepository
) {
    suspend operator fun invoke(data: Intent?): Result<User> =
        repository.handleGoogleSignInResult(data)
}