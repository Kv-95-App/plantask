package kv.apps.taskmanager.presentation.screens.projectScreens

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import kv.apps.taskmanager.R
import kv.apps.taskmanager.domain.model.Friend
import kv.apps.taskmanager.domain.model.ProjectInvitation
import kv.apps.taskmanager.presentation.shared.uiComposables.BottomNavigationBar
import kv.apps.taskmanager.presentation.shared.uiComposables.TopBar
import kv.apps.taskmanager.presentation.viewmodel.AuthViewModel
import kv.apps.taskmanager.presentation.viewmodel.ProjectViewModel
import kv.apps.taskmanager.presentation.viewmodel.TaskViewModel
import kv.apps.taskmanager.presentation.viewmodel.UserFriendsViewModel
import kv.apps.taskmanager.theme.backgroundColor
import kv.apps.taskmanager.theme.mainAppColor

@SuppressLint("StateFlowValueCalledInComposition")
@Composable
fun ProjectDetailScreen(
    navController: NavController,
    projectId: String,
    projectViewModel: ProjectViewModel,
    taskViewModel: TaskViewModel,
    userFriendsViewModel: UserFriendsViewModel,
    authViewModel: AuthViewModel
) {

    val project by projectViewModel.selectedProject.collectAsState()
    val loading by projectViewModel.loading.collectAsState()
    val error by projectViewModel.error.collectAsState()

    val tasks by taskViewModel.tasks.collectAsState()

    val friendsState by userFriendsViewModel.friendsState.collectAsState()

    var hasLoaded by remember { mutableStateOf(false) }
    val showProjectNotFound by remember(hasLoaded, project) {
        derivedStateOf { hasLoaded && project == null }
    }

    var showAddTeamMembersDialog by remember { mutableStateOf(false) }

    val sendInvitationState by projectViewModel.invitationActionState.collectAsState()

    LaunchedEffect(projectId, authViewModel.currentUserId.value) {
        val userId = authViewModel.currentUserId.value
        if (userId == null) return@LaunchedEffect

        projectViewModel.getProjectById(projectId)
        taskViewModel.loadTasksForProject(projectId)
        hasLoaded = true

        userFriendsViewModel.getFriends(userId)
    }

    Scaffold(
        topBar = {
            TopBar(
                navController = navController,
                onProfileClicked = { },
                onLogoutClicked = {
                    authViewModel.logout()
                    navController.navigate("login") {
                        popUpTo("login") { inclusive = true }
                    }
                },
                showBackArrow = true,
                onBackPressed = { navController.popBackStack() }
            )
        },
        bottomBar = { BottomNavigationBar(navController) },
        containerColor = backgroundColor
    ) { paddingValues ->
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            color = backgroundColor
        ) {
            when {
                loading -> Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                }

                error != null -> Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "Error: $error", color = Color.Red)
                }

                showProjectNotFound -> Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "Project not found", color = Color.Red)
                }

                project != null -> Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                ) {
                    Text(
                        text = project!!.title,
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        modifier = Modifier.padding(16.dp)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = "Due Date",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .background(
                                        shape = RoundedCornerShape(8.dp),
                                        color = mainAppColor
                                    )
                                    .padding(8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.DateRange,
                                    contentDescription = "Due Date",
                                    tint = Color.Black,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = project!!.dueDate,
                                style = MaterialTheme.typography.bodyLarge,
                                color = Color.White
                            )
                        }

                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = "Team Members",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .background(mainAppColor, shape = RoundedCornerShape(8.dp))
                                    .padding(8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    painter = painterResource(id = R.drawable.team),
                                    contentDescription = "Team Members",
                                    tint = Color.Black,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))

                            val totalMembers = project!!.teamMembers.size + 1
                            val memberText =
                                if (totalMembers == 1) "1 Member" else "$totalMembers Members"

                            Text(
                                text = memberText,
                                style = MaterialTheme.typography.bodyLarge,
                                color = Color.White
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Project Details",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = project!!.description,
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    Text(
                        text = "Tasks",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Column(
                        modifier = Modifier.padding(horizontal = 16.dp)
                    ) {
                        tasks.forEach { task ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = "Task Status",
                                    tint = if (task.isCompleted) Color.Green else Color.Gray
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = task.title,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color.White
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = {
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = mainAppColor
                        )
                    ) {
                        Text(text = "Add Task", color = Color.Black)
                    }

                    Button(
                        onClick = {
                            showAddTeamMembersDialog = true
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = mainAppColor
                        )
                    ) {
                        Text(text = "Add Team Members", color = Color.Black)
                    }
                }
            }
        }
    }


    if (showAddTeamMembersDialog) {
        AddTeamMembersDialog(
            showDialog = showAddTeamMembersDialog,
            onDismiss = {
                showAddTeamMembersDialog = false
                projectViewModel.clearInvitationActionState()
            },
            friends = friendsState?.getOrNull() ?: emptyList(),
            currentUserId = authViewModel.currentUserId.value ?: "",
            projectId = projectId,
            projectViewModel = projectViewModel,
            onAddTeamMember = { friendId ->
                val invitation = ProjectInvitation(
                    fromUserId = authViewModel.currentUserId.value ?: "",
                    toUserId = friendId,
                    projectId = projectId
                )
                projectViewModel.sendProjectInvitation(invitation)
            }
        )
    }
    sendInvitationState?.let { result ->
        AlertDialog(
            onDismissRequest = { projectViewModel.clearInvitationActionState() },
            title = { Text("Invitation Status") },
            text = {
                Text(
                    if (result.isSuccess) "Invitation sent successfully!"
                    else "Failed to send invitation: ${result.exceptionOrNull()?.message}"
                )
            },
            confirmButton = {
                Button(onClick = { projectViewModel.clearInvitationActionState() }) {
                    Text("OK")
                }
            }
        )
    }

}

@Composable
fun AddTeamMembersDialog(
    showDialog: Boolean,
    onDismiss: () -> Unit,
    friends: List<Friend>,
    currentUserId: String,
    projectId: String,
    projectViewModel: ProjectViewModel,
    onAddTeamMember: (String) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text(text = "Invite Team Members") },
            text = {
                Column {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        label = { Text("Search Friends") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    LazyColumn {
                        val filteredFriends = friends.filter { friend ->
                            friend.friendId != currentUserId && (
                                    friend.friendName.contains(searchQuery, ignoreCase = true) ||
                                            friend.friendEmail.contains(searchQuery, ignoreCase = true)
                                    )
                        }

                        items(filteredFriends) { friend ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onAddTeamMember(friend.friendId) }
                                    .padding(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(text = friend.friendName)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(text = friend.friendEmail, color = Color.Gray)
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(onClick = onDismiss) {
                    Text("Close")
                }
            }
        )
    }
}