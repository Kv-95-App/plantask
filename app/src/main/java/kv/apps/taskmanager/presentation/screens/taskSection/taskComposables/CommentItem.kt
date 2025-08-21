package kv.apps.taskmanager.presentation.screens.taskSection.taskComposables

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kv.apps.taskmanager.domain.model.TaskComment
import kv.apps.taskmanager.theme.backgroundColor
import kv.apps.taskmanager.theme.mainAppColor

@Composable
fun CommentItem(
    comment: TaskComment,
    currentUserId: String?,
    onEditComment: (String) -> Unit,
    onDeleteComment: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isEditing by remember { mutableStateOf(false) }
    var editedText by remember { mutableStateOf(comment.message) }
    var showMenu by remember { mutableStateOf(false) }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.Start
    ) {
        CommentAvatar(
            displayName = comment.userDisplayName,
            modifier = Modifier
                .size(40.dp)
                .align(Alignment.Top)
        )

        Spacer(modifier = Modifier.width(12.dp))

        Column(
            modifier = Modifier.weight(1f)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = comment.userDisplayName,
                    style = MaterialTheme.typography.labelMedium.copy(
                        color = mainAppColor.copy(alpha = 0.9f),
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 13.sp
                    )
                )
                Text(
                    text = formatCommentTime(comment.timestamp),
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = Color.Gray.copy(alpha = 0.7f),
                        fontSize = 11.sp
                    )
                )
            }
            Spacer(modifier = Modifier.height(4.dp))

            Surface(
                modifier = Modifier
                    .shadow(
                        elevation = 1.dp,
                        shape = RoundedCornerShape(
                            topStart = 16.dp,
                            topEnd = 16.dp,
                            bottomStart = if (comment.userId == currentUserId) 16.dp else 4.dp,
                            bottomEnd = if (comment.userId == currentUserId) 4.dp else 16.dp
                        ),
                        spotColor = Color.Black.copy(alpha = 0.1f)
                    ),
                color = if (comment.userId == currentUserId) {
                    mainAppColor.copy(alpha = 0.9f)
                } else {
                    Color(0xFF2D303B)
                },
                shape = RoundedCornerShape(
                    topStart = 16.dp,
                    topEnd = 16.dp,
                    bottomStart = if (comment.userId == currentUserId) 16.dp else 4.dp,
                    bottomEnd = if (comment.userId == currentUserId) 4.dp else 16.dp
                )
            ) {
                Box(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    if (isEditing) {
                        OutlinedTextField(
                            value = editedText,
                            onValueChange = { editedText = it },
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = mainAppColor,
                                unfocusedBorderColor = Color.Transparent,
                                focusedTextColor = if (comment.userId == currentUserId) Color.Black else Color.White,
                                unfocusedTextColor = if (comment.userId == currentUserId) Color.Black else Color.White,
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent
                            ),
                            textStyle = MaterialTheme.typography.bodyMedium.copy(
                                color = if (comment.userId == currentUserId) Color.Black else Color.White,
                                fontSize = 15.sp,
                                lineHeight = 20.sp
                            )
                        )
                    } else {
                        Text(
                            text = comment.message,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = if (comment.userId == currentUserId) Color.Black else Color.White,
                                fontSize = 15.sp,
                                lineHeight = 20.sp
                            )
                        )
                    }
                }
            }
            if (comment.userId == currentUserId) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (isEditing) {
                        Row {
                            TextButton(
                                onClick = {
                                    onEditComment(editedText)
                                    isEditing = false
                                },
                                colors = androidx.compose.material3.ButtonDefaults.textButtonColors(
                                    contentColor = mainAppColor
                                ),
                                modifier = Modifier.padding(end = 8.dp)
                            ) {
                                Text(
                                    "Save",
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        fontWeight = FontWeight.Medium,
                                        fontSize = 13.sp
                                    )
                                )
                            }
                            TextButton(
                                onClick = { isEditing = false },
                                colors = androidx.compose.material3.ButtonDefaults.textButtonColors(
                                    contentColor = Color.Gray
                                )
                            ) {
                                Text(
                                    "Cancel",
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        fontSize = 13.sp
                                    )
                                )
                            }
                        }
                    } else {
                        Box {
                            IconButton(
                                onClick = { showMenu = true },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    Icons.Filled.MoreVert,
                                    contentDescription = "More options",
                                    tint = Color.Gray.copy(alpha = 0.7f),
                                    modifier = Modifier.size(18.dp)
                                )
                            }

                            CommentDropdownMenu(
                                expanded = showMenu,
                                onDismissRequest = { showMenu = false },
                                onEditClick = {
                                    isEditing = true
                                    showMenu = false
                                },
                                onDeleteClick = {
                                    onDeleteComment()
                                    showMenu = false
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CommentDropdownMenu(
    expanded: Boolean,
    onDismissRequest: () -> Unit,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    DropdownMenu(
        expanded = expanded,
        onDismissRequest = onDismissRequest,
        modifier = Modifier.background(Color(0xFF2D303B))
    ) {
        DropdownMenuItem(
            text = {
                Text(
                    "Edit",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = Color.White,
                        fontSize = 14.sp
                    )
                )
            },
            onClick = onEditClick,
            modifier = Modifier.background(Color(0xFF2D303B))
        )
        HorizontalDivider(thickness = 0.5.dp, color = Color(0xFF40444F))
        DropdownMenuItem(
            text = {
                Text(
                    "Delete",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = Color(0xFFFF6B6B),
                        fontSize = 14.sp
                    )
                )
            },
            onClick = onDeleteClick,
            modifier = Modifier.background(Color(0xFF2D303B))
        )
    }
}

@Composable
fun CommentAvatar(
    displayName: String,
    modifier: Modifier = Modifier,
    size: Dp = 40.dp
) {
    val initials = remember(displayName) {
        generateInitials(displayName)
    }

    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(
                brush = androidx.compose.ui.graphics.Brush.verticalGradient(
                    colors = listOf(
                        mainAppColor.copy(alpha = 0.9f),
                        mainAppColor.copy(alpha = 0.7f)
                    )
                )
            )
            .shadow(
                elevation = 2.dp,
                shape = CircleShape,
                spotColor = mainAppColor.copy(alpha = 0.3f)
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = initials,
            style = MaterialTheme.typography.bodyLarge.copy(
                color = Color.Black,
                fontWeight = FontWeight.Bold,
                fontSize = when (initials.length) {
                    1 -> 16.sp
                    2 -> 14.sp
                    else -> 12.sp
                }
            )
        )
    }
}

private fun generateInitials(displayName: String): String {
    return displayName.split(" ")
        .filter { it.isNotBlank() }
        .take(2)
        .joinToString("") { it.take(1).uppercase() }
        .take(2)
        .ifEmpty { "?" }
}

private fun formatCommentTime(timestamp: java.util.Date): String {
    val now = java.util.Date()
    val diff = now.time - timestamp.time
    val minutes = diff / (1000 * 60)
    val hours = diff / (1000 * 60 * 60)
    val days = diff / (1000 * 60 * 60 * 24)

    return when {
        minutes < 1 -> "now"
        minutes < 60 -> "${minutes}m ago"
        hours < 24 -> "${hours}h ago"
        days < 7 -> "${days}d ago"
        else -> java.text.SimpleDateFormat("MMM dd", java.util.Locale.getDefault()).format(timestamp)
    }
}