package kv.apps.taskmanager.presentation.screens.projectScreens

import android.R.attr.onClick
import android.app.DatePickerDialog
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import kv.apps.taskmanager.domain.model.Project
import kv.apps.taskmanager.domain.model.ProjectInvitation
import kv.apps.taskmanager.presentation.shared.uiComposables.TopBar
import kv.apps.taskmanager.presentation.viewmodel.AuthViewModel
import kv.apps.taskmanager.presentation.viewmodel.ProjectViewModel
import kv.apps.taskmanager.presentation.viewmodel.UserFriendsViewModel
import kv.apps.taskmanager.theme.backgroundColor
import kv.apps.taskmanager.theme.mainAppColor
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Calendar
import java.util.UUID

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
    var showDatePicker by remember { mutableStateOf(false) }
    var showError by remember { mutableStateOf(false) }
    var isPastDate by remember { mutableStateOf(false) }
    var selectedFriends by remember { mutableStateOf<Set<String>>(emptySet()) }
    val focusManager = LocalFocusManager.current

    val friendsState by userFriendsViewModel.friendsState.collectAsState()
    val friends = remember(friendsState) {
        friendsState?.getOrNull() ?: emptyList()
    }

    LaunchedEffect(Unit) {
        val currentUserId = authViewModel.user.value?.uid
        if (currentUserId != null) {
            userFriendsViewModel.getFriends(currentUserId)
        }
    }

    val context = LocalContext.current
    val dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")

    if (showDatePicker) {
        val calendar = Calendar.getInstance()
        val datePickerDialog = DatePickerDialog(
            context,
            { _, year, month, dayOfMonth ->
                val selectedDate = LocalDate.of(year, month + 1, dayOfMonth)
                val currentDate = LocalDate.now()

                if (selectedDate.isBefore(currentDate)) {
                    isPastDate = true
                } else {
                    isPastDate = false
                    dueDate = selectedDate.format(dateFormatter)
                }
                showDatePicker = false
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
        datePickerDialog.setOnCancelListener { showDatePicker = false }
        LaunchedEffect(showDatePicker) {
            datePickerDialog.show()
        }
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
                modifier = Modifier.fillMaxWidth()
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
                    .clickable { showDatePicker = true }
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
            Column {
                friends.forEach { friend ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                selectedFriends = if (selectedFriends.contains(friend.friendId)) {
                                    selectedFriends - friend.friendId
                                } else {
                                    selectedFriends + friend.friendId
                                }
                            }
                            .padding(8.dp)
                    ) {
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
                                uncheckedColor = Color.White
                            )
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = friend.friendName,
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White
                        )
                    }
                }
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
                            val currentUserId = authViewModel.user.value?.uid ?: ""
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