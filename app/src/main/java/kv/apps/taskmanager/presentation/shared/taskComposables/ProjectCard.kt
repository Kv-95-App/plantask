package kv.apps.taskmanager.presentation.shared.taskComposables

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
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import kv.apps.taskmanager.R
import kv.apps.taskmanager.domain.model.Project
import kv.apps.taskmanager.presentation.navigation.Screen
import kv.apps.taskmanager.theme.mainAppColor
import kv.apps.taskmanager.theme.onGoingCardColor


@Composable
fun ProjectCard(
    project: Project,
    onDeleteClicked: () -> Unit,
    onMarkComplete: () -> Unit,
    navController: NavController
) {
    val showDeleteDialog = remember { mutableStateOf(false) }
    val showCompleteDialog = remember { mutableStateOf(false) }
    val customFont = FontFamily(
        Font(R.font.pilat )
    )
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .clickable {
                if (true) {
                    navController.navigate(Screen.ProjectDetail.createRoute(project.id))
                }
            },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = onGoingCardColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = project.title,
                    style = TextStyle(
                        letterSpacing = 2.sp,
                        fontSize = 14.sp,
                        fontFamily = customFont,
                        color = Color.White
                    )
                )
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        imageVector = Icons.Default.DateRange,
                        contentDescription = "Due Date",
                        tint = mainAppColor,
                        modifier = Modifier
                            .size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Due: ${project.dueDate}",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFFC0C0C0)
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.team2),
                        contentDescription = "Team Members",
                        tint = mainAppColor,
                        modifier = Modifier
                            .size(24.dp)
                    )
                    val totalMembers = project.teamMembers.size
                    val memberText =
                        if (totalMembers == 1) "1 Member" else "$totalMembers Members"
                    Spacer(modifier = Modifier.width(8.dp))

                    Text(
                        text = "$memberText ",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFFC0C0C0),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { showDeleteDialog.value = true }) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete",
                            tint = mainAppColor
                        )
                    }
                    IconButton(onClick = { showCompleteDialog.value = true }) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Mark as Complete",
                            tint = Color.Green
                        )
                    }
                }
            }
        }
    }

    if (showDeleteDialog.value) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog.value = false },
            title = { Text(text = "Delete Project") },
            text = { Text(text = "Are you sure you want to delete this project?") },
            confirmButton = {
                TextButton(onClick = {
                    onDeleteClicked()
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

    if (showCompleteDialog.value) {
        AlertDialog(
            onDismissRequest = { showCompleteDialog.value = false },
            title = { Text(text = "Mark Project as Complete") },
            text = { Text(text = "Are you sure you want to mark this project as complete?") },
            confirmButton = {
                TextButton(onClick = {
                    onMarkComplete()
                    showCompleteDialog.value = false
                }) {
                    Text("Yes")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCompleteDialog.value = false }) {
                    Text("No")
                }
            }
        )
    }
}