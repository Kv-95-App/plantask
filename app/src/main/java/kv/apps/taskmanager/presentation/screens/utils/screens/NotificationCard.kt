package kv.apps.taskmanager.presentation.screens.utils.screens

import android.annotation.SuppressLint
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
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
import kv.apps.taskmanager.domain.model.ProjectInvitation
import kv.apps.taskmanager.presentation.viewmodel.project.ProjectViewModel
import kv.apps.taskmanager.theme.backgroundColor
import kv.apps.taskmanager.theme.mainAppColor
import kv.apps.taskmanager.theme.onGoingCardColor

@Composable
fun NotificationCard(
    invitation: ProjectInvitation,
    onAccept: () -> Unit,
    onReject: () -> Unit,
    viewModel: ProjectViewModel,
    modifier: Modifier = Modifier,
    isLoading: Boolean
) {
    val projectUiState by viewModel.uiState.collectAsState()

    val creatorNames by remember(invitation.fromUserId) {
        derivedStateOf {
            viewModel.creatorNamesCache[invitation.fromUserId]
        }
    }

    val projectTitle by remember(invitation.projectId) {
        derivedStateOf {
            viewModel.projectTitlesCache[invitation.projectId] ?: "Project"
        }
    }

    val showAcceptDialog = remember { mutableStateOf(false) }
    val showRejectDialog = remember { mutableStateOf(false) }
    val customFont = FontFamily(Font(R.font.pilat))

    LaunchedEffect(invitation.fromUserId, invitation.projectId) {
        if (creatorNames == null) viewModel.fetchCreatorName(invitation.fromUserId)
        if (viewModel.projectTitlesCache[invitation.projectId] == null) {
            viewModel.fetchProjectTitle(invitation.projectId)
        }
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(8.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = onGoingCardColor
        ),
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
                    text = "Project Invitation",
                    style = TextStyle(
                        letterSpacing = 2.sp,
                        fontSize = 14.sp,
                        fontFamily = customFont,
                        color = Color.White
                    )
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = projectTitle,
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                Spacer(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(Color(0xFFC0C0C0).copy(alpha = 0.3f))
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "From",
                        tint = mainAppColor,
                        modifier = Modifier.size(24.dp)
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    Column {
                        Text(
                            text = "From",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFFC0C0C0)
                        )
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                color = mainAppColor,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text(
                                text = creatorNames?.let { "${it.first} ${it.second}" }
                                    ?: "Unknown user",
                                style = MaterialTheme.typography.bodyMedium,
                                color = if (creatorNames != null) Color.White else Color(0xFFC0C0C0),
                                fontWeight = if (creatorNames != null) FontWeight.SemiBold else FontWeight.Normal
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    IconButton(
                        onClick = { showRejectDialog.value = true },
                        modifier = Modifier.size(48.dp),
                        enabled = !isLoading // Use the passed isLoading state
                    ) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = "Decline",
                            tint = if (isLoading) Color(0xFFC0C0C0) else Color.Red
                        )
                    }

                    IconButton(
                        onClick = { showAcceptDialog.value = true },
                        modifier = Modifier.size(48.dp),
                        enabled = !isLoading
                    ) {
                        Icon(
                            Icons.Default.Check,
                            contentDescription = "Accept",
                            tint = if (isLoading) Color(0xFFC0C0C0) else Color.Green
                        )
                    }
                }
            }
        }
    }

    // Dialogs remain the same
    if (showAcceptDialog.value) {
        AlertDialog(
            onDismissRequest = { showAcceptDialog.value = false },
            title = {
                Text(
                    text = "Accept Invitation",
                    style = TextStyle(
                        fontFamily = customFont,
                        color = mainAppColor
                    )
                )
            },
            text = {
                Text(
                    text = "Are you sure you want to accept this project invitation?",
                    color = Color.White
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onAccept()
                        showAcceptDialog.value = false
                    },
                    enabled = !isLoading
                ) {
                    Text("Yes", color = mainAppColor)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showAcceptDialog.value = false },
                    enabled = !isLoading
                ) {
                    Text("No", color = mainAppColor)
                }
            },
            containerColor = backgroundColor
        )
    }

    if (showRejectDialog.value) {
        AlertDialog(
            onDismissRequest = { showRejectDialog.value = false },
            title = {
                Text(
                    text = "Reject Invitation",
                    style = TextStyle(
                        fontFamily = customFont,
                        color = mainAppColor
                    )
                )
            },
            text = {
                Text(
                    text = "Are you sure you want to reject this project invitation?",
                    color = Color.White
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onReject()
                        showRejectDialog.value = false
                    },
                    enabled = !isLoading
                ) {
                    Text("Yes", color = mainAppColor)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showRejectDialog.value = false },
                    enabled = !isLoading
                ) {
                    Text("No", color = mainAppColor)
                }
            },
            containerColor = backgroundColor
        )
    }
}