package kv.apps.taskmanager.presentation.screens.projectSection.screens

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import kotlinx.coroutines.launch
import kv.apps.taskmanager.R
import kv.apps.taskmanager.domain.model.ProjectInvitation
import kv.apps.taskmanager.presentation.navigation.Screen
import kv.apps.taskmanager.presentation.screens.projectSection.projectComposables.ProjectSelectionModal
import kv.apps.taskmanager.presentation.screens.taskSection.taskComposables.TaskItem
import kv.apps.taskmanager.presentation.screens.utils.shared.menuBars.BottomNavigationBar
import kv.apps.taskmanager.presentation.screens.utils.shared.menuBars.TopBar
import kv.apps.taskmanager.presentation.screens.utils.shared.uiComposables.AppDrawer
import kv.apps.taskmanager.presentation.screens.utils.shared.uiComposables.ConfirmationDialog
import kv.apps.taskmanager.presentation.screens.utils.shared.uiComposables.rememberCustomDatePicker
import kv.apps.taskmanager.presentation.viewmodel.auth.AuthViewModel
import kv.apps.taskmanager.presentation.viewmodel.project.ProjectViewModel
import kv.apps.taskmanager.presentation.viewmodel.task.TaskViewModel
import kv.apps.taskmanager.presentation.viewmodel.userFriends.UserFriendsViewModel
import kv.apps.taskmanager.theme.backgroundColor
import kv.apps.taskmanager.theme.mainAppColor
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("StateFlowValueCalledInComposition")
@Composable
fun ProjectDetailScreen(
    navController: NavController,
    projectId: String,
    projectViewModel: ProjectViewModel,
    taskViewModel: TaskViewModel,
    authViewModel: AuthViewModel,
    userFriendsViewModel: UserFriendsViewModel
) {
    val project by projectViewModel.selectedProject.collectAsState()
    val loading by projectViewModel.loading.collectAsState()
    val error by projectViewModel.error.collectAsState()
    val uiState = taskViewModel.uiState.collectAsState()
    val tasks = uiState.value.tasks.filter { it.projectId == projectId }
    val authUiState by authViewModel.uiState.collectAsState()
    val isLoggingOut = authUiState.isLoggingOut

    var showFriendSelection by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    var selectedFriends by remember { mutableStateOf<Set<String>>(emptySet()) }
    var showCompletionConfirmation by remember { mutableStateOf(false) }
    var pendingCompletionStatus by remember { mutableStateOf(false) }

    val friendsState by userFriendsViewModel.uiState.collectAsState()
    val friends = remember(friendsState.friends) {
        friendsState.friends?.getOrNull() ?: emptyList()
    }

    val filteredFriends = remember(friends, project, searchQuery) {
        if (project == null) {
            emptyList()
        } else {
            friends.filter { friend ->
                (friend.displayName.contains(searchQuery, ignoreCase = true) ||
                        friend.email.contains(searchQuery, ignoreCase = true) )
            }.filter { friend ->
                !project!!.teamMembers.contains(friend.friendId)
            }
        }
    }

    val currentUserId = authUiState.user?.uid
    val canEdit = remember(project, currentUserId) {
        derivedStateOf { project?.createdBy == currentUserId }
    }

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val coroutineScope = rememberCoroutineScope()

    var hasLoaded by remember { mutableStateOf(false) }
    val showProjectNotFound by remember(hasLoaded, project) {
        derivedStateOf { hasLoaded && project == null }
    }

    var isEditing by remember { mutableStateOf(false) }
    var editedTitle by remember { mutableStateOf("") }
    var editedDescription by remember { mutableStateOf("") }
    var editedDueDate by remember { mutableStateOf("") }

    val customFont = FontFamily(Font(R.font.pilat))

    LaunchedEffect(projectId, authUiState.user?.uid) {
        val userId = authUiState.user?.uid
        if (userId == null) return@LaunchedEffect

        projectViewModel.getProjectById(projectId)
        taskViewModel.loadTasksForProject(projectId)
        userFriendsViewModel.getFriends(userId)
        hasLoaded = true
    }

    LaunchedEffect(project) {
        if (project != null && !isEditing) {
            editedTitle = project!!.title
            editedDescription = project!!.description
            editedDueDate = project!!.dueDate
        }
    }

    if (loading) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(backgroundColor),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(color = mainAppColor)
        }
        return
    }

    if (showFriendSelection) {
        ProjectSelectionModal(
            showFriendSelection = true,
            onDismiss = {
                showFriendSelection = false
                selectedFriends = emptySet()
            },
            searchQuery = searchQuery,
            onSearchQueryChange = { searchQuery = it },
            selectedFriends = selectedFriends,
            onFriendSelected = { friendId ->
                selectedFriends = if (selectedFriends.contains(friendId)) {
                    selectedFriends - friendId
                } else {
                    selectedFriends + friendId
                }
            },
            filteredFriends = filteredFriends,
            showSendButton = true,
            onSendInvitations = {
                val currentUserId = authViewModel.uiState.value.user?.uid ?: ""
                selectedFriends.forEach { friendId ->
                    val invitation = ProjectInvitation(
                        invitationId = "inv_${friendId}_${System.currentTimeMillis()}",
                        fromUserId = currentUserId,
                        toUserId = friendId,
                        projectId = projectId,
                        status = "Pending"
                    )
                    projectViewModel.sendProjectInvitation(invitation)
                }
            }
        )
    }

    if (showCompletionConfirmation) {
        ConfirmationDialog(
            title = if (pendingCompletionStatus) "Mark Project as Complete?" else "Mark Project as In Progress?",
            message = if (pendingCompletionStatus)
                "Are you sure you want to mark this project as completed?"
            else
                "Are you sure you want to reopen this project?",
            onDismissRequest = { showCompletionConfirmation = false },
            onConfirm = {
                projectViewModel.updateProject(
                    projectId = projectId,
                    project!!.copy(isCompleted = pendingCompletionStatus)
                )
                showCompletionConfirmation = false

                if (pendingCompletionStatus) {
                    navController.navigate(Screen.CompletedProjectDetail.createRoute(projectId)) {
                        popUpTo(Screen.ProjectDetail.createRoute(projectId)) { inclusive = true }
                    }
                }
            }
        )
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
            containerColor = backgroundColor,
            contentWindowInsets = WindowInsets(0.dp)
        ) { paddingValues ->
            Surface(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                color = backgroundColor
            ) {
                when {
                    error != null -> Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Error: $error", color = Color.Red)
                    }

                    showProjectNotFound -> Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Project not found", color = Color.Red)
                    }

                    project != null -> LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp)
                    ) {
                        item {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                if (isEditing) {
                                    OutlinedTextField(
                                        value = editedTitle,
                                        onValueChange = { editedTitle = it },
                                        modifier = Modifier.weight(1f),
                                        textStyle = TextStyle(
                                            fontSize = 18.sp,
                                            fontFamily = customFont,
                                            fontWeight = FontWeight.Light,
                                            letterSpacing = 1.7.sp,
                                            color = Color.White
                                        )
                                    )
                                } else {
                                    Text(
                                        text = editedTitle.uppercase(),
                                        style = TextStyle(
                                            fontSize = 18.sp,
                                            fontFamily = customFont,
                                            fontWeight = FontWeight.Light,
                                            letterSpacing = 1.7.sp,
                                            color = Color.White
                                        ),
                                        fontWeight = FontWeight.Bold
                                    )
                                }

                                if (canEdit.value) {
                                    if (isEditing) {
                                        Row {
                                            IconButton(
                                                onClick = {
                                                    projectViewModel.updateProject(
                                                        projectId = projectId,
                                                        project!!.copy(
                                                            title = editedTitle,
                                                            description = editedDescription,
                                                            dueDate = editedDueDate
                                                        )
                                                    )
                                                    isEditing = false
                                                }
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Check,
                                                    contentDescription = "Save",
                                                    tint = Color.Green
                                                )
                                            }
                                            Spacer(modifier = Modifier.width(8.dp))
                                            IconButton(
                                                onClick = {
                                                    isEditing = false
                                                    editedTitle = project!!.title
                                                    editedDescription = project!!.description
                                                    editedDueDate = project!!.dueDate
                                                }
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Close,
                                                    contentDescription = "Cancel",
                                                    tint = Color.Red
                                                )
                                            }
                                        }
                                    } else {
                                        IconButton(
                                            onClick = { isEditing = true }
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Edit,
                                                contentDescription = "Edit",
                                                tint = Color.White
                                            )
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 16.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(16.dp))
                                        .background(
                                            if (project!!.isCompleted) Color.Green.copy(alpha = 0.2f)
                                            else mainAppColor.copy(alpha = 0.2f)
                                        )
                                        .padding(horizontal = 16.dp, vertical = 8.dp)
                                ) {
                                    Text(
                                        text = if (project!!.isCompleted) "COMPLETED" else "IN PROGRESS",
                                        color = if (project!!.isCompleted) Color.Green else mainAppColor,
                                        fontWeight = FontWeight.Bold
                                    )
                                }

                                if (canEdit.value) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Text(
                                            text = if (project!!.isCompleted) "Reopen" else "Complete",
                                            color = Color.White
                                        )
                                        Switch(
                                            checked = project!!.isCompleted,
                                            onCheckedChange = { newStatus ->
                                                pendingCompletionStatus = newStatus
                                                showCompletionConfirmation = true
                                            },
                                            colors = SwitchDefaults.colors(
                                                checkedThumbColor = Color.Green,
                                                checkedTrackColor = Color.Green.copy(alpha = 0.5f),
                                                uncheckedThumbColor = mainAppColor,
                                                uncheckedTrackColor = mainAppColor.copy(alpha = 0.5f)
                                            )
                                        )
                                    }
                                }
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
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

                                    if (isEditing && canEdit.value) {
                                        val customDatePicker = rememberCustomDatePicker(
                                            initialDate = if (editedDueDate.isNotEmpty()) {
                                                try {
                                                    LocalDate.parse(editedDueDate, DateTimeFormatter.ofPattern("dd/MM/yyyy"))
                                                } catch (_: Exception) {
                                                    null
                                                }
                                            } else {
                                                null
                                            },
                                            onDateSelected = { selectedDate ->
                                                editedDueDate = selectedDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
                                            }
                                        )

                                        Box(
                                            modifier = Modifier
                                                .background(
                                                    color = mainAppColor.copy(alpha = 0.3f),
                                                    shape = RoundedCornerShape(8.dp)
                                                )
                                                .padding(horizontal = 12.dp, vertical = 8.dp)
                                                .clickable { customDatePicker() },
                                            content = {
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                                ) {
                                                    Text(
                                                        text = editedDueDate.ifEmpty { "Tap to select date" },
                                                        style = MaterialTheme.typography.bodyLarge.copy(
                                                            color = Color.White,
                                                            fontWeight = if (editedDueDate.isEmpty()) FontWeight.Light else FontWeight.Normal
                                                        )
                                                    )
                                                    Icon(
                                                        imageVector = Icons.Default.Edit,
                                                        contentDescription = "Edit date",
                                                        tint = Color.White,
                                                        modifier = Modifier.size(16.dp)
                                                    )
                                                }
                                            }
                                        )
                                    } else {
                                        Text(
                                            text = editedDueDate,
                                            style = MaterialTheme.typography.bodyLarge,
                                            color = Color.White
                                        )
                                    }
                                }

                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable {
                                            navController.navigate(Screen.ProjectMembers.createRoute(projectId))
                                        }
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
                                            painter = painterResource(id = R.drawable.team2),
                                            contentDescription = "Team Members",
                                            tint = Color.Black,
                                            modifier = Modifier.size(24.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    val totalMembers = project!!.teamMembers.size
                                    val memberText = if (totalMembers == 1) "1 Member" else "$totalMembers Members"
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
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            if (isEditing && canEdit.value) {
                                OutlinedTextField(
                                    value = editedDescription,
                                    onValueChange = { editedDescription = it },
                                    modifier = Modifier.fillMaxWidth(),
                                    maxLines = 4,
                                    textStyle = TextStyle(
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Light,
                                        letterSpacing = 1.5.sp,
                                        color = Color.White
                                    ),
                                )
                            } else {
                                Text(
                                    text = editedDescription,
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = Color.White
                                )
                            }

                            Spacer(modifier = Modifier.height(24.dp))

                            Text(
                                text = "Tasks (${tasks.size})",
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                ),
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                        }

                        if (tasks.isEmpty()) {
                            item {
                                Text(
                                    text = "No tasks yet",
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        color = Color.Gray
                                    ),
                                    modifier = Modifier.padding(vertical = 16.dp)
                                )
                            }
                        } else {
                            items(tasks) { task ->
                                TaskItem(
                                    task = task,
                                    onTaskClicked = {
                                        taskViewModel.clearTaskState()
                                        if (task.isCompleted) {
                                            navController.navigate(
                                                Screen.CompletedTaskDetail.createRoute(
                                                    task.id,
                                                    projectId
                                                )
                                            )
                                        } else {
                                            navController.navigate(
                                                Screen.TaskDetail.createRoute(
                                                    task.id,
                                                    projectId
                                                )
                                            )
                                        }
                                    },
                                    taskViewModel = taskViewModel,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 8.dp),
                                    authViewModel = authViewModel,
                                    projectCreatedBy = project?.createdBy ?: "",
                                    onDelete = {
                                        taskViewModel.deleteTaskFromProject(
                                            taskId = task.id,
                                            projectId = projectId
                                        )
                                    }
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                            }
                        }
                        if (canEdit.value) {
                            item {
                                Column(
                                    verticalArrangement = Arrangement.spacedBy(8.dp),
                                    modifier = Modifier.padding(top = 16.dp)
                                ) {
                                    Button(
                                        onClick = {
                                            navController.navigate(Screen.AddTask.createRoute(projectId))
                                        },
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = mainAppColor
                                        )
                                    ) {
                                        Text(text = "Add Task", color = Color.Black)
                                    }

                                    Button(
                                        onClick = {
                                            showFriendSelection = true
                                            selectedFriends = emptySet()
                                        },
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = mainAppColor
                                        ),
                                        enabled = true
                                    ) {
                                        Text(text = "Invite Members", color = Color.Black)
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