package kv.apps.taskmanager.data.repositoryImpl

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.tasks.await
import kv.apps.taskmanager.data.remote.AuthRemoteDataSource
import kv.apps.taskmanager.domain.model.User
import kv.apps.taskmanager.domain.repository.AuthRepository
import javax.inject.Inject
import kotlinx.coroutines.withContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map

class AuthRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val authRemoteDataSource: AuthRemoteDataSource
) : AuthRepository {

    override suspend fun getCurrentUserId(): String? = withContext(Dispatchers.IO) {
        try {
            authRemoteDataSource.firebaseAuth.currentUser?.uid
        } catch (e: Exception) {
            Log.e("AuthRepository", "Failed to get current user UID", e)
            null
        }
    }


    override suspend fun login(email: String, password: String): Result<User> {
        return withContext(Dispatchers.IO) {
            try {
                authRemoteDataSource.signInWithEmailAndPassword(email, password)

                val firebaseUser = authRemoteDataSource.firebaseAuth.currentUser
                    ?: return@withContext Result.failure(Exception("Authentication failed"))

                val userDoc = firestore.collection("users").document(firebaseUser.uid).get().await()

                if (!userDoc.exists()) {
                    return@withContext Result.failure(Exception("User profile not found"))
                }

                userDoc.toObject(User::class.java)?.let { user ->
                    Result.success(user.copy(uid = firebaseUser.uid))
                } ?: Result.failure(Exception("Invalid user data format"))

            } catch (_: FirebaseAuthInvalidUserException) {
                Result.failure(Exception("Account not found"))
            } catch (_: FirebaseAuthInvalidCredentialsException) {
                Result.failure(Exception("Invalid email or password"))
            } catch (e: Exception) {
                Log.e("AuthRepository", "Login error", e)
                Result.failure(Exception("Login failed: ${e.message}"))
            }
        }
    }

    override suspend fun register(
        firstName: String,
        lastName: String,
        birthday: String,
        email: String,
        password: String
    ): Result<User> {
        return withContext(Dispatchers.IO) {
            try {
                authRemoteDataSource.signUpWithEmailAndPassword(email, password)
                val firebaseUser = authRemoteDataSource.firebaseAuth.currentUser
                    ?: return@withContext Result.failure(Exception("Registration failed"))

                val user = User(
                    uid = firebaseUser.uid,
                    email = email,
                    firstName = firstName,
                    lastName = lastName,
                    birthday = birthday
                )

                firestore.collection("users").document(user.uid).set(user).await()

                Result.success(user)

            } catch (_: FirebaseAuthUserCollisionException) {
                Result.failure(Exception("Email already in use"))
            } catch (e: Exception) {
                Log.e("AuthRepository", "Registration error", e)
                try {
                    authRemoteDataSource.firebaseAuth.currentUser?.delete()?.await()
                } catch (deleteError: Exception) {
                    Log.e("AuthRepository", "Failed to clean up user", deleteError)
                }
                Result.failure(Exception("Registration failed: ${e.message}"))
            }
        }
    }

    override suspend fun resetPassword(email: String): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                authRemoteDataSource.resetPassword(email)
                Result.success(Unit)
            } catch (_: FirebaseAuthInvalidUserException) {
                Result.failure(Exception("No account with this email"))
            } catch (e: Exception) {
                Log.e("AuthRepository", "Password reset error", e)
                Result.failure(Exception("Failed to reset password"))
            }
        }
    }

    override suspend fun logout() {
        withContext(Dispatchers.IO) {
            try {
                authRemoteDataSource.logout()

                delay(1000)
            } catch (e: Exception) {
                Log.e("AuthRepository", "Logout error", e)
                throw e
            }
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    override suspend fun observeAuthState(): Flow<User?> = callbackFlow {
        val listener = FirebaseAuth.AuthStateListener { auth ->
            trySend(auth.currentUser?.uid)
        }
        authRemoteDataSource.firebaseAuth.addAuthStateListener(listener)

        awaitClose {
            authRemoteDataSource.firebaseAuth.removeAuthStateListener(listener)
        }
    }.flatMapLatest { userId ->
        if (userId != null) {
            firestore.collection("users").document(userId).snapshots()
                .map { it.toObject(User::class.java)?.copy(uid = userId) }
        } else {
            flowOf(null)
        }
    }
}