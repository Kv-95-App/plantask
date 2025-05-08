package kv.apps.taskmanager.presentation.screens.utilScreens

import android.R.attr.fontWeight
import android.R.attr.text
import android.annotation.SuppressLint
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import kotlinx.coroutines.delay
import kv.apps.taskmanager.domain.model.ProjectInvitation
import kv.apps.taskmanager.presentation.shared.uiComposables.BottomNavigationBar
import kv.apps.taskmanager.presentation.shared.uiComposables.TopBar
import kv.apps.taskmanager.presentation.viewmodel.AuthViewModel
import kv.apps.taskmanager.presentation.viewmodel.ProjectViewModel
import kv.apps.taskmanager.theme.backgroundColor
import kv.apps.taskmanager.theme.mainAppColor
import kv.apps.taskmanager.theme.onGoingCardColor

@Composable
fun NotificationsScreen(
    navController: NavController,
    authViewModel: AuthViewModel,
    projectViewModel: ProjectViewModel = viewModel()
) {
    val focusManager = LocalFocusManager.current
    val currentUserId by authViewModel.currentUserId.collectAsState()

    val loading by projectViewModel.loading.collectAsState()
    val error by projectViewModel.error.collectAsState()
    val invitations by projectViewModel.invitations.collectAsState()
    val invitationActionState by projectViewModel.invitationActionState.collectAsState()

    var showSnackBar by remember { mutableStateOf(false) }
    var snackBarMessage by remember { mutableStateOf("") }

    LaunchedEffect(invitationActionState) {
        invitationActionState?.let { result ->
            result.onSuccess {
                snackBarMessage = "Action completed successfully"
                showSnackBar = true
            }.onFailure { e ->
                snackBarMessage = "Error: ${e.message}"
                showSnackBar = true
            }
            projectViewModel.clearInvitationActionState()
        }
    }

    LaunchedEffect(error) {
        error?.let {
            snackBarMessage = it
            showSnackBar = true
            projectViewModel.clearError()
        }
    }

    LaunchedEffect(currentUserId) {
        currentUserId?.let { userId ->
            projectViewModel.getPendingProjectInvitations(userId)
        }
    }

    if (showSnackBar) {
        LaunchedEffect(showSnackBar) {
            delay(3000)
            showSnackBar = false
        }
        Snackbar(
            modifier = Modifier.padding(16.dp),
            action = {
                IconButton(
                    onClick = { showSnackBar = false }
                ) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                }
            }
        ) {
            Text(text = snackBarMessage, color = Color.White)
        }
    }

    Scaffold(
        bottomBar = { BottomNavigationBar(navController) },
        containerColor = backgroundColor
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (loading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = mainAppColor)
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .clickable { focusManager.clearFocus() }
                ) {
                    item {
                        Column {
                            TopBar(
                                navController = navController,
                                onProfileClicked = { /* Handle profile click */ },
                                onLogoutClicked = {
                                    navController.navigate("login") {
                                        popUpTo("login") { inclusive = true }
                                    }
                                },
                                showBackArrow = true,
                                onBackPressed = { navController.popBackStack() }
                            )

                            Text(
                                text = "Notifications",
                                color = Color.White,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(16.dp)
                            )
                        }
                    }

                    if (invitations.isEmpty()) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillParentMaxSize()
                                    .padding(top = 32.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(text = "No notifications found", color = Color.White)
                            }
                        }
                    } else {
                        items(invitations) { invitation ->
                            NotificationCard(
                                onAccept = {
                                    projectViewModel.acceptInvitation(
                                        invitationId = invitation.invitationId,
                                        projectId = invitation.projectId,
                                        userId = invitation.toUserId
                                    )
                                },
                                onReject = {
                                    projectViewModel.rejectInvitation(
                                        invitationId = invitation.invitationId,
                                        projectId = invitation.projectId,
                                        userId = invitation.toUserId
                                    )
                                },
                                modifier = Modifier.padding(8.dp),
                                invitation = invitation,
                                viewModel = projectViewModel
                            )
                        }
                    }
                }
            }
        }
    }
}

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
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = onGoingCardColor)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
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
                            modifier = Modifier.size(14.dp),
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
                modifier = Modifier.fillMaxWidth()
            ) {
                IconButton(
                    onClick = onReject,
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(
                        Icons.Default.Close,
                        contentDescription = "Decline",
                        tint = Color.Red
                    )
                }
                IconButton(
                    onClick = onAccept,
                    modifier = Modifier.size(48.dp)
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