package kv.apps.taskmanager.presentation.screens.projectScreens

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import kv.apps.taskmanager.presentation.shared.taskComposables.CompletedProjectCard
import kv.apps.taskmanager.presentation.shared.taskComposables.ProjectCard
import kv.apps.taskmanager.presentation.shared.uiComposables.BottomNavigationBar
import kv.apps.taskmanager.presentation.shared.uiComposables.SectionHeader
import kv.apps.taskmanager.presentation.shared.uiComposables.TopBar
import kv.apps.taskmanager.presentation.viewmodel.auth.AuthViewModel
import kv.apps.taskmanager.presentation.viewmodel.ProjectViewModel
import kv.apps.taskmanager.presentation.viewmodel.TaskViewModel
import kv.apps.taskmanager.theme.backgroundColor
import kv.apps.taskmanager.theme.mainAppColor
import kotlin.math.roundToInt

@Composable
fun ProjectListScreen(
    navController: NavController,
    projectViewModel: ProjectViewModel,
    authViewModel: AuthViewModel,
    taskViewModel: TaskViewModel,
    onAddProjectClicked: () -> Unit
) {
    val projects by projectViewModel.projects.collectAsState()
    val userId = authViewModel.uiState.collectAsState().value.userId
    val isLoading by projectViewModel.loading.collectAsState()

    var completedExpanded by remember { mutableStateOf(true) }
    var ongoingExpanded by remember { mutableStateOf(true) }

    val filteredProjects = remember(projects, userId) {
        projects.filter { project ->
            userId.let { uid ->
                project.createdBy == uid.toString() ||
                        project.teamMembers.any { it == uid.toString() }
            } == true
        }
    }

    val ongoingProjects = remember(filteredProjects) {
        filteredProjects.filter { !it.isCompleted }
    }
    val completedProjects = remember(filteredProjects) {
        filteredProjects.filter { it.isCompleted }
    }

    var fabPosition by remember { mutableStateOf(Offset(0f, 0f)) }
    var isDragging by remember { mutableStateOf(false) }

    LaunchedEffect(userId) {
        if (userId != null) {
            projectViewModel.fetchAllProjects()
        }
    }

    Scaffold(
        topBar = {
            TopBar(
                navController = navController,
                onProfileClicked = { navController.navigate("profile") },
                onLogoutClicked = {
                    authViewModel.logout()
                    navController.navigate("login") {
                        popUpTo("login") { inclusive = true }
                    }
                }
            )
        },
        bottomBar = { BottomNavigationBar(navController) },
        floatingActionButton = {
            Box(
                modifier = Modifier
            ) {
                FloatingActionButton(
                    onClick = onAddProjectClicked,
                    containerColor = mainAppColor,
                    modifier = Modifier
                        .padding(16.dp)
                        .size(36.dp)
                        .offset {
                            IntOffset(
                                x = fabPosition.x.roundToInt(),
                                y = fabPosition.y.roundToInt()
                            )
                        }
                        .pointerInput(Unit) {
                            detectDragGestures { change, dragAmount ->
                                change.consume()
                                if (isDragging) {
                                    fabPosition = Offset(
                                        fabPosition.x + dragAmount.x,
                                        fabPosition.y + dragAmount.y
                                    )
                                } else {
                                    isDragging = true
                                }
                            }
                        },
                    shape = FloatingActionButtonDefaults.extendedFabShape

                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add Project")
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(backgroundColor)
        ) {
            if (isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else {
                SectionHeader(
                    title = "Completed Projects (${completedProjects.size})",
                    isExpanded = completedExpanded,
                    onToggleClick = { completedExpanded = !completedExpanded },
                    onSeeAllClick = { navController.navigate("completed_projects") }
                )

                if (completedExpanded) {
                    if (completedProjects.isEmpty()) {
                        Text(
                            text = "No completed projects",
                            modifier = Modifier
                                .padding(16.dp),
                            color = Color.Gray
                        )
                    } else {
                        LazyRow(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp)
                        ) {
                            items(completedProjects, key = { it.id }) { project ->
                                CompletedProjectCard(
                                    project = project,
                                    onDeleteClicked = { projectViewModel.deleteProject(project.id) }
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                SectionHeader(
                    title = "Ongoing Projects (${ongoingProjects.size})",
                    isExpanded = ongoingExpanded,
                    onToggleClick = { ongoingExpanded = !ongoingExpanded },
                    onSeeAllClick = { navController.navigate("ongoing_projects") }
                )

                if (ongoingExpanded) {
                    if (ongoingProjects.isEmpty()) {
                        Text(
                            text = "No ongoing projects",
                            modifier = Modifier
                                .padding(16.dp)
                                .weight(1f),
                            color = Color.Gray
                        )
                    } else {
                        LazyColumn(
                            modifier = Modifier
                                .weight(1f)) {
                            items(ongoingProjects, key = { it.id }) { project ->
                                ProjectCard(
                                    project = project,
                                    onDeleteClicked = {
                                        projectViewModel.deleteProject(project.id)
                                    },
                                    onMarkComplete = {
                                        val updatedProject = project.copy(isCompleted = true)
                                        projectViewModel.updateProject(project.id, updatedProject)
                                    },
                                    navController = navController
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
