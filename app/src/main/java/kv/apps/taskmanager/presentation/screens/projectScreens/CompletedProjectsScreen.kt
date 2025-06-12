package kv.apps.taskmanager.presentation.screens.projectScreens

import androidx.compose.foundation.background
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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import kotlinx.coroutines.launch
import kv.apps.taskmanager.R
import kv.apps.taskmanager.presentation.navigation.Screen
import kv.apps.taskmanager.presentation.shared.uiComposables.AppDrawer
import kv.apps.taskmanager.presentation.shared.uiComposables.BottomNavigationBar
import kv.apps.taskmanager.presentation.shared.uiComposables.TopBar
import kv.apps.taskmanager.presentation.viewmodel.auth.AuthViewModel
import kv.apps.taskmanager.presentation.viewmodel.project.ProjectViewModel
import kv.apps.taskmanager.theme.backgroundColor
import kv.apps.taskmanager.theme.mainAppColor
import kv.apps.taskmanager.theme.onGoingCardColor

@Composable
fun CompletedProjectsScreen(
    navController: NavController,
    projectViewModel: ProjectViewModel,
    authViewModel: AuthViewModel
) {
    val projects by projectViewModel.projects.collectAsState()
    val authState by authViewModel.uiState.collectAsState()
    val userId = authState.userId
    val isLoading by projectViewModel.loading.collectAsState()
    val isLoggingOut = authState.isLoggingOut

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val coroutineScope = rememberCoroutineScope()

    val completedProjects = remember(projects, userId) {
        if (userId == null) emptyList() else {
            projects.filter { project ->
                project.isCompleted && (
                        project.createdBy == userId.toString() ||
                                project.teamMembers.any { it == userId.toString() }
                        )
            }
        }
    }

    LaunchedEffect(userId) {
        if (userId != null) {
            projectViewModel.fetchAllProjects()
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
                    showBackArrow = true,
                    isLoggingOut = isLoggingOut,
                    onBackPressed = { navController.popBackStack() },
                    modifier = Modifier.padding(top = 24.dp)
                )
            },
            bottomBar = { BottomNavigationBar(navController) },
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .background(backgroundColor)
            ) {
                when {
                    isLoading -> {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(backgroundColor),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(color = mainAppColor)
                        }
                    }
                    userId == null -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("Please log in to view projects", color = Color.White)
                        }
                    }
                    completedProjects.isEmpty() -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "No completed projects",
                                color = Color.White,
                                modifier = Modifier.padding(16.dp)
                            )
                        }
                    }
                    else -> {
                        Column(
                            modifier = Modifier.fillMaxSize()
                        ) {
                            Text(
                                text = "Completed Projects (${completedProjects.size})",
                                color = Color.White,
                                style = MaterialTheme.typography.titleLarge,
                                modifier = Modifier.padding(16.dp)
                            )

                            LazyColumn(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(1f)
                                    .padding(horizontal = 16.dp)
                            ) {
                                items(completedProjects, key = { it.id }) { project ->
                                    Card(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 8.dp),
                                        // Removed clickable modifier completely
                                        shape = RoundedCornerShape(16.dp),
                                        colors = CardDefaults.cardColors(containerColor = onGoingCardColor),
                                        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(16.dp)
                                        ) {
                                            Column(
                                                modifier = Modifier.fillMaxWidth()
                                            ) {
                                                Text(
                                                    text = project.title,
                                                    style = TextStyle(
                                                        letterSpacing = 2.sp,
                                                        fontSize = 14.sp,
                                                        fontFamily = FontFamily(Font(R.font.pilat)),
                                                        color = Color.White
                                                    )
                                                )
                                                Spacer(modifier = Modifier.height(16.dp))
                                                Row(
                                                    modifier = Modifier
                                                        .fillMaxWidth(),
                                                    verticalAlignment = Alignment.CenterVertically,
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.DateRange,
                                                        contentDescription = "Due Date",
                                                        tint = mainAppColor,
                                                        modifier = Modifier.size(24.dp)
                                                    )
                                                    Spacer(modifier = Modifier.width(8.dp))
                                                    Text(
                                                        text = "Due: ${project.dueDate}",
                                                        style = MaterialTheme.typography.bodySmall,
                                                        color = Color(0xFFC0C0C0)
                                                    )
                                                }
                                                Spacer(modifier = Modifier.height(16.dp))

                                                Row(
                                                    modifier = Modifier
                                                        .fillMaxWidth(),
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Icon(
                                                        painter = painterResource(id = R.drawable.team2),
                                                        contentDescription = "Team Members",
                                                        tint = mainAppColor,
                                                        modifier = Modifier.size(24.dp)
                                                    )
                                                    val totalMembers = project.teamMembers.size
                                                    val memberText =
                                                        if (totalMembers == 1) "1 Member" else "$totalMembers Members"
                                                    Spacer(modifier = Modifier.width(8.dp))

                                                    Text(
                                                        text = "$memberText ",
                                                        style = MaterialTheme.typography.bodySmall,
                                                        color = Color(0xFFC0C0C0),
                                                        maxLines = 1,
                                                        overflow = TextOverflow.Ellipsis
                                                    )
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
        }
    }
}