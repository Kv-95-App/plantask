package kv.apps.taskmanager.domain.usecase.authUseCases

import kv.apps.taskmanager.domain.model.User
import kv.apps.taskmanager.domain.repository.AuthRepository
import java.util.regex.Pattern
import javax.inject.Inject


class LoginUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(email: String, password: String): Result<User> {
        return try {
            if (email.isEmpty()) {
                return Result.failure(Exception("Email cannot be empty"))
            }
            if (password.isEmpty()) {
                return Result.failure(Exception("Password cannot be empty"))
            }
            if (!isValidEmail(email)) {
                return Result.failure(Exception("Invalid email format"))
            }

            authRepository.login(email, password)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun isValidEmail(email: String): Boolean {
        val emailPattern = "^[A-Za-z0-9+_.-]+@(.+)$"
        val pattern = Pattern.compile(emailPattern)
        return pattern.matcher(email).matches()
    }
}
