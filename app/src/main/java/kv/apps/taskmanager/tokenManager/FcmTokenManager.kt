package kv.apps.taskmanager.tokenManager

import android.content.Context
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.messaging.FirebaseMessaging
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FcmTokenManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth,
    private val coroutineScope: CoroutineScope
) {
    companion object {
        private const val TAG = "FcmTokenManager"
        private const val FCM_TOKEN_FIELD = "fcmToken"
        private const val FCM_UPDATED_FIELD = "fcmTokenUpdated"
    }

    fun saveToken(userId: String, token: String) {
        coroutineScope.launch {
            try {
                firestore.collection("users").document(userId)
                    .set(
                        mapOf(
                            FCM_TOKEN_FIELD to token,
                            FCM_UPDATED_FIELD to FieldValue.serverTimestamp()
                        ),
                        SetOptions.merge()
                    )
                    .await()
                Log.d(TAG, "Token saved for user $userId")
            } catch (e: Exception) {
                Log.e(TAG, "Failed to save FCM token", e)
            }
        }
    }

    suspend fun getCurrentToken(userId: String): String? {
        return try {
            firestore.collection("users")
                .document(userId)
                .get()
                .await()
                .getString(FCM_TOKEN_FIELD)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to fetch FCM token", e)
            null
        }
    }

    fun refreshToken() {
        coroutineScope.launch {
            try {
                auth.currentUser?.uid?.let { userId ->
                    val token = FirebaseMessaging.getInstance().token.await()
                    saveToken(userId, token)
                    Log.d(TAG, "Token refreshed for user $userId")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Token refresh failed", e)
            }
        }
    }
}