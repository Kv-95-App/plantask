package kv.apps.taskmanager.presentation.screens.taskScreens

import android.app.DatePickerDialog
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import kv.apps.taskmanager.domain.model.Task
import kv.apps.taskmanager.presentation.shared.uiComposables.TopBar
import kv.apps.taskmanager.presentation.viewmodel.auth.AuthViewModel
import kv.apps.taskmanager.presentation.viewmodel.project.ProjectViewModel
import kv.apps.taskmanager.presentation.viewmodel.task.TaskViewModel
import kv.apps.taskmanager.theme.backgroundColor
import kv.apps.taskmanager.theme.mainAppColor
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Calendar

@Composable
fun AddTaskScreen(
    navController: NavController,
    projectId: String,
    authViewModel: AuthViewModel ,
    taskViewModel: TaskViewModel ,
    projectViewModel: ProjectViewModel
) {
    var title by remember { mutableStateOf("") }
    var taskDetails by remember { mutableStateOf("") }
    var dueDate by remember { mutableStateOf<LocalDate?>(null) }
    var showDatePicker by remember { mutableStateOf(false) }
    var showError by remember { mutableStateOf(false) }
    var selectedUsers by remember { mutableStateOf<Set<String>>(emptySet()) }
    val focusManager = LocalFocusManager.current

    val teamMembersState by projectViewModel.teamMembersWithDetails.collectAsState()
    val loading by projectViewModel.teamMembersLoading.collectAsState()
    val error by projectViewModel.teamMembersError.collectAsState()

    val context = LocalContext.current
    val dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")

    LaunchedEffect(projectId) {
        projectViewModel.fetchTeamMembersForProject(projectId)
    }

    if (showDatePicker) {
        val calendar = Calendar.getInstance()
        val datePickerDialog = DatePickerDialog(
            context,
            { _, year, month, dayOfMonth ->
                dueDate = LocalDate.of(year, month + 1, dayOfMonth)
                showDatePicker = false
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).apply {
            setOnCancelListener { showDatePicker = false }
        }

        LaunchedEffect(showDatePicker) {
            datePickerDialog.show()
        }
    }

    Scaffold(
        topBar = {
            TopBar(
                navController = navController,
                showBackArrow = true,
                onBackPressed = { navController.popBackStack() },
                onProfileClicked = { navController.navigate("profile") },
                onLogoutClicked = {
                    authViewModel.logout()
                    navController.navigate("login") {
                        popUpTo("login") { inclusive = true }
                    }
                }
            )
        },
        containerColor = backgroundColor
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(backgroundColor)
                .padding(paddingValues)
                .padding(16.dp)
                .clickable { focusManager.clearFocus() }
        ) {
            if (loading) {
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
                        .heightIn(min = 100.dp),
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
                        .clickable { showDatePicker = true }
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
            }

            items(teamMembersState) { member ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            selectedUsers = if (selectedUsers.contains(member.userId)) {
                                selectedUsers - member.userId
                            } else {
                                selectedUsers + member.userId
                            }
                        }
                        .padding(8.dp)
                ) {
                    Checkbox(
                        checked = selectedUsers.contains(member.userId),
                        onCheckedChange = { isChecked ->
                            selectedUsers = if (isChecked) {
                                selectedUsers + member.userId
                            } else {
                                selectedUsers - member.userId
                            }
                        },
                        colors = CheckboxDefaults.colors(
                            checkedColor = mainAppColor,
                            uncheckedColor = Color.Gray
                        )
                    )
                    Column {
                        Text(
                            text = "${member.firstName} ${member.lastName}",
                            color = Color.White,
                            modifier = Modifier.padding(start = 8.dp)
                        )
                        Text(
                            text = member.email.toString(),
                            color = Color.Gray,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(start = 8.dp)
                        )
                    }
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
                    enabled = !loading
                ) {
                    Text("Save Task", color = Color.Black)
                }
            }
        }
    }
}