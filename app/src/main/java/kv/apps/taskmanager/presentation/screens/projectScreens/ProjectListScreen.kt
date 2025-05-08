package kv.apps.taskmanager.presentation.screens.projectScreens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
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
    projectViewModel: ProjectViewModel,
    authViewModel: AuthViewModel,
    taskViewModel: TaskViewModel,
    onAddProjectClicked: () -> Unit
) {
    val projects by projectViewModel.projects.collectAsState()
    val userId by authViewModel.userId.collectAsState()
    val isLoading by projectViewModel.loading.collectAsState()

    val filteredProjects = remember(projects, userId) {
        projects.filter { project ->
            userId?.let { uid ->
                project.createdBy == uid.toString() ||
                        project.teamMembers.any { it == uid }
            } == true
        }
    }


    val ongoingProjects = remember(filteredProjects) {
        filteredProjects.filter { !it.isCompleted }
    }
    val completedProjects = remember(filteredProjects) {
        filteredProjects.filter { it.isCompleted }
    }

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
            FloatingActionButton(
                onClick = onAddProjectClicked,
                containerColor = mainAppColor
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
                SectionHeader(title = "Completed Projects", onSeeAllClick = {
                    navController.navigate("completed_projects")
                })
                if (completedProjects.isEmpty()) {
                    Text(
                        text = "No completed projects",
                        modifier = Modifier.padding(16.dp)
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

                Spacer(modifier = Modifier.height(8.dp))

                SectionHeader(title = "Ongoing Projects", onSeeAllClick = {
                    navController.navigate("ongoing_projects")
                })
                if (ongoingProjects.isEmpty()) {
                    Text(
                        text = "No ongoing projects",
                        modifier = Modifier
                            .padding(16.dp)
                            .weight(1f)
                    )
                } else {
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
                                navController = navController,
                                onProjectClicked = {}
                            )
                        }
                    }
                }
            }
        }
    }
}