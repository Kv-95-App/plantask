package kv.apps.taskmanager.presentation.shared.taskComposables

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import kv.apps.taskmanager.domain.model.Project
import kv.apps.taskmanager.theme.mainAppColor

@Composable
fun CompletedProjectCard(project: Project, onDeleteClicked: (Project) -> Unit) {
    val showDeleteDialog = remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .width(200.dp)
            .padding(8.dp)
            .clickable(onClick = { /* Handle project click */ }),
        colors = CardDefaults.cardColors(containerColor = mainAppColor)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = project.title,
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.Black,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )

                IconButton(
                    onClick = { showDeleteDialog.value = true },
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = Color.Black
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Project Description
            Text(
                text = project.description,
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Black,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Due Date
            Text(
                text = "Due: ${project.dueDate}",
                style = MaterialTheme.typography.bodySmall,
                color = Color.Black
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Completion Indicator
            LinearProgressIndicator(
                progress = 1f, // 100% completed
                modifier = Modifier.fillMaxWidth(),
                color = Color.Black
            )
        }
    }

    // Delete Confirmation Dialog
    if (showDeleteDialog.value) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog.value = false },
            title = { Text(text = "Delete Project") },
            text = { Text(text = "Are you sure you want to delete this project?") },
            confirmButton = {
                TextButton(onClick = {
                    onDeleteClicked(project)
                    showDeleteDialog.value = false
                }) {
                    Text("Yes")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog.value = false }) {
                    Text("No")
                }
            }
        )
    }
}