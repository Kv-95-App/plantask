package kv.apps.taskmanager.presentation.screens.projectScreens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import kotlinx.coroutines.launch
import kv.apps.taskmanager.presentation.navigation.Screen
import kv.apps.taskmanager.presentation.shared.taskComposables.CompletedProjectCard
import kv.apps.taskmanager.presentation.shared.taskComposables.ProjectCard
import kv.apps.taskmanager.presentation.shared.uiComposables.AppDrawer
import kv.apps.taskmanager.presentation.shared.uiComposables.BottomNavigationBar
import kv.apps.taskmanager.presentation.shared.uiComposables.SectionHeader
import kv.apps.taskmanager.presentation.shared.uiComposables.TopBar
import kv.apps.taskmanager.presentation.viewmodel.auth.AuthViewModel
import kv.apps.taskmanager.presentation.viewmodel.project.ProjectViewModel
import kv.apps.taskmanager.theme.backgroundColor
import kv.apps.taskmanager.theme.mainAppColor
import kotlin.math.roundToInt

@Composable
fun ProjectListScreen(
    navController: NavController,
    projectViewModel: ProjectViewModel,
    authViewModel: AuthViewModel,
    onAddProjectClicked: () -> Unit
) {
    val projects by projectViewModel.projects.collectAsState()
    val userId = authViewModel.uiState.collectAsState().value.userId
    val isLoading by projectViewModel.loading.collectAsState()
    val focusManager = LocalFocusManager.current
    val interactionSource = remember { MutableInteractionSource() }
    val isLoggingOut = authViewModel.uiState.collectAsState().value.isLoggingOut

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val coroutineScope = rememberCoroutineScope()

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
            modifier = Modifier.fillMaxSize(),
            topBar = {
                TopBar(
                    navController = navController,
                    onMenuClicked = { coroutineScope.launch { drawerState.open() } },
                    showBackArrow = false,
                    isLoggingOut = isLoggingOut,
                    modifier = Modifier
                        .background(MaterialTheme.colorScheme.background)
                        .statusBarsPadding()
                )
            },
            bottomBar = {
                BottomNavigationBar(
                    navController = navController,
                    modifier = Modifier
                        .background(MaterialTheme.colorScheme.background)
                        .navigationBarsPadding()
                )
            },
            floatingActionButton = {
                Box(modifier = Modifier) {
                    FloatingActionButton(
                        onClick = {
                            onAddProjectClicked()
                            focusManager.clearFocus()
                        },
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
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(backgroundColor)
                    .clickable(
                        interactionSource = interactionSource,
                        indication = null,
                        onClick = { focusManager.clearFocus() }
                    )
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
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValues)
                    ) {
                        item {
                            SectionHeader(
                                title = "Completed Projects (${completedProjects.size})",
                                isExpanded = completedExpanded,
                                onToggleClick = {
                                    completedExpanded = !completedExpanded
                                    focusManager.clearFocus()
                                },
                                onSeeAllClick = {
                                    navController.navigate(Screen.CompletedProjects.route)
                                    focusManager.clearFocus()
                                }
                            )

                            if (completedExpanded) {
                                if (completedProjects.isEmpty()) {
                                    Text(
                                        text = "No completed projects",
                                        modifier = Modifier.padding(16.dp),
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
                                                onDeleteClicked = { projectToDelete ->
                                                    if (projectToDelete.createdBy == userId.toString()) {
                                                        projectViewModel.deleteProject(projectToDelete.id)
                                                    }
                                                },
                                                showDelete = project.createdBy == userId.toString()
                                            )
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            SectionHeader(
                                title = "Ongoing Projects (${ongoingProjects.size})",
                                isExpanded = ongoingExpanded,
                                onToggleClick = {
                                    ongoingExpanded = !ongoingExpanded
                                    focusManager.clearFocus()
                                },
                                onSeeAllClick = {
                                    navController.navigate(Screen.OngoingProjects.route)
                                    focusManager.clearFocus()
                                }
                            )
                        }

                        if (ongoingExpanded) {
                            items(ongoingProjects, key = { it.id }) { project ->
                                ProjectCard(
                                    project = project,
                                    onDeleteClicked = {
                                        if (project.createdBy == userId.toString()) {
                                            projectViewModel.deleteProject(project.id)
                                        }
                                    },
                                    onMarkComplete = {
                                        if (project.createdBy == userId.toString()) {
                                            val updatedProject = project.copy(isCompleted = true)
                                            projectViewModel.updateProject(
                                                project.id,
                                                updatedProject
                                            )
                                        }
                                    },
                                    navController = navController,
                                    showActions = project.createdBy == userId.toString()
                                )
                            }

                            if (ongoingProjects.isEmpty()) {
                                item {
                                    Text(
                                        text = "No ongoing projects",
                                        modifier = Modifier.padding(16.dp),
                                        color = Color.Gray
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