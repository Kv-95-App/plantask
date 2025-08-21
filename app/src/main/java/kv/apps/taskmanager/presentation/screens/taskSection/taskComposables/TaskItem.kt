package kv.apps.taskmanager.presentation.screens.taskSection.taskComposables

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kv.apps.taskmanager.domain.model.Task
import kv.apps.taskmanager.presentation.screens.utils.shared.avatar.MemberAvatarsRow
import kv.apps.taskmanager.presentation.screens.utils.shared.uiComposables.ConfirmationDialog
import kv.apps.taskmanager.presentation.screens.utils.shared.uiComposables.DeleteConfirmationDialog
import kv.apps.taskmanager.presentation.viewmodel.auth.AuthViewModel
import kv.apps.taskmanager.presentation.viewmodel.task.TaskViewModel
import kv.apps.taskmanager.theme.mainAppColor

@Composable
fun TaskItem(
    task: Task,
    taskViewModel: TaskViewModel,
    onTaskClicked: () -> Unit,
    authViewModel: AuthViewModel,
    projectCreatedBy: String,
    modifier: Modifier = Modifier,
    onDelete: (() -> Unit)? = null,
    isReadOnly: Boolean = false
) {
    val uiState by taskViewModel.uiState.collectAsState()
    val authState by authViewModel.uiState.collectAsState()

    val currentUserId = authState.user?.uid ?: ""
    val canDelete = remember(currentUserId, projectCreatedBy) {
        currentUserId == projectCreatedBy && !isReadOnly
    }

    var isChecked by remember { mutableStateOf(task.isCompleted) }
    var showCompletionConfirmation by remember { mutableStateOf(false) }
    var pendingCompletionStatus by remember { mutableStateOf(false) }

    val alpha by animateFloatAsState(targetValue = if (isChecked) 0.7f else 1f)
    val borderColor by animateColorAsState(
        targetValue = if (isChecked) mainAppColor.copy(alpha = 0.5f) else Color(0xFF3A3E4B),
        label = "borderColor"
    )

    var showDeleteDialog by remember { mutableStateOf(false) }

    LaunchedEffect(task.id) {
        taskViewModel.fetchAssignedUsersInitials(task.projectId, task.id)
    }

    LaunchedEffect(task.isCompleted) {
        isChecked = task.isCompleted
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onTaskClicked() },
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF2A2D36)
        ),
        shape = RoundedCornerShape(14.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .alpha(alpha)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (!isReadOnly) {
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .border(
                                width = 2.dp,
                                color = borderColor,
                                shape = RoundedCornerShape(6.dp)
                            )
                            .background(
                                if (isChecked) mainAppColor else Color.Transparent
                            )
                            .clickable {
                                pendingCompletionStatus = !isChecked
                                showCompletionConfirmation = true
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        if (isChecked) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "Completed",
                                tint = Color.Black,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                }

                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = task.title,
                            style = MaterialTheme.typography.titleMedium.copy(
                                color = if (isChecked) Color(0xFF6D7187) else Color.White,
                                fontWeight = FontWeight.SemiBold
                            ),
                            modifier = Modifier.weight(1f)
                        )

                        if (canDelete && onDelete != null) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Delete task",
                                tint = Color(0xFF6D7187),
                                modifier = Modifier
                                    .size(24.dp)
                                    .clickable { showDeleteDialog = true }
                            )
                        }
                    }

                    if (task.taskDetails.isNotBlank()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = task.taskDetails,
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = Color(0xFF6D7187)
                            ),
                            maxLines = 2
                        )
                    }
                }
            }

            if (task.assignedTo.isNotEmpty() || task.dueDate.isNotBlank()) {
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (task.assignedTo.isNotEmpty()) {
                        MemberAvatarsRow(
                            userIds = task.assignedTo,
                            initialsMap = uiState.taskAssignedUsersInitials,
                            isLoading = uiState.isLoading
                        )
                    }

                    if (task.dueDate.isNotBlank()) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(mainAppColor.copy(alpha = 0.2f))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = task.dueDate,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = mainAppColor
                                )
                            )
                        }
                    }
                }
            }
        }
    }

    if (showDeleteDialog) {
        DeleteConfirmationDialog(
            title = "Delete Task",
            itemName = task.title,
            onDismissRequest = { showDeleteDialog = false },
            onConfirm = {
                onDelete?.invoke()
                showDeleteDialog = false
            }
        )
    }

    if (showCompletionConfirmation && !isReadOnly) {
        ConfirmationDialog(
            title = if (pendingCompletionStatus) "Mark as Completed?" else "Mark as In Progress?",
            message = if (pendingCompletionStatus)
                "Are you sure you want to mark this task as completed?"
            else
                "Are you sure you want to mark this task as in progress?",
            onDismissRequest = { showCompletionConfirmation = false },
            onConfirm = {
                isChecked = pendingCompletionStatus
                taskViewModel.updateTaskInProject(
                    task.projectId,
                    task.copy(isCompleted = pendingCompletionStatus)
                )
                showCompletionConfirmation = false
            }
        )
    }
}