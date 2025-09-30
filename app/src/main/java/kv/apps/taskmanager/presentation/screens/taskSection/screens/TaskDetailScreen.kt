package kv.apps.taskmanager.presentation.screens.taskSection.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Error
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ElevatedAssistChip
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextFieldDefaults
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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import kotlinx.coroutines.launch
import kv.apps.taskmanager.domain.model.TeamMember
import kv.apps.taskmanager.presentation.navigation.Screen
import kv.apps.taskmanager.presentation.screens.taskSection.taskComposables.CommentItem
import kv.apps.taskmanager.presentation.screens.taskSection.taskComposables.CommentsHeader
import kv.apps.taskmanager.presentation.screens.taskSection.taskComposables.MemberSelectionModal
import kv.apps.taskmanager.presentation.screens.utils.shared.avatar.MemberAvatarsRow
import kv.apps.taskmanager.presentation.screens.utils.shared.menuBars.TopBar
import kv.apps.taskmanager.presentation.screens.utils.shared.uiComposables.AppDrawer
import kv.apps.taskmanager.presentation.screens.utils.shared.uiComposables.ConfirmationDialog
import kv.apps.taskmanager.presentation.screens.utils.shared.uiComposables.rememberCustomDatePicker
import kv.apps.taskmanager.presentation.viewmodel.auth.AuthViewModel
import kv.apps.taskmanager.presentation.viewmodel.project.ProjectViewModel
import kv.apps.taskmanager.presentation.viewmodel.task.TaskViewModel
import kv.apps.taskmanager.presentation.components.OfflineStatusIndicator
import kv.apps.taskmanager.theme.backgroundColor
import kv.apps.taskmanager.theme.mainAppColor
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskDetailScreen(
    taskId: String,
    projectId: String,
    onBackPressed: () -> Unit,
    taskViewModel: TaskViewModel,
    projectViewModel: ProjectViewModel,
    authViewModel: AuthViewModel,
    navController: NavController
) {
    val uiState by taskViewModel.uiState.collectAsState()
    val task = uiState.selectedTask
    val loading = uiState.isLoading
    val errorMessage = uiState.errorMessage
    val projectUiState by projectViewModel.uiState.collectAsState()
    val projectTeamMembers = projectUiState.selectedProject
    val authState by authViewModel.uiState.collectAsState()
    val isLoggingOut = authState.isLoggingOut

    val commentsState by taskViewModel.commentsState.collectAsState()
    var newCommentText by remember { mutableStateOf("") }
    val currentTaskComments = commentsState[taskId] ?: emptyList()
    val currentUserId = authState.user?.uid

    var showCompletionConfirmation by remember { mutableStateOf(false) }
    var pendingCompletionStatus by remember { mutableStateOf(false) }

    var showContent by remember { mutableStateOf(false) }
    val canEdit = remember(projectTeamMembers, currentUserId, task) {
        derivedStateOf {
            projectTeamMembers?.createdBy == currentUserId ||
                    task?.assignedTo?.contains(currentUserId) == true
        }
    }

    var areCommentsExpanded by remember { mutableStateOf(true) }

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    var isEditing by remember { mutableStateOf(false) }
    var editedTitle by remember { mutableStateOf("") }
    var editedDetails by remember { mutableStateOf("") }
    var editedDueDate by remember { mutableStateOf("") }
    var isCompleted by remember { mutableStateOf(false) }
    val projectMembersState = projectUiState.teamMembersWithDetails
    val currentUser = authViewModel.uiState.collectAsState().value.user
    var searchQuery by remember { mutableStateOf("") }
    var selectedMembers by remember { mutableStateOf<Set<String>>(emptySet()) }
    var showMemberSelection by remember { mutableStateOf(false) }

    var showLoading by remember { mutableStateOf(true) }

    val allMembers = remember(projectMembersState, currentUser) {
        val members = projectMembersState.toMutableList()
        if (currentUser != null && members.none { it.userId == currentUserId }) {
            members.add(
                0,
                (projectMembersState.find { it.userId == currentUserId } ?: currentUser.copy(
                    firstName = currentUser.firstName,
                    lastName = currentUser.lastName,
                    email = currentUser.email
                )) as TeamMember
            )
        }
        members
    }

    val filteredMembers = allMembers.filter { member ->
        member.firstName.contains(searchQuery, ignoreCase = true) ||
                member.lastName.contains(searchQuery, ignoreCase = true) ||
                member.email?.contains(searchQuery, ignoreCase = true) == true
    }

    val showDatePicker = rememberCustomDatePicker(
        initialDate = if (editedDueDate.isNotEmpty()) {
            try {
                val formatter = DateTimeFormatter.ofPattern("[dd/MM/yyyy][yyyy-MM-dd]")
                LocalDate.parse(editedDueDate, formatter)
            } catch (_: Exception) {
                null
            }
        } else {
            null
        },
        onDateSelected = { selectedDate ->
            val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
            editedDueDate = selectedDate.format(formatter)
            if (!isEditing) {
                taskViewModel.updateTaskInProject(
                    projectId,
                    task?.copy(dueDate = editedDueDate) ?: return@rememberCustomDatePicker
                )
            }
        }
    )

    LaunchedEffect(taskId, projectId) {
        taskViewModel.fetchTaskByIdFromProject(projectId, taskId)
        taskViewModel.fetchTaskMembers(projectId, taskId)
        taskViewModel.fetchAssignedUsersInitials(projectId, taskId)
        taskViewModel.loadTaskComments(projectId, taskId)
        taskViewModel.observeTaskComments(projectId, taskId)

        kotlinx.coroutines.delay(1000)
        showLoading = false

    }

    LaunchedEffect(task) {
        if (task != null) {
            showContent = true
            if (!isEditing) {
                editedTitle = task.title
                editedDetails = task.taskDetails
                editedDueDate = task.dueDate
                isCompleted = task.isCompleted
                selectedMembers = task.assignedTo.toSet()
            }
        }
    }

    LaunchedEffect(uiState.isLoading) {
        showContent = !uiState.isLoading
    }

    val alpha by animateFloatAsState(
        targetValue = if (showContent) 1f else 0f,
        animationSpec = tween(durationMillis = 300)
    )

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
                    showBackArrow = true,
                    onBackPressed = onBackPressed
                )
            },
            bottomBar = {
            },
            containerColor = backgroundColor,
            snackbarHost = { SnackbarHost(snackbarHostState) }
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(backgroundColor)
                    .padding(paddingValues)
            ) {
                when {
                    showLoading || loading -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(color = mainAppColor)
                        }
                    }

                    errorMessage != null -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    Icons.Default.Error,
                                    contentDescription = "Error",
                                    tint = Color.Red,
                                    modifier = Modifier.size(48.dp)
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Text("Failed to load task", color = Color.White)
                                Text(errorMessage, color = Color.LightGray)
                                Spacer(modifier = Modifier.height(16.dp))
                                TextButton(
                                    onClick = {
                                        taskViewModel.fetchTaskByIdFromProject(projectId, taskId)
                                        taskViewModel.fetchTaskMembers(projectId, taskId)
                                        taskViewModel.fetchAssignedUsersInitials(projectId, taskId)
                                        taskViewModel.loadTaskComments(projectId, taskId)
                                    },
                                    colors = ButtonDefaults.textButtonColors(
                                        contentColor = mainAppColor
                                    )
                                ) {
                                    Text("Retry")
                                }
                            }
                        }
                    }

                    task != null -> {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 16.dp)
                                .alpha(alpha)
                        ) {
                            OfflineStatusIndicator(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp)
                            )
                            LazyColumn(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(16.dp)
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
                                                textStyle = MaterialTheme.typography.titleLarge.copy(
                                                    color = Color.White,
                                                    fontWeight = FontWeight.Bold
                                                ),
                                                colors = TextFieldDefaults.colors(
                                                    focusedContainerColor = Color(0xFF3A3E4B),
                                                    unfocusedContainerColor = Color(0xFF3A3E4B),
                                                    focusedTextColor = Color.White,
                                                    unfocusedTextColor = Color.White
                                                )
                                            )
                                        } else {
                                            Text(
                                                text = editedTitle,
                                                style = MaterialTheme.typography.titleLarge.copy(
                                                    color = Color.White,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            )
                                        }

                                        if (canEdit.value) {
                                            EditSaveButtons(
                                                isEditing = isEditing,
                                                onEditClick = { isEditing = true },
                                                onSaveClick = {
                                                    taskViewModel.updateTaskInProject(
                                                        projectId,
                                                        task.copy(
                                                            title = editedTitle,
                                                            taskDetails = editedDetails,
                                                            dueDate = editedDueDate,
                                                            isCompleted = isCompleted,
                                                            assignedTo = selectedMembers.toList()
                                                        )
                                                    )
                                                    isEditing = false
                                                },
                                                onCancelClick = {
                                                    isEditing = false
                                                    editedTitle = task.title
                                                    editedDetails = task.taskDetails
                                                    editedDueDate = task.dueDate
                                                    isCompleted = task.isCompleted
                                                    selectedMembers =
                                                        task.assignedTo.toSet()
                                                }
                                            )
                                        }
                                    }
                                }

                                item {
                                    StatusChip(
                                        isCompleted = isCompleted,
                                        canEdit = canEdit.value,
                                        onCheckedChange = { newStatus ->
                                            if (isEditing) {
                                                isCompleted = newStatus
                                            } else {
                                                pendingCompletionStatus = newStatus
                                                showCompletionConfirmation = true
                                            }
                                        }
                                    )

                                    if (showCompletionConfirmation) {
                                        ConfirmationDialog(
                                            title = if (pendingCompletionStatus) "Mark as Completed?" else "Mark as In Progress?",
                                            message = if (pendingCompletionStatus)
                                                "Are you sure you want to mark this task as completed?"
                                            else
                                                "Are you sure you want to mark this task as in progress?",
                                            onDismissRequest = {
                                                showCompletionConfirmation = false
                                            },
                                            onConfirm = {
                                                isCompleted = pendingCompletionStatus
                                                taskViewModel.updateTaskInProject(
                                                    projectId,
                                                    task.copy(isCompleted = pendingCompletionStatus)
                                                        ?: return@ConfirmationDialog
                                                )
                                                showCompletionConfirmation = false
                                            }
                                        )
                                    }
                                }

                                item {
                                    DueDateSection(
                                        dueDate = editedDueDate,
                                        isEditing = isEditing && canEdit.value,
                                        onDateClick = { showDatePicker() }
                                    )
                                }

                                item {
                                    AssignedMembersSection(
                                        assignedTo = if (isEditing) selectedMembers.toList() else task.assignedTo,
                                        initialsMap = uiState.taskAssignedUsersInitials,
                                        isLoading = false,
                                        canEdit = canEdit.value,
                                        isEditing = isEditing,
                                        onEditClick = { showMemberSelection = true }
                                    )
                                }

                                item {
                                    TaskDetailsSection(
                                        details = editedDetails,
                                        isEditing = isEditing && canEdit.value,
                                        onDetailsChange = { editedDetails = it }
                                    )
                                }

                                if (currentTaskComments.isNotEmpty()) {
                                    item {
                                        CommentsHeader(
                                            commentCount = currentTaskComments.size,
                                            isExpanded = areCommentsExpanded,
                                            onToggleExpanded = {
                                                areCommentsExpanded = !areCommentsExpanded
                                            }
                                        )
                                    }

                                    if (areCommentsExpanded) {
                                        items(currentTaskComments) { comment ->
                                            CommentItem(
                                                comment = comment,
                                                currentUserId = currentUserId,
                                                onEditComment = { editedText ->
                                                    taskViewModel.editComment(
                                                        projectId,
                                                        taskId,
                                                        comment.id,
                                                        editedText
                                                    )
                                                },
                                                onDeleteComment = {
                                                    taskViewModel.deleteComment(
                                                        projectId,
                                                        taskId,
                                                        comment.id
                                                    )
                                                },
                                                modifier = Modifier.padding(vertical = 8.dp)
                                            )
                                        }
                                    }
                                }
                            }

                            CommentInput(
                                newCommentText = newCommentText,
                                onNewCommentTextChange = { newCommentText = it },
                                onSendComment = {
                                    if (newCommentText.isNotBlank()) {
                                        taskViewModel.addComment(
                                            projectId,
                                            taskId,
                                            newCommentText
                                        )
                                        newCommentText = ""
                                    }
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 16.dp, bottom = 16.dp)
                            )
                        }
                    }
                }
            }
        }

        if (showMemberSelection && canEdit.value) {
            MemberSelectionModal(
                showMemberSelection = true,
                onDismiss = { showMemberSelection = false },
                searchQuery = searchQuery,
                onSearchQueryChange = { searchQuery = it },
                selectedUsers = selectedMembers,
                onUserSelected = { userId ->
                    selectedMembers = if (selectedMembers.contains(userId)) {
                        selectedMembers - userId
                    } else {
                        selectedMembers + userId
                    }
                },
                filteredMembers = filteredMembers,
                onConfirmSelection = {
                    showMemberSelection = false
                    if (!isEditing) {
                        task?.let { currentTask ->
                            taskViewModel.updateTaskInProject(
                                projectId,
                                currentTask.copy(assignedTo = selectedMembers.toList())
                            )
                        }
                    }
                },
                assignedMembers = task?.assignedTo?.toSet() ?: emptySet()
            )
        }
    }
}

