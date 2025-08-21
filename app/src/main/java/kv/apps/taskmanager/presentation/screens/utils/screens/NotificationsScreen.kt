package kv.apps.taskmanager.presentation.screens.utils.screens

import android.annotation.SuppressLint
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import kv.apps.taskmanager.presentation.navigation.Screen
import kv.apps.taskmanager.presentation.screens.utils.shared.uiComposables.AppDrawer
import kv.apps.taskmanager.presentation.screens.utils.shared.menuBars.BottomNavigationBar
import kv.apps.taskmanager.presentation.screens.utils.shared.menuBars.TopBar
import kv.apps.taskmanager.presentation.viewmodel.auth.AuthViewModel
import kv.apps.taskmanager.presentation.viewmodel.project.ProjectViewModel
import kv.apps.taskmanager.theme.backgroundColor
import kv.apps.taskmanager.theme.mainAppColor

@SuppressLint("StateFlowValueCalledInComposition")
@Composable
fun NotificationsScreen(
    navController: NavController,
    authViewModel: AuthViewModel,
    projectViewModel: ProjectViewModel
) {
    val focusManager = LocalFocusManager.current
    val currentUser by authViewModel.uiState.collectAsState()
    val currentUserId = currentUser.user?.uid
    val authUiState by authViewModel.uiState.collectAsState()
    val isLoggingOut = authUiState.isLoggingOut

    val isLoading by projectViewModel.notificationsLoading.collectAsState()
    val error by projectViewModel.error.collectAsState()
    val invitations by projectViewModel.invitations.collectAsState()
    val invitationActionState by projectViewModel.invitationActionState.collectAsState()

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val coroutineScope = rememberCoroutineScope()

    var showSnackBar by remember { mutableStateOf(false) }
    var snackBarMessage by remember { mutableStateOf("") }
    val isEmpty by remember(invitations) { derivedStateOf { invitations.isEmpty() } }

    LaunchedEffect(currentUserId) {
        currentUserId?.let { userId ->
            projectViewModel.getPendingProjectInvitations(userId, true)

            snapshotFlow { userId }
                .distinctUntilChanged()
                .collectLatest {
                    projectViewModel.getPendingProjectInvitations(it, true)
                }
        }
    }

    LaunchedEffect(invitationActionState, error) {
        invitationActionState?.let { result ->
            result.fold(
                onSuccess = {
                    snackBarMessage = "Action completed successfully"
                    showSnackBar = true
                },
                onFailure = { e ->
                    snackBarMessage = "Error: ${e.message}"
                    showSnackBar = true
                }
            )
            projectViewModel.clearInvitationActionState()
        }

        error?.let {
            snackBarMessage = it
            showSnackBar = true
            projectViewModel.clearError()
        }
    }

    if (showSnackBar) {
        LaunchedEffect(true) {
            delay(3000)
            showSnackBar = false
        }

        Snackbar(
            modifier = Modifier.padding(16.dp),
            action = {
                IconButton(
                    onClick = { showSnackBar = false },
                    modifier = Modifier.padding(8.dp)
                ) {
                    Icon(
                        Icons.Default.Close,
                        contentDescription = "Close",
                        tint = Color.White
                    )
                }
            }
        ) {
            Text(
                text = snackBarMessage,
                color = Color.White,
                modifier = Modifier.padding(8.dp))
        }
    }

    AppDrawer(
        onProfileClicked = { navController.navigate(Screen.Profile.route) },
        onLogoutClicked = {
            authViewModel.logout()
            projectViewModel.clearNotificationsCache()
        },
        drawerState = drawerState,
        isLoggingOut = isLoggingOut,
        navController = navController
    ) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            topBar = {
                TopBar(
                    navController = navController,
                    onMenuClicked = { coroutineScope.launch { drawerState.open() } },
                    showBackArrow = false
                )
            },
            bottomBar = {
                BottomNavigationBar(navController = navController)
            },
            containerColor = backgroundColor
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .clickable { focusManager.clearFocus() }
            ) {
                when {
                    isLoading && invitations.isEmpty() -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(color = mainAppColor)
                        }
                    }
                    isEmpty -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "No notifications found",
                                color = Color.White,
                                fontSize = 16.sp
                            )
                        }
                    }
                    else -> {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize()
                        ) {
                            item {
                                Text(
                                    text = "Notifications",
                                    color = Color.White,
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(16.dp)
                                )
                            }

                            items(
                                items = invitations,
                                key = { it.invitationId }
                            ) { invitation ->
                                NotificationCard(
                                    invitation = invitation,
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
                                    viewModel = projectViewModel,
                                    isLoading = projectViewModel.loading.value,
                                    modifier = Modifier.padding(8.dp)
                                )
                            }

                            if (isLoading && invitations.isNotEmpty()) {
                                item {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(16.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        CircularProgressIndicator(
                                            color = mainAppColor,
                                            modifier = Modifier.size(24.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}