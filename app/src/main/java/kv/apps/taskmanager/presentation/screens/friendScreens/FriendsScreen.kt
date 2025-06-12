package kv.apps.taskmanager.presentation.screens.friendScreens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kv.apps.taskmanager.presentation.navigation.Screen
import kv.apps.taskmanager.presentation.shared.friendsComposables.FriendCard
import kv.apps.taskmanager.presentation.shared.friendsComposables.PendingFriendRequestCard
import kv.apps.taskmanager.presentation.shared.uiComposables.AppDrawer
import kv.apps.taskmanager.presentation.shared.uiComposables.BottomNavigationBar
import kv.apps.taskmanager.presentation.shared.uiComposables.TopBar
import kv.apps.taskmanager.presentation.viewmodel.auth.AuthViewModel
import kv.apps.taskmanager.presentation.viewmodel.userFriends.UserFriendsEvent
import kv.apps.taskmanager.presentation.viewmodel.userFriends.UserFriendsViewModel
import kv.apps.taskmanager.theme.backgroundColor
import kv.apps.taskmanager.theme.mainAppColor

@Composable
fun FriendsScreen(
    navController: NavController,
    userFriendsViewModel: UserFriendsViewModel,
    authViewModel: AuthViewModel
) {
    val currentUserId = authViewModel.uiState.collectAsState().value.userId
    val hasUserId = currentUserId != null

    val uiState by userFriendsViewModel.uiState.collectAsState()
    val friendsState = uiState.friends
    val pendingRequestsState = uiState.pendingFriendRequests
    val isLoading = uiState.isLoading

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    val events = userFriendsViewModel.events.collectAsState(initial = null)
    LaunchedEffect(events.value) {
        events.value?.let { event ->
            when (event) {
                is UserFriendsEvent.Error -> {
                    snackbarHostState.showSnackbar(event.message)
                }
                else -> Unit
            }
        }
    }

    var hasLoadedInitialData by remember { mutableStateOf(false) }
    LaunchedEffect(currentUserId) {
        if (currentUserId != null && !hasLoadedInitialData) {
            userFriendsViewModel.loadInitialData(currentUserId)
            hasLoadedInitialData = true
        }
    }

    LaunchedEffect(navController) {
        navController.currentBackStackEntry?.savedStateHandle?.get<Boolean>("refreshFriends")?.let {
            if (it && currentUserId != null) {
                userFriendsViewModel.getFriends(currentUserId)
                userFriendsViewModel.getPendingFriendRequests(currentUserId)
                navController.currentBackStackEntry?.savedStateHandle?.remove<Boolean>("refreshFriends")
            }
        }
    }

    if (!hasUserId) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(backgroundColor),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(color = mainAppColor)
        }
        return
    }

    if (isLoading && friendsState == null && pendingRequestsState == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(backgroundColor),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = mainAppColor)
            }
        return
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
        isLoggingOut = authViewModel.uiState.collectAsState().value.isLoggingOut
    ) {
        Scaffold(
            bottomBar = { BottomNavigationBar(navController) },
            floatingActionButton = {
                FloatingActionButton(
                    onClick = { navController.navigate(Screen.AddFriend.route) },
                    containerColor = mainAppColor
                ) {
                    Icon(
                        Icons.Default.Add,
                        contentDescription = "Add Friend",
                        tint = Color.Black
                    )
                }
            },
            containerColor = backgroundColor
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .background(backgroundColor)
            ) {
                TopBar(
                    navController = navController,
                    onMenuClicked = {
                        coroutineScope.launch {
                            drawerState.open()
                        }
                    },
                    showBackArrow = false,
                    onBackPressed = { navController.popBackStack() },
                    modifier = Modifier.padding(top = 24.dp)
                )

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) {
                    Text(
                        text = "Pending Friend Requests",
                        color = Color.White,
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.padding(16.dp)
                    )

                    when {
                        uiState.isLoadingPendingRequests -> {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(100.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(color = mainAppColor)
                            }
                        }
                        pendingRequestsState == null -> {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(100.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "Pull to refresh",
                                    color = Color.White
                                )
                            }
                        }
                        pendingRequestsState.isSuccess -> {
                            val pendingRequests = pendingRequestsState.getOrNull() ?: emptyList()
                            if (pendingRequests.isNotEmpty()) {
                                LazyColumn(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .heightIn(max = 200.dp)
                                ) {
                                    items(pendingRequests) { user ->
                                        PendingFriendRequestCard(
                                            user = user,
                                            currentUserId = currentUserId,
                                            userFriendsViewModel = userFriendsViewModel,
                                            modifier = Modifier.padding(8.dp)
                                        )
                                    }
                                }
                            } else {
                                Text(
                                    text = "No pending friend requests",
                                    color = Color.White,
                                    modifier = Modifier.padding(16.dp)
                                )
                            }
                        }
                        pendingRequestsState.isFailure -> {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = "Failed to load pending requests",
                                        color = Color.Red
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Button(
                                        onClick = { currentUserId.let { userFriendsViewModel.getPendingFriendRequests(it) } },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = mainAppColor
                                        )
                                    ) {
                                        Text("Retry", color = Color.Black)
                                    }
                                }
                            }
                        }
                    }

                    Divider(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        thickness = 1.dp,
                        color = Color.Gray.copy(alpha = 0.5f)
                    )

                    Text(
                        text = "Friend List",
                        color = Color.White,
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.padding(16.dp)
                    )

                    when {
                        uiState.isLoadingFriends -> {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(1f),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(color = mainAppColor)
                            }
                        }
                        friendsState == null -> {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(1f),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "Pull to refresh",
                                    color = Color.White
                                )
                            }
                        }
                        friendsState.isSuccess -> {
                            val friendsList = friendsState.getOrNull() ?: emptyList()
                            if (friendsList.isNotEmpty()) {
                                LazyColumn(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .weight(1f)
                                ) {
                                    items(friendsList) { friend ->
                                        FriendCard(
                                            friend = friend,
                                            onClick = { /* Handle click */ },
                                            modifier = Modifier.padding(8.dp),
                                            currentUserId = currentUserId,
                                            userFriendsViewModel = userFriendsViewModel
                                        )
                                    }
                                }
                            } else {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .weight(1f),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.Center
                                    ) {
                                        Text(text = "No friends found", color = Color.White)
                                        Spacer(modifier = Modifier.height(16.dp))
                                        Button(
                                            onClick = {
                                                navController.navigate(Screen.AddFriend.route)
                                            },
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = mainAppColor
                                            )
                                        ) {
                                            Text("Add a Friend", color = Color.Black)
                                        }
                                    }
                                }
                            }
                        }
                        friendsState.isFailure -> {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(1f),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = "Failed to load friends list",
                                        color = Color.Red
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Button(
                                        onClick = { currentUserId.let { userFriendsViewModel.getFriends(it) } },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = mainAppColor
                                        )
                                    ) {
                                        Text("Retry", color = Color.Black)
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