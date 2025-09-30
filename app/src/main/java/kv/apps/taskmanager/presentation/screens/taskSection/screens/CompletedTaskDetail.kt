package kv.apps.taskmanager.presentation.screens.taskSection.screens

import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import kotlinx.coroutines.launch
import kv.apps.taskmanager.R
import kv.apps.taskmanager.presentation.navigation.Screen
import kv.apps.taskmanager.presentation.screens.taskSection.taskComposables.CommentItem
import kv.apps.taskmanager.presentation.screens.taskSection.taskComposables.CommentsHeader
import kv.apps.taskmanager.presentation.screens.utils.shared.avatar.MemberAvatarsRow
import kv.apps.taskmanager.presentation.screens.utils.shared.menuBars.BottomNavigationBar
import kv.apps.taskmanager.presentation.screens.utils.shared.menuBars.TopBar
import kv.apps.taskmanager.presentation.screens.utils.shared.uiComposables.AppDrawer
import kv.apps.taskmanager.presentation.viewmodel.auth.AuthViewModel
import kv.apps.taskmanager.presentation.viewmodel.task.TaskViewModel
import kv.apps.taskmanager.theme.backgroundColor
import kv.apps.taskmanager.theme.mainAppColor

@Composable
fun CompletedTaskDetailScreen(
    navController: NavController,
    taskId: String,
    projectId: String,
    taskViewModel: TaskViewModel,
    authViewModel: AuthViewModel
) {
    val uiState by taskViewModel.uiState.collectAsState()
    val task = uiState.selectedTask
    val loading = uiState.isLoading
    val authState by authViewModel.uiState.collectAsState()
    val isLoggingOut = authState.isLoggingOut

    val commentsState by taskViewModel.commentsState.collectAsState()
    val currentTaskComments = commentsState[taskId] ?: emptyList()
    val currentUserId = authState.user?.uid

    var areCommentsExpanded by remember { mutableStateOf(true) }

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val coroutineScope = rememberCoroutineScope()

    val customFont = FontFamily(
        Font(R.font.pilat)
    )

    var showLoading by remember { mutableStateOf(true) }

    LaunchedEffect(taskId, projectId) {
        taskViewModel.fetchTaskByIdFromProject(projectId, taskId)
        taskViewModel.fetchTaskMembers(projectId, taskId)
        taskViewModel.fetchAssignedUsersInitials(projectId, taskId)
        taskViewModel.loadTaskComments(projectId, taskId)
        taskViewModel.observeTaskComments(projectId, taskId)
        kotlinx.coroutines.delay(1000)
        showLoading = false
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
                    showLoading || loading -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(color = mainAppColor)
                        }
                    }
                    task != null -> {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp)
                        ) {
                            LazyColumn(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                item {
                                    Text(
                                        text = task.title.uppercase(),
                                        style = TextStyle(
                                            fontSize = 18.sp,
                                            fontFamily = customFont,
                                            fontWeight = FontWeight.Light,
                                            letterSpacing = 1.7.sp,
                                            color = Color.White
                                        ),
                                        fontWeight = FontWeight.Bold
                                    )

                                    Spacer(modifier = Modifier.height(16.dp))

                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(16.dp))
                                            .background(
                                                if (task.isCompleted) Color.Green.copy(alpha = 0.2f)
                                                else mainAppColor.copy(alpha = 0.2f)
                                            )
                                            .padding(horizontal = 16.dp, vertical = 8.dp)
                                    ) {
                                        Text(
                                            text = if (task.isCompleted) "COMPLETED" else "IN PROGRESS",
                                            color = if (task.isCompleted) Color.Green else mainAppColor,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(16.dp))

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.DateRange,
                                            contentDescription = "Due Date",
                                            tint = mainAppColor,
                                            modifier = Modifier.size(24.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "Due: ${task.dueDate}",
                                            style = MaterialTheme.typography.bodyLarge,
                                            color = Color.White
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(16.dp))

                                    Column(modifier = Modifier.fillMaxWidth()) {
                                        Text(
                                            text = "Assigned To",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = Color.White
                                        )
                                        Spacer(modifier = Modifier.height(8.dp))

                                        if (task.assignedTo.isEmpty()) {
                                            Text(
                                                text = "No one assigned",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = Color.Gray
                                            )
                                        } else {
                                            MemberAvatarsRow(
                                                userIds = task.assignedTo,
                                                initialsMap = uiState.taskAssignedUsersInitials,
                                                isLoading = false
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(16.dp))

                                    Column(modifier = Modifier.fillMaxWidth()) {
                                        Text(
                                            text = "Task Details",
                                            style = MaterialTheme.typography.titleLarge,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White
                                        )
                                        Spacer(modifier = Modifier.height(8.dp))

                                        if (task.taskDetails.isBlank()) {
                                            Text(
                                                text = "No details provided",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = Color.Gray
                                            )
                                        } else {
                                            Text(
                                                text = task.taskDetails,
                                                style = MaterialTheme.typography.bodyLarge,
                                                color = Color.White,
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .background(
                                                        Color(0xFF3A3E4B),
                                                        RoundedCornerShape(12.dp)
                                                    )
                                                    .padding(16.dp)
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(24.dp))
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
                                                onEditComment = {  },
                                                onDeleteComment = {  },
                                                modifier = Modifier.padding(vertical = 8.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                    else -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    "Task not available",
                                    style = MaterialTheme.typography.titleLarge,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    "This task may have been deleted",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color.Gray
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    "Go back and try again",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = mainAppColor
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}