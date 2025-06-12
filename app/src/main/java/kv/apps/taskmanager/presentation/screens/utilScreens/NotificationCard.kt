package kv.apps.taskmanager.presentation.screens.utilScreens

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kv.apps.taskmanager.domain.model.ProjectInvitation
import kv.apps.taskmanager.presentation.viewmodel.project.ProjectViewModel
import kv.apps.taskmanager.theme.onGoingCardColor

@SuppressLint("StateFlowValueCalledInComposition")
@Composable
fun NotificationCard(
    invitation: ProjectInvitation,
    onAccept: () -> Unit,
    onReject: () -> Unit,
    viewModel: ProjectViewModel,
    modifier: Modifier = Modifier
) {
    val creatorName by remember(invitation.fromUserId) {
        derivedStateOf {
            viewModel.creatorNamesCache[invitation.fromUserId]
        }
    }

    val projectTitle by remember(invitation.projectId) {
        derivedStateOf {
            viewModel.projectTitlesCache[invitation.projectId] ?: "Project"
        }
    }

    LaunchedEffect(invitation.fromUserId, invitation.projectId) {
        if (creatorName == null) {
            viewModel.fetchCreatorName(invitation.fromUserId)
        }
        if (viewModel.projectTitlesCache[invitation.projectId] == null) {
            viewModel.fetchProjectTitle(invitation.projectId)
        }
    }

    Card(
        modifier = modifier
            .fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = onGoingCardColor)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
        ) {
            Text(
                text = "Project Invitation: $projectTitle",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "From: ",
                    color = Color.White,
                    fontSize = 14.sp
                )
                when {
                    viewModel.loading.value ->
                        CircularProgressIndicator(
                            modifier = Modifier
                                .size(14.dp),
                            color = Color.White,
                            strokeWidth = 1.5.dp
                        )
                    creatorName != null -> Text(
                        text = "${creatorName?.first} ${creatorName?.second}",
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    else -> Text(
                        text = "Unknown user",
                        color = Color.White.copy(alpha = 0.7f),
                        fontSize = 14.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "You have been invited to join the project '$projectTitle'",
                color = Color.Gray,
                fontSize = 14.sp
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
                    .fillMaxWidth()
            ) {
                IconButton(
                    onClick = onReject,
                    modifier = Modifier
                        .size(48.dp)
                ) {
                    Icon(
                        Icons.Default.Close,
                        contentDescription = "Decline",
                        tint = Color.Red
                    )
                }
                IconButton(
                    onClick = onAccept,
                    modifier = Modifier
                        .size(48.dp)
                ) {
                    Icon(
                        Icons.Default.Check,
                        contentDescription = "Accept",
                        tint = Color.Green
                    )
                }
            }
        }
    }
}