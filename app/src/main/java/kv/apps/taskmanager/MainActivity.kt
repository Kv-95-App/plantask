package kv.apps.taskmanager

import android.Manifest
import android.app.Activity
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.messaging.FirebaseMessaging
import dagger.hilt.android.AndroidEntryPoint
import kv.apps.taskmanager.presentation.navigation.NavGraph
import kv.apps.taskmanager.presentation.navigation.Screen
import kv.apps.taskmanager.presentation.viewmodel.auth.AuthViewModel
import kv.apps.taskmanager.theme.TaskManagerTheme
import kv.apps.taskmanager.theme.backgroundColor
import kv.apps.taskmanager.theme.onGoingCardColor
import kv.apps.taskmanager.tokenManager.FcmTokenManager
import javax.inject.Inject
import android.os.Handler
import androidx.activity.result.ActivityResultLauncher
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    companion object {
        private const val TAG = "MainActivity"
        const val NOTIFICATION_CHANNEL_ID = "default_channel_id"
        const val NOTIFICATION_CHANNEL_NAME = "General Notifications"
    }

    private lateinit var requestPermissionLauncher: ActivityResultLauncher<String>
    private lateinit var googleSignInLauncher: ActivityResultLauncher<Intent>

    @Inject
    lateinit var tokenManager: FcmTokenManager

    private val authViewModel: AuthViewModel by viewModels()
    private var pendingIntent: Intent? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        pendingIntent = intent

        requestPermissionLauncher = registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted ->
            handleNotificationPermissionResult(isGranted)
        }

        googleSignInLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            authViewModel.handleGoogleSignInResult(result.data, keepLoggedIn = false)
        }

        createNotificationChannel()
        WindowCompat.setDecorFitsSystemWindows(window, false)

        Firebase.auth.addAuthStateListener { auth ->
            auth.currentUser?.let { user ->
                checkAndRequestNotificationPermission()
            }
        }

        setContent {
            TaskManagerTheme {
                val navController = rememberNavController()
                val view = LocalView.current
                var hasHandledInitialIntent by remember { mutableStateOf(false) }

                LaunchedEffect(hasHandledInitialIntent) {
                    if (!hasHandledInitialIntent) {
                        kotlinx.coroutines.delay(500)
                        handleNotificationIntent(pendingIntent, navController, isInitialIntent = true)
                        hasHandledInitialIntent = true
                        pendingIntent = null
                    }
                }

                LaunchedEffect(pendingIntent) {
                    pendingIntent?.let { intent ->
                        if (hasHandledInitialIntent) {
                            handleNotificationIntent(intent, navController, isInitialIntent = false)
                            pendingIntent = null
                        }
                    }
                }

                SideEffect {
                    configureSystemBars(view)
                }

                MaterialTheme {
                    Box(modifier = Modifier.fillMaxSize()) {
                        NavGraph(
                            navController = navController,
                            onGoogleSignInClicked = {
                                authViewModel.getGoogleSignInIntent { signInIntent ->
                                    signInIntent?.let {
                                        googleSignInLauncher.launch(it)
                                    }
                                }
                            }
                        )
                    }
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        pendingIntent = intent
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                NOTIFICATION_CHANNEL_ID,
                NOTIFICATION_CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "All app notifications"
                enableLights(true)
                lightColor = Color.RED
                enableVibration(true)
            }

            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun handleNotificationPermissionResult(isGranted: Boolean) {
        if (isGranted) {
            Firebase.auth.currentUser?.uid?.let { userId ->
                FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
                    try {
                        if (task.isSuccessful) {
                            task.result?.let { token ->
                                tokenManager.saveToken(userId, token)
                                Log.d(TAG, "FCM token retrieved and saved successfully")
                            }
                        } else {
                            val exception = task.exception
                            Log.w(TAG, "FCM token retrieval failed after permission grant", exception)
                            Handler(Looper.getMainLooper()).postDelayed({
                                checkAndRequestNotificationPermission()
                            }, 5000L)
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "Unexpected error during FCM token retrieval", e)
                    }
                }
            }
        } else {
            Toast.makeText(
                this,
                "Notification permission denied - some features may not work",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    private fun checkAndRequestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            when {
                ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED -> {
                    handleNotificationPermissionGranted()
                }
                shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS) -> {
                    showPermissionRationale()
                }
                else -> {
                    requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            }
        } else {
            handleNotificationPermissionGranted()
        }
    }

    private fun handleNotificationPermissionGranted() {
        Firebase.auth.currentUser?.uid?.let { userId ->
            FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
                try {
                    if (task.isSuccessful) {
                        task.result?.let { token ->
                            tokenManager.saveToken(userId, token)
                            Log.d(TAG, "FCM token retrieved and saved successfully")
                        }
                    } else {
                        val exception = task.exception
                        Log.w(TAG, "FCM token retrieval failed, will retry", exception)
                        Handler(Looper.getMainLooper()).postDelayed({
                            checkAndRequestNotificationPermission()
                        }, 5000L)
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Unexpected error during FCM token retrieval", e)
                }
            }
        }
    }

    private fun showPermissionRationale() {
        Toast.makeText(
            this,
            "Notifications help you stay updated with important information",
            Toast.LENGTH_LONG
        ).show()
        requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
    }

    private fun handleNotificationIntent(intent: Intent?, navController: NavHostController, isInitialIntent: Boolean) {
        if (intent?.hasExtra("type") == true) {
            val type = intent.getStringExtra("type")
            val route = when (type) {
                "friend_request" -> Screen.FriendRequests.route
                "project_invitation" -> {
                    val projectId = intent.getStringExtra("projectId") ?: ""
                    Screen.ProjectInvites.createRoute(projectId)
                }
                else -> {
                    if (!isInitialIntent) Screen.Notifications.route else null
                }
            }

            route?.let {
                navController.navigate(it) {
                    if (!isInitialIntent) {
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                    }
                    launchSingleTop = true
                    restoreState = true
                }
            }
        }
    }

    private fun configureSystemBars(view: View) {
        val window = (view.context as Activity).window
        val insetsController = WindowCompat.getInsetsController(window, view)

        window.statusBarColor = backgroundColor.toArgb()
        insetsController.isAppearanceLightStatusBars = false

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            window.isNavigationBarContrastEnforced = false
            window.navigationBarColor = onGoingCardColor.toArgb()
        }
        insetsController.isAppearanceLightNavigationBars = true
    }
}