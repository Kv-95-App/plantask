package kv.apps.taskmanager.presentation.screens.projectSection.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import kv.apps.taskmanager.domain.model.Project
import kv.apps.taskmanager.domain.model.ProjectInvitation
import kv.apps.taskmanager.presentation.navigation.Screen
import kv.apps.taskmanager.presentation.screens.projectSection.projectComposables.ProjectSelectionModal
import kv.apps.taskmanager.presentation.screens.utils.shared.menuBars.TopBar
import kv.apps.taskmanager.presentation.screens.utils.shared.uiComposables.AppDrawer
import kv.apps.taskmanager.presentation.screens.utils.shared.uiComposables.rememberCustomDatePicker
import kv.apps.taskmanager.presentation.viewmodel.auth.AuthViewModel
import kv.apps.taskmanager.presentation.viewmodel.project.ProjectViewModel
import kv.apps.taskmanager.presentation.viewmodel.userFriends.UserFriendsViewModel
import kv.apps.taskmanager.theme.backgroundColor
import kv.apps.taskmanager.theme.mainAppColor
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddProjectScreen(
    navController: NavController,
    projectViewModel: ProjectViewModel,
    authViewModel: AuthViewModel,
    userFriendsViewModel: UserFriendsViewModel
) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var dueDate by remember { mutableStateOf<String?>(null) }
    var showError by remember { mutableStateOf(false) }
    var isPastDate by remember { mutableStateOf(false) }
    var selectedFriends by remember { mutableStateOf<Set<String>>(emptySet()) }
    var searchQuery by remember { mutableStateOf("") }
    var showFriendSelection by remember { mutableStateOf(false) }

    val focusManager = LocalFocusManager.current
    val interactionSource = remember { MutableInteractionSource() }
    var isKeyboardVisible by remember { mutableStateOf(false) }

    val isLoggingOut = authViewModel.uiState.collectAsState().value.isLoggingOut
    val projectId by projectViewModel.projects.collectAsState().value.lastOrNull()?.id?.let { id ->
        remember { mutableStateOf(id) }
    } ?: remember { mutableStateOf("") }

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val coroutineScope = rememberCoroutineScope()

    val friendsState by userFriendsViewModel.uiState.collectAsState()
    val friends = remember(friendsState.friends) {
        friendsState.friends?.getOrNull() ?: emptyList()
    }

    val filteredFriends = friends.filter { friend ->
        friend.displayName.contains(searchQuery, ignoreCase = true) ||
                friend.email.contains(searchQuery, ignoreCase = true)
    }

    val dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")

    val showDatePicker = rememberCustomDatePicker(
        initialDate = dueDate?.let { LocalDate.parse(it, dateFormatter) },
        onDateSelected = { selectedDate ->
            val currentDate = LocalDate.now()
            if (selectedDate.isBefore(currentDate)) {
                isPastDate = true
                dueDate = null
            } else {
                isPastDate = false
                dueDate = selectedDate.format(dateFormatter)
            }
        }
    )

    LaunchedEffect(Unit) {
        val currentUserId = authViewModel.uiState.value.user?.uid
        if (currentUserId != null) {
            userFriendsViewModel.getFriends(currentUserId)
        }
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
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(backgroundColor)
                        .padding(paddingValues)
                        .padding(16.dp)
                ) {
                    Text(
                        "Project Title",
                        style = MaterialTheme.typography.bodyLarge.copy(fontSize = 18.sp),
                        color = Color.White
                    )
                    TextField(
                        value = title,
                        onValueChange = { title = it },
                        placeholder = { Text("Enter project title") },
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
                        "Project Description",
                        style = MaterialTheme.typography.bodyLarge.copy(fontSize = 18.sp),
                        color = Color.White
                    )
                    TextField(
                        value = description,
                        onValueChange = { description = it },
                        placeholder = { Text("Enter project description") },
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
                    if (showError && description.isBlank()) {
                        Text(
                            text = "Description is required",
                            color = Color.Red,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(start = 8.dp, top = 4.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

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
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Due: ${dueDate ?: "Select Due Date"}",
                            color = Color.White,
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
                    if (isPastDate) {
                        Text(
                            text = "Due date cannot be in the past",
                            color = Color.Red,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(start = 8.dp, top = 4.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Invite Team Members",
                        style = MaterialTheme.typography.bodyLarge.copy(fontSize = 18.sp),
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    if (selectedFriends.isNotEmpty()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            selectedFriends.take(3).forEach { friendId ->
                                friends.find { it.friendId == friendId }?.let { friend ->
                                    Box(
                                        modifier = Modifier
                                            .size(40.dp)
                                            .background(mainAppColor, CircleShape)
                                            .clip(CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = friend.displayName
                                                .split(" ")
                                                .mapNotNull { it.firstOrNull()?.toString() }
                                                .take(2)
                                                .joinToString("")
                                                .uppercase(),
                                            color = Color.Black,
                                            style = MaterialTheme.typography.bodyLarge
                                        )
                                    }
                                }
                            }
                            if (selectedFriends.size > 3) {
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .background(mainAppColor, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "+${selectedFriends.size - 3}",
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
                            showFriendSelection = true
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF2C2F38),
                            contentColor = Color.White
                        )
                    ) {
                        Text("Select Team Members")
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Spacer(modifier = Modifier.weight(1f))

                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedButton(
                            onClick = { navController.popBackStack() },
                            modifier = Modifier.padding(8.dp)
                        ) {
                            Text(
                                text = "Cancel",
                                color = Color.White,
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        Button(
                            onClick = {
                                if (title.isBlank() || description.isBlank() || dueDate == null || isPastDate) {
                                    showError = true
                                } else {
                                    val currentUserId = authViewModel.uiState.value.user?.uid ?: ""
                                    val projectId = UUID.randomUUID().toString()

                                    val newProject = Project(
                                        id = projectId,
                                        title = title,
                                        description = description,
                                        dueDate = dueDate!!,
                                        isCompleted = false,
                                        createdBy = currentUserId,
                                        teamMembers = listOf(currentUserId)
                                    )
                                    projectViewModel.createProject(newProject)

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

                                    navController.popBackStack()
                                }
                            },
                            modifier = Modifier.padding(8.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = mainAppColor,
                                contentColor = Color.Black
                            )
                        ) {
                            Text("Save Project", color = Color.Black)
                        }
                    }
                }
            }
        }
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
                })
        }

    }