package kv.apps.taskmanager.presentation.screens.projectScreens

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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.material3.rememberModalBottomSheetState
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
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import kotlinx.coroutines.launch
import kv.apps.taskmanager.domain.model.Project
import kv.apps.taskmanager.domain.model.ProjectInvitation
import kv.apps.taskmanager.presentation.navigation.Screen
import kv.apps.taskmanager.presentation.shared.uiComposables.AppDrawer
import kv.apps.taskmanager.presentation.shared.uiComposables.TopBar
import kv.apps.taskmanager.presentation.shared.uiComposables.rememberCustomDatePicker
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
    val isLoggingOut = authViewModel.uiState.collectAsState().value.isLoggingOut
    val sheetState = rememberModalBottomSheetState()
    val focusRequester = remember { FocusRequester() }

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val coroutineScope = rememberCoroutineScope()

    val friendsState by userFriendsViewModel.uiState.collectAsState()
    val friends = remember(friendsState.friends) {
        friendsState.friends?.getOrNull() ?: emptyList()
    }

    val filteredFriends = friends.filter { friend ->
        friend.friendName.contains(searchQuery, ignoreCase = true) ||
                friend.friendEmail.contains(searchQuery, ignoreCase = true) == true
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
            navController.navigate(Screen.Login.route) {
                popUpTo("login") { inclusive = true }
            }
        },
        drawerState = drawerState,
        isLoggingOut = isLoggingOut
    ) {
        Scaffold(
            topBar = {
                TopBar(
                    navController = navController,
                    onMenuClicked = { coroutineScope.launch { drawerState.open() } },
                    showBackArrow = false,
                    isLoggingOut = isLoggingOut,
                    modifier = Modifier.padding(top = 24.dp)
                )
            },
            containerColor = backgroundColor
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(backgroundColor)
                    .padding(paddingValues)
                    .padding(16.dp)
                    .clickable { focusManager.clearFocus() }
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
                    modifier = Modifier.fillMaxWidth(),
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
                        .heightIn(min = 100.dp),
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
                        .clickable { showDatePicker() }
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
                                        text = friend.friendName.take(2).uppercase(),
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
                    onClick = { showFriendSelection = true },
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
                        onClick = {
                            navController.popBackStack()
                        },
                        modifier = Modifier
                            .padding(8.dp)
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
                        modifier = Modifier
                            .padding(8.dp),
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

                // Friends list
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
                                    friend.friendEmail.let { email ->
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
                    onClick = { showFriendSelection = false },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = mainAppColor,
                        contentColor = Color.Black
                    )
                ) {
                    Text("Done")
                }
            }
        }
    }
}