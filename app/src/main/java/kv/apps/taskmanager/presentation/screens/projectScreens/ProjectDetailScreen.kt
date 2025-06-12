package kv.apps.taskmanager.presentation.screens.projectScreens

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.material3.rememberModalBottomSheetState
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
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import kotlinx.coroutines.launch
import kv.apps.taskmanager.R
import kv.apps.taskmanager.domain.model.ProjectInvitation
import kv.apps.taskmanager.presentation.navigation.Screen
import kv.apps.taskmanager.presentation.shared.uiComposables.AppDrawer
import kv.apps.taskmanager.presentation.shared.uiComposables.BottomNavigationBar
import kv.apps.taskmanager.presentation.shared.uiComposables.TopBar
import kv.apps.taskmanager.presentation.viewmodel.auth.AuthViewModel
import kv.apps.taskmanager.presentation.viewmodel.project.ProjectViewModel
import kv.apps.taskmanager.presentation.viewmodel.task.TaskViewModel
import kv.apps.taskmanager.presentation.viewmodel.userFriends.UserFriendsViewModel
import kv.apps.taskmanager.theme.backgroundColor
import kv.apps.taskmanager.theme.mainAppColor
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Calendar

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
    val authState = authViewModel.uiState.collectAsState().value

    var showFriendSelection by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    var selectedFriends by remember { mutableStateOf<Set<String>>(emptySet()) }
    val focusManager = LocalFocusManager.current
    val focusRequester = remember { FocusRequester() }
    val sheetState = rememberModalBottomSheetState()

    val friendsState by userFriendsViewModel.uiState.collectAsState()
    val friends = remember(friendsState.friends) {
        friendsState.friends?.getOrNull() ?: emptyList()
    }

    val filteredFriends = remember(friends, project, searchQuery) {
        if (project == null) {
            emptyList()
        } else {
            friends.filter { friend ->
                (friend.friendName.contains(searchQuery, ignoreCase = true) ||
                        friend.friendEmail.contains(searchQuery, ignoreCase = true))
            }.filter { friend ->
                !project!!.teamMembers.contains(friend.friendId)
            }
        }
    }

    val currentUserId = authState.user?.uid
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

    val customFont = FontFamily(
        Font(R.font.pilat)
    )

    LaunchedEffect(projectId, authState.user?.uid) {
        val userId = authState.user?.uid
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
        ModalBottomSheet(
            onDismissRequest = { showFriendSelection = false },
            sheetState = sheetState,
            containerColor = backgroundColor,
            contentColor = Color.White
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    text = "Invite Team Members",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                if (project == null) {
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = mainAppColor)
                    }
                } else {
                    if (filteredFriends.isEmpty()) {
                        if (searchQuery.isNotEmpty()) {
                            Text(
                                text = "No friends found matching your search",
                                color = Color.Gray,
                                modifier = Modifier.fillMaxWidth(),
                                textAlign = TextAlign.Center
                            )
                        } else {
                            Text(
                                text = "All your friends are already in this project",
                                color = Color.Gray,
                                modifier = Modifier.fillMaxWidth(),
                                textAlign = TextAlign.Center
                            )
                        }
                    } else {
                        TextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            placeholder = { Text("Search friends") },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Search,
                                    contentDescription = "Search",
                                    tint = Color.White
                                )
                            },
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = Color(0xFF2C2F38),
                                unfocusedContainerColor = Color(0xFF2C2F38),
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                cursorColor = Color.White
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .focusRequester(focusRequester),
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                            keyboardActions = KeyboardActions(onSearch = { focusManager.clearFocus() })
                        )

                        LaunchedEffect(showFriendSelection) {
                            if (showFriendSelection) {
                                focusRequester.requestFocus()
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = "Selected: ${selectedFriends.size}",
                            color = mainAppColor,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )

                        Divider(color = Color(0xFF2C2F38), thickness = 1.dp)

                        LazyColumn(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max = 400.dp)
                        ) {
                            items(filteredFriends) { friend ->
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp)
                                        .clickable {
                                            selectedFriends = if (selectedFriends.contains(friend.friendId)) {
                                                selectedFriends - friend.friendId
                                            } else {
                                                selectedFriends + friend.friendId
                                            }
                                        },
                                    colors = CardDefaults.cardColors(
                                        containerColor = if (selectedFriends.contains(friend.friendId)) {
                                            mainAppColor.copy(alpha = 0.2f)
                                        } else {
                                            Color(0xFF2C2F38)
                                        }
                                    )
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.padding(12.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(48.dp)
                                                .background(mainAppColor, CircleShape)
                                                .clip(CircleShape),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = friend.friendName.take(2).uppercase(),
                                                color = Color.Black,
                                                style = MaterialTheme.typography.bodyLarge
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Column(
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Text(
                                                text = friend.friendName,
                                                color = Color.White,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                            friend.friendEmail?.let { email ->
                                                Text(
                                                    text = email,
                                                    color = Color.Gray,
                                                    fontSize = 12.sp,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                            }
                                        }
                                        Checkbox(
                                            checked = selectedFriends.contains(friend.friendId),
                                            onCheckedChange = { isChecked ->
                                                selectedFriends = if (isChecked) {
                                                    selectedFriends + friend.friendId
                                                } else {
                                                    selectedFriends - friend.friendId
                                                }
                                            },
                                            colors = CheckboxDefaults.colors(
                                                checkedColor = mainAppColor,
                                                uncheckedColor = Color.Gray
                                            )
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Button(
                            onClick = {
                                selectedFriends.forEach { friendId ->
                                    val invitation = ProjectInvitation(
                                        invitationId = "inv_${friendId}_${System.currentTimeMillis()}",
                                        fromUserId = currentUserId ?: "",
                                        toUserId = friendId,
                                        projectId = projectId,
                                        status = "Pending"
                                    )
                                    projectViewModel.sendProjectInvitation(invitation)
                                }
                                showFriendSelection = false
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = mainAppColor,
                                contentColor = Color.Black
                            ),
                            enabled = selectedFriends.isNotEmpty()
                        ) {
                            Text("Send Invitations")
                        }
                    }
                }
            }
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
        isLoggingOut = authState.isLoggingOut
    ) {
        Scaffold(
            topBar = {
                TopBar(
                    navController = navController,
                    onMenuClicked = { coroutineScope.launch { drawerState.open() } },
                    showBackArrow = false,
                    isLoggingOut = authState.isLoggingOut,
                    modifier = Modifier.padding(top = 24.dp) // Add padding for status bar
                )
            },
            bottomBar = {
                BottomNavigationBar(
                    navController = navController,
                    modifier = Modifier.padding(bottom = 8.dp) // Add padding for navigation bar
                )
            },
            containerColor = backgroundColor,
            contentWindowInsets = WindowInsets(0.dp) // Disable all window insets
        ) { paddingValues ->
            Surface(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                color = backgroundColor
            ) {
                when {
                    error != null -> Box(
                        modifier = Modifier
                            .fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Error: $error",
                            color = Color.Red
                        )
                    }

                    showProjectNotFound -> Box(
                        modifier = Modifier
                            .fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Project not found",
                            color = Color.Red
                        )
                    }

                    project != null -> Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(16.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (isEditing) {
                                OutlinedTextField(
                                    value = editedTitle,
                                    onValueChange = { editedTitle = it },
                                    modifier = Modifier
                                        .weight(1f),
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
                                    text = editedTitle
                                        .uppercase(),
                                    style = TextStyle(
                                        fontSize = 18.sp,
                                        fontFamily = customFont,
                                        fontWeight = FontWeight.Light,
                                        letterSpacing = 1.7.sp,
                                        color = Color.White
                                    ),
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }

                            if (canEdit.value) {
                                if (isEditing) {
                                    Row {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = "Save",
                                            tint = Color.Green,
                                            modifier = Modifier
                                                .size(32.dp)
                                                .clickable {
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
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Icon(
                                            imageVector = Icons.Default.Close,
                                            contentDescription = "Cancel",
                                            tint = Color.Red,
                                            modifier = Modifier
                                                .size(32.dp)
                                                .clickable {
                                                    isEditing = false
                                                    editedTitle = project!!.title
                                                    editedDescription = project!!.description
                                                    editedDueDate = project!!.dueDate
                                                }
                                        )
                                    }
                                } else {
                                    Icon(
                                        imageVector = Icons.Default.Edit,
                                        contentDescription = "Edit",
                                        tint = Color.White,
                                        modifier = Modifier
                                            .size(24.dp)
                                            .clickable {
                                                isEditing = true
                                            }
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Row(
                            modifier = Modifier
                                .fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier
                                    .weight(1f)
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
                                        modifier = Modifier
                                            .size(24.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))

                                if (isEditing && canEdit.value) {
                                    val context = LocalContext.current
                                    val dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
                                    var showDatePicker by remember { mutableStateOf(false) }

                                    if (showDatePicker) {
                                        val calendar = Calendar.getInstance()
                                        val datePickerDialog = DatePickerDialog(
                                            context,
                                            { _, year, month, dayOfMonth ->
                                                val selectedDate =
                                                    LocalDate.of(year, month + 1, dayOfMonth)
                                                editedDueDate = selectedDate.format(dateFormatter)
                                                showDatePicker = false
                                            },
                                            calendar.get(Calendar.YEAR),
                                            calendar.get(Calendar.MONTH),
                                            calendar.get(Calendar.DAY_OF_MONTH)
                                        )
                                        datePickerDialog.setOnCancelListener {
                                            showDatePicker = false
                                        }
                                        LaunchedEffect(showDatePicker) {
                                            datePickerDialog.show()
                                        }
                                    }

                                    Box(
                                        modifier = Modifier
                                            .background(
                                                color = mainAppColor.copy(alpha = 0.3f),
                                                shape = RoundedCornerShape(8.dp)
                                            )
                                            .padding(horizontal = 12.dp, vertical = 8.dp)
                                            .clickable { showDatePicker = true },
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
                                                    modifier = Modifier
                                                        .size(16.dp)
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
                                        navController.navigate(
                                            Screen.ProjectMembers.createRoute(projectId)
                                        )
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
                                        modifier = Modifier
                                            .size(24.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                val totalMembers = project!!.teamMembers.size
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
                            text = "Tasks",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Column {
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
                                        color = Color.White,
                                        modifier = Modifier.clickable {
                                            navController.navigate(
                                                Screen.TaskDetail.createRoute(
                                                    task.id,
                                                    projectId
                                                )
                                            )
                                        }
                                    )
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        if (canEdit.value) {
                            Column(
                                verticalArrangement = Arrangement.spacedBy(8.dp)
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
                                    enabled = project != null
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