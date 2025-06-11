package kv.apps.taskmanager.presentation.shared.taskComposables

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kv.apps.taskmanager.R
import kv.apps.taskmanager.domain.model.Project
import kv.apps.taskmanager.theme.mainAppColor

@Composable
fun CompletedProjectCard(
    project: Project,
    onDeleteClicked: (Project) -> Unit,
    modifier: Modifier = Modifier
) {
    val showDeleteDialog = remember { mutableStateOf(false) }

    val customFont = FontFamily(
        Font(R.font.pilat)
    )

    Card(
        modifier = modifier
            .padding(8.dp)
            .clickable(onClick = {  }),
        colors = CardDefaults.cardColors(containerColor = mainAppColor)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()) {
                Text(
                    text = project.title,
                    style = TextStyle(
                        fontSize = 14.sp,
                        fontFamily = customFont,
                        fontWeight = FontWeight.Thin,
                        letterSpacing = 1.7.sp,
                        color = Color.Black
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(end = 32.dp)
                )

                IconButton(
                    onClick = { showDeleteDialog.value = true },
                    modifier = Modifier
                        .size(24.dp)
                        .align(Alignment.TopEnd)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = Color.Black
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Due: ${project.dueDate}",
                style = MaterialTheme.typography.bodySmall,
                color = Color.Black
            )

            Spacer(modifier = Modifier.height(8.dp))

            LinearProgressIndicator(
                progress = 1f,
                modifier = Modifier.fillMaxWidth(),
                color = Color.Black,
                trackColor = Color.Gray
            )
        }
    }

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