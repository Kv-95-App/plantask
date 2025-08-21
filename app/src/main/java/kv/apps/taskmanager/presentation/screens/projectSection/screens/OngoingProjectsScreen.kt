package kv.apps.taskmanager.presentation.screens.projectSection.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
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
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import kotlinx.coroutines.launch
import kv.apps.taskmanager.presentation.navigation.Screen
import kv.apps.taskmanager.presentation.screens.projectSection.projectComposables.ProjectCard
import kv.apps.taskmanager.presentation.screens.utils.shared.uiComposables.AppDrawer
import kv.apps.taskmanager.presentation.screens.utils.shared.menuBars.BottomNavigationBar
import kv.apps.taskmanager.presentation.screens.utils.shared.uiComposables.SectionHeader
import kv.apps.taskmanager.presentation.screens.utils.shared.menuBars.TopBar
import kv.apps.taskmanager.presentation.viewmodel.auth.AuthViewModel
import kv.apps.taskmanager.presentation.viewmodel.project.ProjectViewModel
import kv.apps.taskmanager.theme.backgroundColor
import kv.apps.taskmanager.theme.mainAppColor

@Composable
fun OngoingProjectsScreen (
    navController: NavController,
    projectViewModel: ProjectViewModel,
    authViewModel: AuthViewModel,
    onAddProjectClicked: () -> Unit
) {
    val projects by projectViewModel.projects.collectAsState()
    val userId = authViewModel.uiState.collectAsState().value.userId
    val isLoading by projectViewModel.loading.collectAsState()
    val authUiState by authViewModel.uiState.collectAsState()
    val isLoggingOut = authUiState.isLoggingOut

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val coroutineScope = rememberCoroutineScope()

    val filteredProjects = remember(projects, userId) {
        projects.filter { project ->
            userId.let { uid ->
                project.createdBy == uid.toString() ||
                        project.teamMembers.any { it == uid.toString() }
            }
        }
    }


    val ongoingProjects = remember(filteredProjects) {
        filteredProjects.filter { !it.isCompleted }
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

        },
        drawerState = drawerState,
        isLoggingOut = isLoggingOut,
        navController = navController
    ) {
        Scaffold(
            topBar = {
                TopBar(
                    navController = navController,
                    onMenuClicked = { coroutineScope.launch {
                        drawerState.open()  }
                    },
                    showBackArrow = false
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
                            .fillMaxSize()
                            .background(backgroundColor),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = mainAppColor)
                    }
                } else {
                    SectionHeader(
                        title = "Ongoing Projects", onSeeAllClick = null,
                        isExpanded = false,
                        onToggleClick = {},
                        modifier = Modifier
                            .padding(16.dp)
                    )
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
                                    showActions = true
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}