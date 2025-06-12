package kv.apps.taskmanager.presentation.screens.utilScreens

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kv.apps.taskmanager.presentation.navigation.Screen
import kv.apps.taskmanager.presentation.shared.uiComposables.AppDrawer
import kv.apps.taskmanager.presentation.shared.uiComposables.BottomNavigationBar
import kv.apps.taskmanager.presentation.shared.uiComposables.TopBar
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
    val currentUserId = authViewModel.uiState.value.user?.uid
    val isLoggingOut = authViewModel.uiState.value.isLoggingOut

    val isLoading by projectViewModel.loading.collectAsState()
    val error by projectViewModel.error.collectAsState()
    val invitations by projectViewModel.invitations.collectAsState()
    val invitationActionState by projectViewModel.invitationActionState.collectAsState()

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val coroutineScope = rememberCoroutineScope()

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

    AppDrawer(
        onProfileClicked = { navController.navigate(Screen.Profile.route) },
        onLogoutClicked = {
            authViewModel.logout()
            navController.navigate(Screen.Login.route) {
                popUpTo("login") { inclusive = true }
            }
        },
        drawerState = drawerState,
        isLoggingOut = isLoggingOut
    ) {
        Scaffold(
            bottomBar = { BottomNavigationBar(navController) },
            containerColor = backgroundColor
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                if (isLoading) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(backgroundColor),
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
                                    onMenuClicked = { coroutineScope.launch {
                                        drawerState.open()  }
                                    },
                                    showBackArrow = false,
                                    onBackPressed = { navController.popBackStack() },
                                    isLoggingOut = isLoggingOut,
                                    modifier = Modifier.padding(top = 24.dp)
                                )

                                Text(
                                    text = "Notifications",
                                    color = Color.White,
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier
                                        .padding(16.dp)
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
                                    Text(
                                        text = "No notifications found",
                                        color = Color.White)
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
                                    modifier = Modifier
                                        .padding(8.dp),
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
}