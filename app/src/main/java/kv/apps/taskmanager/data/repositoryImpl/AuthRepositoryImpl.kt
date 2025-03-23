package kv.apps.taskmanager.data.repositoryImpl

import android.util.Log
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import kv.apps.taskmanager.data.remote.AuthRemoteDataSource
import kv.apps.taskmanager.domain.model.User
import kv.apps.taskmanager.domain.repository.AuthRepository
import javax.inject.Inject
import kotlinx.coroutines.withContext
import kotlinx.coroutines.Dispatchers

class AuthRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val authRemoteDataSource: AuthRemoteDataSource
) : AuthRepository {

    override suspend fun login(email: String, password: String): Result<User> {
        return withContext(Dispatchers.IO) {
            try {
                authRemoteDataSource.signInWithEmailAndPassword(email, password)
                val firebaseUser = authRemoteDataSource.firebaseAuth.currentUser
                    ?: return@withContext Result.failure(Exception("User not found in Firebase Auth"))
                val userSnapshot = firestore.collection("users").document(firebaseUser.uid).get().await()
                val userData = userSnapshot.toObject(User::class.java)
                userData?.let {
                    Result.success(it)
                } ?: run {
                    Result.failure(Exception("User details not found in Firestore"))
                }
            } catch (e: FirebaseAuthInvalidCredentialsException) {
                Result.failure(Exception("Invalid email or password"))
            } catch (e: Exception) {
                Log.e("AuthRepositoryImpl", "Login failed: ${e.message}", e)
                Result.failure(e)
            }
        }
    }

    override suspend fun register(
        firstName: String,
        lastName: String,
        birthday: String,
        email: String,
        password: String
    ): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                authRemoteDataSource.signUpWithEmailAndPassword(email, password)
                val firebaseUser = authRemoteDataSource.firebaseAuth.currentUser
                    ?: return@withContext Result.failure(Exception("User registration failed"))
                val user = User(
                    uid = firebaseUser.uid,
                    email = firebaseUser.email ?: "unknown",
                    firstName = firstName,
                    lastName = lastName,
                    birthday = birthday
                )
                firestore.collection("users").document(user.uid).set(user).await()
                Result.success(Unit)
            } catch (e: FirebaseAuthUserCollisionException) {
                Result.failure(Exception("Email already in use"))
            } catch (e: Exception) {
                Log.e("AuthRepositoryImpl", "Registration failed: ${e.message}", e)
                Result.failure(e)
            }
        }
    }

    override suspend fun resetPassword(email: String): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                authRemoteDataSource.resetPassword(email)
                Result.success(Unit)
            } catch (e: FirebaseAuthInvalidUserException) {
                Result.failure(Exception("No user found with this email"))
            } catch (e: Exception) {
                Log.e("AuthRepositoryImpl", "Password reset failed: ${e.message}", e)
                Result.failure(e)
            }
        }
    }

    override suspend fun logout() {
        withContext(Dispatchers.IO) {
            authRemoteDataSource.signOut()
        }
    }
}