package kv.apps.taskmanager.presentation.shared.friendsComposables


import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import kv.apps.taskmanager.domain.model.Friend
import kv.apps.taskmanager.domain.model.User
import kv.apps.taskmanager.presentation.viewmodel.UserFriendsViewModel
import kv.apps.taskmanager.theme.onGoingCardColor

@Composable
fun FriendCard(
    friend: Friend,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    currentUserId: String,
    userFriendsViewModel: UserFriendsViewModel = hiltViewModel()
) {
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
                .padding(16.dp)
                .clickable { onClick() },
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = friend.friendName,
                color = Color.White,
                modifier = Modifier.weight(1f)
            )
            IconButton(
                onClick = {
                    userFriendsViewModel.deleteFriend(currentUserId, friend.friendId)
                }
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete friend",
                    tint = Color.White
                )
            }
        }
    }
}