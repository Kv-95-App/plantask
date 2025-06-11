package kv.apps.taskmanager.data.remote

import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import javax.inject.Inject

class AuthRemoteDataSource @Inject constructor(
    val firebaseAuth: FirebaseAuth
) {

    suspend fun signUpWithEmailAndPassword(email: String, password: String) {
        firebaseAuth.createUserWithEmailAndPassword(email, password).await()
    }

    suspend fun signInWithEmailAndPassword(email: String, password: String) {
        firebaseAuth.signInWithEmailAndPassword(email, password).await()
    }

    suspend fun resetPassword(email: String) {
        firebaseAuth.sendPasswordResetEmail(email).await()
    }

    suspend fun logout() {
        withContext(Dispatchers.IO) {
            firebaseAuth.signOut()
            delay(50)
        }
    }
}