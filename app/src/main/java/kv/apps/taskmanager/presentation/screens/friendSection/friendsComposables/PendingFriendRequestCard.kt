package kv.apps.taskmanager.presentation.screens.friendSection.friendsComposables

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kv.apps.taskmanager.domain.model.User
import kv.apps.taskmanager.presentation.screens.utils.shared.uiComposables.ConfirmationDialog
import kv.apps.taskmanager.presentation.screens.utils.shared.uiComposables.DeleteConfirmationDialog
import kv.apps.taskmanager.presentation.viewmodel.userFriends.UserFriendsViewModel
import kv.apps.taskmanager.theme.mainAppColor
import kv.apps.taskmanager.theme.onGoingCardColor
import kv.apps.taskmanager.theme.contrastingTextColor

@Composable
fun PendingFriendRequestCard(
    user: User,
    currentUserId: String?,
    userFriendsViewModel: UserFriendsViewModel,
    modifier: Modifier = Modifier
) {
    var showAcceptDialog by remember { mutableStateOf(false) }
    var showRejectDialog by remember { mutableStateOf(false) }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = onGoingCardColor
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(mainAppColor, CircleShape)
                    .clip(CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = getInitials("${user.firstName} ${user.lastName}"),
                    color = contrastingTextColor(mainAppColor),
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = "${user.firstName} ${user.lastName}",
                    color = Color.White,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = FontWeight.Bold
                    )
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = user.email,
                    color = Color.Gray,
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            if (currentUserId != null) {
                Row {
                    IconButton(
                        onClick = { showAcceptDialog = true }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Accept",
                            tint = Color.Green
                        )
                    }
                    IconButton(
                        onClick = { showRejectDialog = true }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Reject",
                            tint = Color.Red
                        )
                    }
                }
            }
        }
    }
    if (showAcceptDialog) {
        ConfirmationDialog(
            title = "Accept Friend Request",
            message = "Accept friend request from ${user.firstName}?",
            confirmText = "ACCEPT",
            onDismissRequest = { showAcceptDialog = false },
            onConfirm = {
                currentUserId?.let { userId ->
                    userFriendsViewModel.acceptFriendRequest(userId, user.email)
                }
                showAcceptDialog = false
            }
        )
    }

    if (showRejectDialog) {
        DeleteConfirmationDialog(
            title = "Reject Friend Request",
            itemName = "${user.firstName} ${user.lastName}",
            onDismissRequest = { showRejectDialog = false },
            onConfirm = {
                currentUserId?.let { userId ->
                    userFriendsViewModel.rejectFriendRequest(userId, user.email)
                }
                showRejectDialog = false
            }
        )
    }
}

private fun getInitials(fullName: String): String {
    return fullName
        .split(" ")
        .mapNotNull { it.firstOrNull()?.toString() }
        .take(2)
        .joinToString("")
        .uppercase()
}