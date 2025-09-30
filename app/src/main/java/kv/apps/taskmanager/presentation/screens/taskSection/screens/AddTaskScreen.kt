package kv.apps.taskmanager.presentation.screens.taskSection.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import kotlinx.coroutines.launch
import kv.apps.taskmanager.domain.model.Task
import kv.apps.taskmanager.domain.model.TeamMember
import kv.apps.taskmanager.presentation.navigation.Screen
import kv.apps.taskmanager.presentation.screens.taskSection.taskComposables.MemberSelectionModal
import kv.apps.taskmanager.presentation.screens.utils.shared.menuBars.BottomNavigationBar
import kv.apps.taskmanager.presentation.screens.utils.shared.menuBars.TopBar
import kv.apps.taskmanager.presentation.screens.utils.shared.uiComposables.AppDrawer
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
fun AddTaskScreen(
    navController: NavController,
    projectId: String,
    authViewModel: AuthViewModel,
    taskViewModel: TaskViewModel,
    projectViewModel: ProjectViewModel
) {
    var title by remember { mutableStateOf("") }
    var taskDetails by remember { mutableStateOf("") }
    var dueDate by remember { mutableStateOf<LocalDate?>(null) }
    var showError by remember { mutableStateOf(false) }
    var selectedUsers by remember { mutableStateOf<Set<String>>(emptySet()) }
    var searchQuery by remember { mutableStateOf("") }
    var showMemberSelection by remember { mutableStateOf(false) }

    val focusManager = LocalFocusManager.current
    val interactionSource = remember { MutableInteractionSource() }
    var isKeyboardVisible by remember { mutableStateOf(false) }


    val authUiState by authViewModel.uiState.collectAsState()
    val isLoggingOut = authUiState.isLoggingOut
    val currentUser = authViewModel.uiState.collectAsState().value.user
    val currentUserId = currentUser?.uid ?: ""

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val coroutineScope = rememberCoroutineScope()

    val projectUiState by projectViewModel.uiState.collectAsState()
    val projectMembersState = projectUiState.teamMembersWithDetails
    val isLoading = projectUiState.isTeamMembersLoading
    val error = projectUiState.errorMessage

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

    val dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
    val showDatePicker = rememberCustomDatePicker(
        initialDate = dueDate,
        onDateSelected = { selectedDate ->
            dueDate = selectedDate
        }
    )

    LaunchedEffect(projectId) {
        projectViewModel.fetchTeamMembersForProject(projectId)
    }

    AppDrawer(
        onProfileClicked = { navController.navigate(Screen.Profile.route) },
        onLogoutClicked = {
            authViewModel.logout()

        },
        drawerState = rememberDrawerState(initialValue = DrawerValue.Closed),
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
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clickable(
                        interactionSource = interactionSource,
                        indication = null
                    ) {
                        if (isKeyboardVisible) {
                            focusManager.clearFocus()
                        }
                    }
            ) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(backgroundColor)
                        .padding(paddingValues)
                        .padding(16.dp)
                ) {
                    item {
                        OfflineStatusIndicator()
                    }
                    if (isLoading) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(color = mainAppColor)
                            }
                        }
                    }

                    error?.let { errorMessage ->
                        item {
                            Text(
                                text = errorMessage,
                                color = Color.Red,
                                modifier = Modifier.padding(16.dp)
                            )
                        }
                    }

                    item {
                        Text(
                            "Task Title*",
                            style = MaterialTheme.typography.bodyLarge.copy(fontSize = 18.sp),
                            color = Color.White
                        )
                        TextField(
                            value = title,
                            onValueChange = { title = it },
                            placeholder = { Text("Enter task title") },
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = Color(0xFF2C2F38),
                                unfocusedContainerColor = Color(0xFF2C2F38),
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                cursorColor = Color.White
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .onFocusChanged { isKeyboardVisible = it.isFocused },
                            isError = showError && title.isBlank()
                        )
                        if (showError && title.isBlank()) {
                            Text(
                                text = "Title is required",
                                color = Color.Red,
                                fontSize = 12.sp,
                                modifier = Modifier.padding(start = 8.dp, top = 4.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            "Task Details",
                            style = MaterialTheme.typography.bodyLarge.copy(fontSize = 18.sp),
                            color = Color.White
                        )
                        TextField(
                            value = taskDetails,
                            onValueChange = { taskDetails = it },
                            placeholder = { Text("Enter task details") },
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = Color(0xFF2C2F38),
                                unfocusedContainerColor = Color(0xFF2C2F38),
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                cursorColor = Color.White
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 100.dp)
                                .onFocusChanged { isKeyboardVisible = it.isFocused },
                            maxLines = 3
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            "Due Date*",
                            style = MaterialTheme.typography.bodyLarge.copy(fontSize = 18.sp),
                            color = Color.White
                        )
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null,
                                    onClick = {
                                        isKeyboardVisible = false
                                        focusManager.clearFocus()
                                        showDatePicker()
                                    }
                                )
                                .padding(vertical = 12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = dueDate?.format(dateFormatter) ?: "Select Due Date",
                                color = if (dueDate == null) Color.Gray else Color.White,
                                style = MaterialTheme.typography.bodyLarge
                            )
                            Icon(
                                imageVector = Icons.Default.DateRange,
                                contentDescription = "Pick Date",
                                tint = Color.White
                            )
                        }
                        if (showError && dueDate == null) {
                            Text(
                                text = "Due date is required",
                                color = Color.Red,
                                fontSize = 12.sp,
                                modifier = Modifier.padding(start = 8.dp, top = 4.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            "Assign To",
                            style = MaterialTheme.typography.bodyLarge.copy(fontSize = 18.sp),
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        if (selectedUsers.isNotEmpty()) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                selectedUsers.take(3).forEach { userId ->
                                    allMembers.find { it.userId == userId }?.let { member ->
                                        Box(
                                            modifier = Modifier
                                                .size(40.dp)
                                                .background(mainAppColor, CircleShape)
                                                .clip(CircleShape),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = member.firstName.take(1) + member.lastName.take(1),
                                                color = Color.Black,
                                                style = MaterialTheme.typography.bodyLarge
                                            )
                                        }
                                    }
                                }
                                if (selectedUsers.size > 3) {
                                    Box(
                                        modifier = Modifier
                                            .size(40.dp)
                                            .background(mainAppColor, CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "+${selectedUsers.size - 3}",
                                            color = Color.Black
                                        )
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                        }

                        Button(
                            onClick = {
                                isKeyboardVisible = false
                                focusManager.clearFocus()
                                showMemberSelection = true
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF2C2F38),
                                contentColor = Color.White
                            )
                        ) {
                            Text("Select Team Members")
                        }
                    }

                    item {
                        Spacer(modifier = Modifier.height(24.dp))

                        Button(
                            onClick = {
                                when {
                                    title.isBlank() -> {
                                        showError = true
                                        focusManager.clearFocus()
                                    }
                                    dueDate == null -> showError = true
                                    else -> {
                                        val newTask = Task(
                                            id = "",
                                            title = title,
                                            taskDetails = taskDetails,
                                            dueDate = dueDate!!.toString(),
                                            isCompleted = false,
                                            assignedTo = selectedUsers.toList(),
                                            projectId = projectId
                                        )
                                        taskViewModel.addTaskToProject(projectId, newTask)
                                        navController.popBackStack()
                                    }
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = mainAppColor,
                                contentColor = Color.Black
                            ),
                            enabled = !isLoading
                        ) {
                            Text("Save Task", color = Color.Black)
                        }
                    }
                }
            }
        }
    }

    if (showMemberSelection) {
        MemberSelectionModal(
            showMemberSelection = true,
            onDismiss = { showMemberSelection = false },
            searchQuery = searchQuery,
            onSearchQueryChange = { searchQuery = it },
            selectedUsers = selectedUsers,
            onUserSelected = { userId ->
                selectedUsers = if (selectedUsers.contains(userId)) {
                    selectedUsers - userId
                } else {
                    selectedUsers + userId
                }
            },
            filteredMembers = filteredMembers,
            onConfirmSelection = {
                showMemberSelection = false
            },
            assignedMembers = emptySet()
        )
    }
}