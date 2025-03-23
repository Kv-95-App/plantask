package kv.apps.taskmanager.presentation.shared.friendsComposables

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import kv.apps.taskmanager.domain.model.User
import kv.apps.taskmanager.presentation.viewmodel.UserFriendsViewModel
import kv.apps.taskmanager.theme.onGoingCardColor

@Composable
fun PendingFriendRequestCard(
    user: User,
    currentUserId: String,
    userFriendsViewModel: UserFriendsViewModel,
    modifier: Modifier = Modifier
) {
    var showAcceptDialog by remember { mutableStateOf(false) }

    var showRejectDialog by remember { mutableStateOf(false) }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(8.dp),
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
            Text(
                text = "${user.firstName} ${user.lastName}",
                color = Color.White,
                modifier = Modifier.weight(1f)
            )

            IconButton(
                onClick = { showAcceptDialog = true }
            ) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Accept",
                    tint = Color.Green
                )
            }

            // Reject Button
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

    if (showAcceptDialog) {
        AlertDialog(
            onDismissRequest = { showAcceptDialog = false },
            title = { Text(text = "Accept Friend Request") },
            text = { Text(text = "Are you sure you want to accept ${user.firstName}'s friend request?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        userFriendsViewModel.acceptFriendRequest(currentUserId, user.email)
                        showAcceptDialog = false
                    }
                ) {
                    Text("Yes")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showAcceptDialog = false }
                ) {
                    Text("No")
                }
            }
        )
    }

    if (showRejectDialog) {
        AlertDialog(
            onDismissRequest = { showRejectDialog = false },
            title = { Text(text = "Reject Friend Request") },
            text = { Text(text = "Are you sure you want to reject ${user.firstName}'s friend request?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        userFriendsViewModel.rejectFriendRequest(currentUserId, user.email)
                        showRejectDialog = false
                    }
                ) {
                    Text("Yes")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showRejectDialog = false }
                ) {
                    Text("No")
                }
            }
        )
    }
}