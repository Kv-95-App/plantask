package kv.apps.taskmanager.utils

import android.content.Context
import android.widget.Toast
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

object OfflineUtils {

    fun showOfflineMessage(
        context: Context,
        message: String = "This action requires internet connection"
    ) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }

    fun showOfflineSnackbar(
        snackbarHostState: SnackbarHostState,
        scope: CoroutineScope,
        message: String = "No internet connection. Changes saved locally.",
        actionLabel: String? = null,
        onActionPerformed: (() -> Unit)? = null
    ) {
        scope.launch {
            val result = snackbarHostState.showSnackbar(
                message = message,
                actionLabel = actionLabel,
                withDismissAction = true
            )

            if (result == SnackbarResult.ActionPerformed && onActionPerformed != null) {
                onActionPerformed()
            }
        }
    }

    fun getSyncPendingMessage(pendingCount: Int): String {
        return when {
            pendingCount == 0 -> "All changes synced"
            pendingCount == 1 -> "1 change pending sync"
            else -> "$pendingCount changes pending sync"
        }
    }

    fun getOfflineFeatureMessage(feature: String): String {
        return "$feature is not available offline. Please connect to the internet to use this feature."
    }

    // Common offline feature messages
    const val COMMENTS_OFFLINE_MESSAGE =
        "Comments are not available offline. Please connect to the internet to view and add comments."
    const val TEAM_MANAGEMENT_OFFLINE_MESSAGE =
        "Team management features require internet connection."
    const val INVITATIONS_OFFLINE_MESSAGE = "Project invitations require internet connection."
    const val USER_PROFILES_OFFLINE_MESSAGE = "User profiles are not available offline."
}