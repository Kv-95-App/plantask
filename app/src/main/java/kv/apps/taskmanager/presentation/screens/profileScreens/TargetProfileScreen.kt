package kv.apps.taskmanager.presentation.screens.profileScreens

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import kotlinx.coroutines.launch
import kv.apps.taskmanager.presentation.screens.utils.shared.menuBars.TopBar
import kv.apps.taskmanager.presentation.screens.utils.shared.uiComposables.AppDrawer
import kv.apps.taskmanager.presentation.viewmodel.auth.AuthViewModel
import kv.apps.taskmanager.presentation.viewmodel.userFriends.UserFriendsViewModel
import kv.apps.taskmanager.theme.backgroundColor
import kv.apps.taskmanager.theme.contrastingTextColor
import kv.apps.taskmanager.theme.mainAppColor

@Composable
fun TargetProfileScreen(
    navController: NavController,
    userId: String,
    userFriendsViewModel: UserFriendsViewModel,
    authViewModel: AuthViewModel,
    showBackArrow: Boolean = true
) {
    val authUiState by authViewModel.uiState.collectAsState()
    val uiState by userFriendsViewModel.uiState.collectAsState()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val coroutineScope = rememberCoroutineScope()

    val isLoggingOut = authUiState.isLoggingOut

    LaunchedEffect(userId) {
        userFriendsViewModel.fetchTargetUser(userId)
    }
    AppDrawer(
        onProfileClicked = { },
        onLogoutClicked = {
            authViewModel.logout()
        },
        drawerState = drawerState,
        isLoggingOut = isLoggingOut,
        navController = navController
    ) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            topBar = {
                TopBar(
                    navController = navController,
                    onMenuClicked = {coroutineScope.launch { drawerState.open() }  },
                    showBackArrow = showBackArrow
                )
            },
            containerColor = backgroundColor
        ) { paddingValues ->
            when {
                uiState.isLoadingTargetUser -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = mainAppColor)
                    }
                }

                uiState.targetUser != null -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValues)
                            .verticalScroll(rememberScrollState())
                            .background(backgroundColor)
                            .padding(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(80.dp)
                                    .background(mainAppColor, CircleShape)
                                    .clip(CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = getInitials(
                                        uiState.targetUser!!.firstName,
                                        uiState.targetUser!!.lastName
                                    ),
                                    color = contrastingTextColor(mainAppColor),
                                    style = MaterialTheme.typography.headlineLarge.copy(
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                            }

                            Spacer(modifier = Modifier.width(16.dp))

                            Column {
                                Text(
                                    text = "${uiState.targetUser!!.firstName} ${uiState.targetUser!!.lastName}",
                                    style = MaterialTheme.typography.headlineMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = uiState.targetUser!!.email,
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        color = Color.Gray
                                    )
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        SectionCard(title = "Personal Information") {
                            ProfileDetailItem(
                                title = "First Name",
                                value = uiState.targetUser!!.firstName
                            )
                            ProfileDetailItem(
                                title = "Last Name",
                                value = uiState.targetUser!!.lastName
                            )
                            ProfileDetailItem(
                                title = "Birthday",
                                value = uiState.targetUser!!.birthday ?: "Not provided"
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        SectionCard(title = "Account Information") {
                            ProfileDetailItem(
                                title = "Email",
                                value = uiState.targetUser!!.email
                            )
                        }
                    }
                }

                else -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("User not found", color = Color.White)
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(
                                onClick = { userFriendsViewModel.fetchTargetUser(userId) },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = mainAppColor
                                )
                            ) {
                                Text("Retry")
                            }
                        }
                    }
                }
            }
        }
    }
}