package kv.apps.taskmanager.service

import android.Manifest
import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.media.RingtoneManager
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresPermission
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import dagger.hilt.android.AndroidEntryPoint
import kv.apps.taskmanager.MainActivity
import kv.apps.taskmanager.R
import kv.apps.taskmanager.tokenManager.FcmTokenManager
import java.net.URL
import javax.inject.Inject

@SuppressLint("MissingFirebaseInstanceTokenRefresh")
@AndroidEntryPoint
class MyFirebaseMessagingService : FirebaseMessagingService() {

    companion object {
        private const val TAG = "FCMService"
        private const val VIBRATION_PATTERN = "0,500,200,500"
    }

    @Inject lateinit var tokenManager: FcmTokenManager
    @Inject lateinit var notificationManager: NotificationManagerCompat

    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        Log.d(TAG, "Message received from: ${remoteMessage.from}")

        if (!hasNotificationPermission()) {
            Log.w(TAG, getString(R.string.notification_permission_denied))
            return
        }

        try {
            val title = remoteMessage.notification?.title ?: remoteMessage.data["title"]
            val body = remoteMessage.notification?.body ?: remoteMessage.data["body"]

            if (title != null || body != null) {
                showNotification(title, body, remoteMessage.data)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Notification handling failed", e)
        }
    }

    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    private fun showNotification(
        title: String?,
        body: String?,
        data: Map<String, String>
    ) {
        createNotificationChannelIfNeeded()

        val notificationId = System.currentTimeMillis().toInt()
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            data.forEach { (key, value) -> putExtra(key, value) }
            action = data["type"]?.let {
                when (it) {
                    "friend_request" -> "OPEN_FRIEND_REQUESTS"
                    "project_invitation" -> "OPEN_PROJECT_INVITES"
                    else -> "OPEN_NOTIFICATIONS"
                }
            } ?: "OPEN_NOTIFICATIONS"
        }

        val pendingIntent = PendingIntent.getActivity(
            this,
            notificationId,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notification = NotificationCompat.Builder(this, getString(R.string.default_notification_channel_id)).apply {
            setSmallIcon(R.drawable.ic_stat_push_notification_icon_final)
            setContentTitle(title ?: getString(R.string.app_name))
            setContentText(body ?: getString(R.string.new_notification))
            priority = NotificationCompat.PRIORITY_HIGH
            setContentIntent(pendingIntent)
            setAutoCancel(true)
            setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
            setVibrate(VIBRATION_PATTERN.split(',').map { it.toLong() }.toLongArray())

            data["imageUrl"]?.let { imageUrl ->
                try {
                    val bitmap = BitmapFactory.decodeStream(URL(imageUrl).openStream())
                    setStyle(NotificationCompat.BigPictureStyle()
                        .bigPicture(bitmap)
                        .setBigContentTitle(title))
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to load notification image", e)
                }
            }
        }.build()

        try {
            notificationManager.notify(notificationId, notification)
            Log.d(TAG, "Notification displayed successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to display notification", e)
        }
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d(TAG, "Refreshed FCM token received")
        Firebase.auth.currentUser?.uid?.let { userId ->
            tokenManager.saveToken(userId, token)
        }
    }

    private fun createNotificationChannelIfNeeded() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channelId = getString(R.string.default_notification_channel_id)
            val channelName = getString(R.string.notification_channel_name)
            val channelDescription = getString(R.string.notification_channel_description)

            val channel = NotificationChannel(
                channelId,
                channelName,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = channelDescription
                enableVibration(true)
                vibrationPattern = VIBRATION_PATTERN.split(',').map { it.toLong() }.toLongArray()
                setSound(
                    RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION),
                    null
                )
            }

            (getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager).apply {
                if (getNotificationChannel(channelId) == null) {
                    createNotificationChannel(channel)
                    Log.d(TAG, "Notification channel created")
                }
            }
        }
    }

    private fun hasNotificationPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
    }
}