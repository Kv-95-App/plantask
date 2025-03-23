package kv.apps.taskmanager.presentation.screens.projectScreens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import kv.apps.taskmanager.presentation.shared.taskComposables.CompletedProjectCard
import kv.apps.taskmanager.presentation.shared.taskComposables.ProjectCard
import kv.apps.taskmanager.presentation.shared.uiComposables.BottomNavigationBar
import kv.apps.taskmanager.presentation.shared.uiComposables.SectionHeader
import kv.apps.taskmanager.presentation.shared.uiComposables.TopBar
import kv.apps.taskmanager.presentation.viewmodel.AuthViewModel
import kv.apps.taskmanager.presentation.viewmodel.ProjectViewModel
import kv.apps.taskmanager.presentation.viewmodel.TaskViewModel
import kv.apps.taskmanager.theme.backgroundColor
import kv.apps.taskmanager.theme.mainAppColor

@Composable
fun ProjectListScreen(
    navController: NavController,
    projectViewModel: ProjectViewModel = hiltViewModel(),
    authViewModel: AuthViewModel = hiltViewModel(),
    taskViewModel: TaskViewModel = hiltViewModel(),
    onAddProjectClicked: () -> Unit,
    onProjectSelected: (String) -> Unit
) {
    val projects by projectViewModel.projects.collectAsState()
    val userState by authViewModel.user.collectAsState()
    val currentUserId = userState?.uid ?: ""

    val filteredProjects = remember(projects, currentUserId) {
        projects.filter { project ->
            project.createdBy == currentUserId || project.teamMembers.contains(currentUserId)
        }
    }

    // Split filtered projects into ongoing and completed
    val ongoingProjects = remember(filteredProjects) {
        filteredProjects.filter { !it.isCompleted }
    }
    val completedProjects = remember(filteredProjects) {
        filteredProjects.filter { it.isCompleted }
    }

    LaunchedEffect(Unit) {
        projectViewModel.fetchAllProjects()
    }

    val fabColor = remember { mainAppColor }

    Scaffold(
        topBar = {
            TopBar(
                navController = navController,
                authViewModel = authViewModel,
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
            FloatingActionButton(
                onClick = onAddProjectClicked,
                containerColor = fabColor
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Project")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(backgroundColor)
        ) {
            // Completed Projects Section (LazyRow on top)
            SectionHeader(title = "Completed Projects", onSeeAllClick = {})
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

            Spacer(modifier = Modifier.height(8.dp))

            // Ongoing Projects Section (LazyColumn below)
            SectionHeader(title = "Ongoing Projects", onSeeAllClick = {})
            LazyColumn(modifier = Modifier.weight(1f)) {
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
                        onProjectClicked = {
                            // Preload project details and tasks before navigating
                            projectViewModel.getProjectById(project.id)
                            taskViewModel.loadTasksForProject(project.id)
                            navController.navigate("projectDetail/${project.id}")
                        },
                        navController = navController
                    )
                }
            }
        }
    }
}
