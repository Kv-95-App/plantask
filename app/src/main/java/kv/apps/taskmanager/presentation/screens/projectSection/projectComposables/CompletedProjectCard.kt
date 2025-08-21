package kv.apps.taskmanager.presentation.screens.projectSection.projectComposables

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProgressIndicatorDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import kv.apps.taskmanager.domain.model.Project
import kv.apps.taskmanager.presentation.screens.utils.shared.uiComposables.DeleteConfirmationDialog
import kv.apps.taskmanager.theme.backgroundColor
import kv.apps.taskmanager.theme.mainAppColor

@Composable
fun CompletedProjectCard(
    project: Project,
    onDeleteClicked: (Project) -> Unit,
    showDelete: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val showDeleteDialog = remember { mutableStateOf(false) }

    Card(
        modifier = modifier
            .padding(8.dp)
            .size(width = 160.dp, height = 140.dp)
            .clickable(onClick = onClick),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(
            containerColor = backgroundColor.copy(
                alpha = 0.9f
            ),
            contentColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = project.title,
                        style = MaterialTheme.typography.titleMedium.copy(
                            color = mainAppColor,
                            fontWeight = FontWeight.Bold
                        ),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )

                    if (showDelete) {
                        Spacer(modifier = Modifier.width(4.dp))
                        IconButton(
                            onClick = { showDeleteDialog.value = true },
                            modifier = Modifier.size(20.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Delete",
                                tint = Color.White.copy(alpha = 0.6f)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Box(
                    modifier = Modifier
                        .background(
                            color = mainAppColor.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(16.dp)
                        )
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "COMPLETED",
                        style = MaterialTheme.typography.labelSmall,
                        color = mainAppColor
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(top = 4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.CalendarToday,
                        contentDescription = "Due date",
                        modifier = Modifier.size(14.dp),
                        tint = Color.White.copy(alpha = 0.7f)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = project.dueDate,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.8f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.weight(1f))

                LinearProgressIndicator(
                    progress = { 1f },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(4.dp),
                    color = mainAppColor,
                    trackColor = mainAppColor.copy(alpha = 0.2f),
                    strokeCap = ProgressIndicatorDefaults.LinearStrokeCap,
                )
            }
        }
    }

    if (showDeleteDialog.value) {
        DeleteConfirmationDialog(
            title = "Delete Project",
            itemName = project.title,
            onDismissRequest = { showDeleteDialog.value = false },
            onConfirm = {
                onDeleteClicked(project)
                showDeleteDialog.value = false
            }
        )
    }
}