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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Edit
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import kv.apps.taskmanager.R
import kv.apps.taskmanager.presentation.navigation.Screen
import kv.apps.taskmanager.presentation.shared.uiComposables.BottomNavigationBar
import kv.apps.taskmanager.presentation.shared.uiComposables.TopBar
import kv.apps.taskmanager.presentation.viewmodel.ProjectViewModel
import kv.apps.taskmanager.presentation.viewmodel.TaskViewModel
import kv.apps.taskmanager.presentation.viewmodel.auth.AuthViewModel
import kv.apps.taskmanager.presentation.viewmodel.userFriends.UserFriendsViewModel
import kv.apps.taskmanager.theme.backgroundColor
import kv.apps.taskmanager.theme.mainAppColor
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Calendar

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

    val currentUserId = authViewModel.uiState.collectAsState().value.user?.uid

    LaunchedEffect(projectId, currentUserId) {
        val userId = currentUserId
        if (userId == null) return@LaunchedEffect

        projectViewModel.getProjectById(projectId)
        taskViewModel.loadTasksForProject(projectId)
        hasLoaded = true
    }

    LaunchedEffect(project) {
        if (project != null && !isEditing) {
            editedTitle = project!!.title
            editedDescription = project!!.description
            editedDueDate = project!!.dueDate
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
                    modifier = Modifier
                        .fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                }

                error != null -> Box(
                    modifier = Modifier
                        .fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Error: $error",
                        color = Color.Red)
                }

                showProjectNotFound -> Box(
                    modifier = Modifier
                        .fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Project not found",
                        color = Color.Red)
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

                            if (isEditing) {
                                val context = LocalContext.current
                                val dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
                                var showDatePicker by remember { mutableStateOf(false) }

                                if (showDatePicker) {
                                    val calendar = Calendar.getInstance()
                                    val datePickerDialog = DatePickerDialog(
                                        context,
                                        { _, year, month, dayOfMonth ->
                                            val selectedDate = LocalDate.of(year, month + 1, dayOfMonth)
                                            editedDueDate = selectedDate.format(dateFormatter)
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
                    if (isEditing) {
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
                }
            }
        }
    }
}

