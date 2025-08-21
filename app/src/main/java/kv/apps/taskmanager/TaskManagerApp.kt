package kv.apps.taskmanager

import android.app.Application
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.google.firebase.Firebase
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.auth
import com.google.firebase.messaging.FirebaseMessaging
import dagger.hilt.android.HiltAndroidApp
import kv.apps.taskmanager.tokenManager.FcmTokenManager
import javax.inject.Inject

@HiltAndroidApp
class TaskManagerApp : Application() {
    @Inject lateinit var tokenManager: FcmTokenManager

    override fun onCreate() {
        super.onCreate()
        initializeFirebase()
        setupTokenMonitoring()
    }

    private fun initializeFirebase() {
        try {
            FirebaseApp.initializeApp(this)
            Firebase.auth.addAuthStateListener { auth ->
                auth.currentUser?.uid?.let { userId ->
                    tokenManager.refreshToken()
                }
            }
        } catch (e: Exception) {
            Log.e("TaskManagerApp", "Firebase initialization failed", e)
        }
    }

    private fun setupTokenMonitoring() {
        Handler(Looper.getMainLooper()).postDelayed({
            fetchAndSaveFcmToken()
        }, 1000)
    }

    private fun fetchAndSaveFcmToken() {
        Firebase.auth.currentUser?.uid?.let { userId ->
            FirebaseMessaging.getInstance().token
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        task.result?.let { token ->
                            tokenManager.saveToken(userId, token)
                        } ?: run {
                            Log.e("TaskManagerApp", "FCM token was null")
                        }
                    } else {
                        Log.e("TaskManagerApp", "FCM token fetch failed", task.exception)
                        // Consider retrying after a delay
                        scheduleRetry()
                    }
                }
        } ?: run {
            Log.d("TaskManagerApp", "No user logged in, skipping token save")
            scheduleTokenCheck()
        }
    }

    private fun scheduleRetry(delayMillis: Long = 5000) {
        Handler(Looper.getMainLooper()).postDelayed({
            fetchAndSaveFcmToken()
        }, delayMillis)
    }

    private fun scheduleTokenCheck(delayMillis: Long = 3000) {
        Handler(Looper.getMainLooper()).postDelayed({
            fetchAndSaveFcmToken()
        }, delayMillis)
    }
}