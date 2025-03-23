package kv.apps.taskmanager.domain.usecase.authUseCases

import android.util.Log
import android.util.Patterns
import kv.apps.taskmanager.domain.repository.AuthRepository
import javax.inject.Inject

class RegisterUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(
        firstName: String,
        lastName: String,
        birthday: String,
        email: String,
        password: String
    ): Result<Unit> {
        return try {
            validateInputs(firstName, lastName, birthday, email, password)

            authRepository.register(firstName, lastName, birthday, email, password)

            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("RegisterUseCase", "Registration failed: ${e.message}", e)
            Result.failure(e)
        }
    }

    private fun validateInputs(
        firstName: String,
        lastName: String,
        birthday: String,
        email: String,
        password: String
    ) {
        if (firstName.isBlank() || lastName.isBlank() || birthday.isBlank() || email.isBlank() || password.isBlank()) {
            throw IllegalArgumentException("All fields are required")
        }

        if (!isValidEmail(email)) {
            throw IllegalArgumentException("Invalid email format")
        }

        if (password.length < 6) {
            throw IllegalArgumentException("Password must be at least 6 characters")
        }
    }

    private fun isValidEmail(email: String): Boolean {
        return Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }
}