package kv.apps.taskmanager.presentation.screens.projectSection.screens

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import kotlinx.coroutines.launch
import kv.apps.taskmanager.domain.model.TeamMember
import kv.apps.taskmanager.presentation.navigation.Screen
import kv.apps.taskmanager.presentation.screens.utils.shared.uiComposables.AppDrawer
import kv.apps.taskmanager.presentation.screens.utils.shared.uiComposables.DeleteConfirmationDialog
import kv.apps.taskmanager.presentation.screens.utils.shared.menuBars.BottomNavigationBar
import kv.apps.taskmanager.presentation.screens.utils.shared.menuBars.TopBar
import kv.apps.taskmanager.presentation.viewmodel.auth.AuthViewModel
import kv.apps.taskmanager.presentation.viewmodel.project.ProjectViewModel
import kv.apps.taskmanager.theme.backgroundColor
import kv.apps.taskmanager.theme.mainAppColor
import kv.apps.taskmanager.presentation.screens.projectSection.projectComposables.MemberItem

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter", "StateFlowValueCalledInComposition")
@Composable
fun ProjectMembers(
    projectId: String,
    projectViewModel: ProjectViewModel,
    navController: NavController,
    authViewModel: AuthViewModel,
    showInvites: Boolean
) {
    val projectUiState by projectViewModel.uiState.collectAsState()
    val teamMembersWithDetails = projectUiState.teamMembersWithDetails
    val isLoading = projectUiState.isLoading
    val error = projectUiState.errorMessage
    val authUiState by authViewModel.uiState.collectAsState()
    val isLoggingOut = authUiState.isLoggingOut

    val selectedProject = projectUiState.selectedProject
    val isCreator = selectedProject?.createdBy == authViewModel.uiState.value.userId

    var showDeleteDialog by remember { mutableStateOf(false) }
    var memberToDelete by remember { mutableStateOf<TeamMember?>(null) }

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val coroutineScope = rememberCoroutineScope()

    var refreshTrigger by remember { mutableIntStateOf(0) }

    LaunchedEffect(teamMembersWithDetails.size) {
        projectViewModel.fetchTeamMembersForProject(projectId, forceRefresh = true)
    }

    LaunchedEffect(projectId, refreshTrigger) {
        projectViewModel.fetchTeamMembersForProject(projectId, forceRefresh = showInvites)
    }



    AppDrawer(
        onProfileClicked = { navController.navigate(Screen.Profile.route) },
        onLogoutClicked = {
            authViewModel.logout()
        },
        drawerState = drawerState,
        isLoggingOut = isLoggingOut,
        navController = navController
    ) {
        Scaffold(
            topBar = {
                TopBar(
                    navController = navController,
                    onMenuClicked = { coroutineScope.launch { drawerState.open() } },
                    showBackArrow = false
                )
            },
            bottomBar = { BottomNavigationBar(navController) },
            containerColor = backgroundColor
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp)
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
                } else if (error != null) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = error, color = Color.Red)
                    }
                } else {
                    Text(
                        text = "Team Members (${teamMembersWithDetails.size})",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 16.dp),
                        color = mainAppColor
                    )

                    if (teamMembersWithDetails.isEmpty()) {
                        Text("No team members yet")
                    } else {
                        LazyColumn {
                            items(teamMembersWithDetails) { member ->
                                val isCurrentUser = member.userId == authViewModel.uiState.value.userId
                                MemberItem(
                                    name = "${member.firstName} ${member.lastName}".trim(),
                                    email = member.email ?: member.userId.take(8),
                                    onClick = {
                                        navController.navigate(
                                            Screen.TargetProfile.createRoute(member.userId)
                                        )
                                    },
                                    showRemoveButton = isCreator && !isCurrentUser,
                                    onRemoveClick = {
                                        memberToDelete = member
                                        showDeleteDialog = true
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (showDeleteDialog && memberToDelete != null) {
        DeleteConfirmationDialog(
            title = "Remove Team Member",
            itemName = "${memberToDelete?.firstName} ${memberToDelete?.lastName}".trim(),
            onDismissRequest = {
                showDeleteDialog = false
                memberToDelete = null
            },
            onConfirm = {
                memberToDelete?.userId?.let { userId ->
                    projectViewModel.removeTeamMembersFromProject(
                        projectId = projectId,
                        teamMemberId = userId,
                        onSuccess = {
                            refreshTrigger++
                        }
                    )
                }
                showDeleteDialog = false
                memberToDelete = null
            },
            confirmText = "REMOVE",
            dismissText = "CANCEL"
        )
    }
}