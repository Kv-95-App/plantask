package kv.apps.taskmanager.data.repositoryImpl

import android.content.Intent
import android.util.Log
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.snapshots
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import kv.apps.taskmanager.data.remote.AuthRemoteDataSource
import kv.apps.taskmanager.data.remote.GoogleSignInHelper
import kv.apps.taskmanager.domain.model.User
import kv.apps.taskmanager.domain.repository.AuthRepository
import javax.inject.Inject

class AuthRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val authRemoteDataSource: AuthRemoteDataSource,
    private val googleSignInHelper: GoogleSignInHelper
) : AuthRepository {

    override fun getGoogleSignInIntent(): Intent? {
        val intent = googleSignInHelper.getSignInIntent()
        Log.d("AuthRepository", "Getting Google Sign-In intent: ${intent != null}")
        return intent
    }

    override suspend fun handleGoogleSignInResult(data: Intent?): Result<User> {
        return withContext(Dispatchers.IO) {
            try {
                val account = googleSignInHelper.handleSignInResult(data)
                    ?: return@withContext Result.failure(Exception("Google sign in failed - no account"))

                if (!googleSignInHelper.firebaseAuthWithGoogle(account)) {
                    return@withContext Result.failure(Exception("Firebase authentication failed"))
                }

                val firebaseUser = authRemoteDataSource.firebaseAuth.currentUser
                    ?: return@withContext Result.failure(Exception("No authenticated user"))

                val fcmToken = try {
                    FirebaseMessaging.getInstance().token.await()
                } catch (e: Exception) {
                    Log.w("AuthRepository", "FCM token fetch failed", e)
                    null
                }

                val user = User(
                    uid = firebaseUser.uid,
                    email = account.email ?: firebaseUser.email ?: "",
                    firstName = account.givenName ?: firebaseUser.displayName?.split(" ")?.firstOrNull() ?: "User",
                    lastName = account.familyName ?: firebaseUser.displayName?.split(" ")?.lastOrNull() ?: "",
                    birthday = null,
                    fcmToken = fcmToken
                )

                // Include createdAt timestamp and all necessary fields
                firestore.collection("users").document(user.uid)
                    .set(mapOf(
                        "uid" to user.uid,
                        "email" to user.email,
                        "firstName" to user.firstName,
                        "lastName" to user.lastName,
                        "birthday" to user.birthday,
                        "fcmToken" to user.fcmToken,
                        "createdAt" to FieldValue.serverTimestamp() // ADD THIS
                    ), SetOptions.merge())
                    .await()

                return@withContext Result.success(user)
            } catch (e: Exception) {
                Log.e("AuthRepository", "Google sign in failed", e)
                Result.failure(Exception("Google sign in failed: ${e.message}"))
            }
        }
    }

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

    override suspend fun changePassword(currentPassword: String, newPassword: String): Result<Unit> = try {
        authRemoteDataSource.changePassword(currentPassword, newPassword)
        Result.success(Unit)
    } catch (_: FirebaseAuthInvalidUserException) {
        Result.failure(Exception("No account with this email"))
    } catch (e: Exception) {
        Log.e("AuthRepository", "Change password error", e)
        Result.failure(Exception("Failed to change password: ${e.message}"))
    }

    override suspend fun register(
        firstName: String,
        lastName: String,
        birthday: String,
        email: String,
        password: String
    ): Result<User> = try {
        authRemoteDataSource
            .signUpWithEmailAndPassword(email, password)
        val firebaseUser = authRemoteDataSource.firebaseAuth.currentUser
            ?: return Result.failure(Exception("Registration failed"))

        val user = User(
            uid = firebaseUser.uid,
            email = email.lowercase(),
            firstName = firstName,
            lastName = lastName,
            birthday = birthday
        )
        firestore
            .collection("users")
            .document(user.uid)
            .set(user)
            .await()

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

    override suspend fun resetPassword(email: String): Result<Unit> = try {
        authRemoteDataSource.resetPassword(email)
        Result.success(Unit)
    } catch (_: FirebaseAuthInvalidUserException) {
        Result.failure(Exception("No account with this email"))
    } catch (e: Exception) {
        Log.e("AuthRepository", "Password reset error", e)
        Result.failure(Exception("Failed to reset password"))
    }

    override suspend fun logout() {
        try {
            authRemoteDataSource.logout()
        } catch (e: Exception) {
            Log.e("AuthRepository", "Logout error", e)
            throw Exception("Logout failed: ${e.message}")
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    override suspend fun observeAuthState(): Flow<User?> = callbackFlow {
        val auth = com.google.firebase.auth.FirebaseAuth.getInstance()
        val listener = com.google.firebase.auth.FirebaseAuth.AuthStateListener { firebaseAuth ->
            trySend(firebaseAuth.currentUser?.uid)
        }
        auth.addAuthStateListener(listener)

        awaitClose {
            auth.removeAuthStateListener(listener)
        }
    }.flatMapLatest { userId ->
        if (userId != null) {
            firestore.collection("users").document(userId).snapshots()
                .map { document ->
                    document.toObject(User::class.java)?.copy(uid = userId)
                }
        } else {
            flowOf(null)
        }
    }
}