package kv.apps.taskmanager.presentation.screens.utils.shared.avatar

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
internal fun MemberAvatarsRow(
    userIds: List<String>,
    initialsMap: Map<String, String>,
    maxVisible: Int = 3,
    avatarSize: Dp = 28.dp,
    overlap: Dp = (-8).dp,
    isLoading: Boolean = false
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(overlap),
        verticalAlignment = Alignment.CenterVertically
    ) {
        userIds.take(maxVisible).forEach { userId ->
            key(userId) { 
                val showLoading by rememberUpdatedState(
                    newValue = isLoading && !initialsMap.containsKey(userId)
                )
                val initials by rememberUpdatedState(
                    newValue = initialsMap[userId] ?: ""
                )

                UserAvatar(
                    initials = initials,
                    isLoading = showLoading,
                    size = avatarSize
                )
            }
        }

        if (userIds.size > maxVisible) {
            RemainingMembersCount(count = userIds.size - maxVisible)
        }
    }
}