@Composable
private fun CommentInput(
    newCommentText: String,
    onNewCommentTextChange: (String) -> Unit,
    onSendComment: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        OutlinedTextField(
            value = newCommentText,
            onValueChange = onNewCommentTextChange,
            modifier = Modifier
                .weight(1f)
                .background(Color(0xFF2D303B), RoundedCornerShape(24.dp)),
            placeholder = {
                Text(
                    "Write a comment...",
                    color = Color.Gray.copy(alpha = 0.7f),
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color.Transparent,
                unfocusedBorderColor = Color.Transparent,
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent
            ),
            textStyle = MaterialTheme.typography.bodyMedium,
            shape = RoundedCornerShape(24.dp),
            singleLine = false,
            maxLines = 3
        )

        Spacer(modifier = Modifier.width(12.dp))

        IconButton(
            onClick = onSendComment,
            enabled = newCommentText.isNotBlank(),
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(
                    if (newCommentText.isNotBlank()) mainAppColor
                    else mainAppColor.copy(alpha = 0.3f)
                )
        ) {
            Icon(
                Icons.AutoMirrored.Filled.Send,
                contentDescription = "Send comment",
                tint = if (newCommentText.isNotBlank()) Color.White else Color.White.copy(alpha = 0.5f),
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
private fun EditSaveButtons(
    isEditing: Boolean,
    onEditClick: () -> Unit,
    onSaveClick: () -> Unit,
    onCancelClick: () -> Unit
) {
    if (isEditing) {
        Row {
            IconButton(
                onClick = onSaveClick,
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(mainAppColor.copy(alpha = 0.2f))
            ) {
                Icon(Icons.Default.Check, "Save", tint = Color.Green)
            }
            Spacer(modifier = Modifier.width(8.dp))
            IconButton(
                onClick = onCancelClick,
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(mainAppColor.copy(alpha = 0.2f))
            ) {
                Icon(Icons.Default.Close, "Cancel", tint = Color.Red)
            }
        }
    } else {
        IconButton(
            onClick = onEditClick,
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(mainAppColor.copy(alpha = 0.2f))
        ) {
            Icon(Icons.Default.Edit, "Edit", tint = mainAppColor)
        }
    }
}

@Composable
private fun StatusChip(
    isCompleted: Boolean,
    canEdit: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text("Status", style = MaterialTheme.typography.bodyMedium, color = Color.White)
        if (canEdit) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = if (isCompleted) "Completed" else "In Progress",
                    color = if (isCompleted) Color.Green else mainAppColor,
                    modifier = Modifier.padding(end = 8.dp)
                )
                Switch(
                    checked = isCompleted,
                    onCheckedChange = onCheckedChange,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.Green,
                        checkedTrackColor = Color.Green.copy(alpha = 0.5f),
                        uncheckedThumbColor = mainAppColor,
                        uncheckedTrackColor = mainAppColor.copy(alpha = 0.5f)
                    )
                )
            }
        } else {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .background(
                        color = if (isCompleted) Color.Green.copy(alpha = 0.2f)
                        else mainAppColor.copy(alpha = 0.2f)
                    )
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Text(
                    text = if (isCompleted) "Completed" else "In Progress",
                    color = if (isCompleted) Color.Green else mainAppColor,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}


@Composable
private fun DueDateSection(
    dueDate: String,
    isEditing: Boolean,
    onDateClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text("Due Date", style = MaterialTheme.typography.bodyMedium, color = Color.White)
        if (isEditing) {
            ElevatedAssistChip(
                onClick = onDateClick,
                label = { Text(dueDate.ifEmpty { "Select date" }, color = Color.White) },
                leadingIcon = { Icon(Icons.Default.DateRange, "Due date", tint = mainAppColor) },
                colors = AssistChipDefaults.assistChipColors(containerColor = Color(0xFF3A3E4B))
            )
        } else {
            Text(dueDate, style = MaterialTheme.typography.bodyLarge, color = Color.White)
        }
    }
}

@Composable
private fun AssignedMembersSection(
    assignedTo: List<String>,
    initialsMap: Map<String, String>,
    isLoading: Boolean,
    canEdit: Boolean,
    isEditing: Boolean,
    onEditClick: () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Assigned To", style = MaterialTheme.typography.bodyMedium, color = Color.White)
            if (canEdit) {
                TextButton(
                    onClick = onEditClick,
                    colors = ButtonDefaults.textButtonColors(contentColor = mainAppColor)
                ) {
                    Text(if (isEditing) "Select Members" else "Edit")
                }
            }
        }
        if (assignedTo.isEmpty()) {
            Text(
                "Unassigned",
                style = MaterialTheme.typography.bodyLarge,
                color = Color.Gray,
                modifier = Modifier.padding(top = 8.dp)
            )
        } else {
            MemberAvatarsRow(
                userIds = assignedTo,
                initialsMap = initialsMap,
                isLoading = isLoading
            )
        }
    }
}

@Composable
private fun TaskDetailsSection(
    details: String,
    isEditing: Boolean,
    onDetailsChange: (String) -> Unit
) {
    Column {
        Text(
            "Task Details",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
        Spacer(modifier = Modifier.height(8.dp))
        if (isEditing) {
            OutlinedTextField(
                value = details,
                onValueChange = onDetailsChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 120.dp),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color(0xFF3A3E4B),
                    unfocusedContainerColor = Color(0xFF3A3E4B),
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                )
            )
        } else {
            Text(
                details,
                style = MaterialTheme.typography.bodyLarge,
                color = Color.White,
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF3A3E4B), RoundedCornerShape(12.dp))
                    .padding(16.dp)
            )
        }
    }
}