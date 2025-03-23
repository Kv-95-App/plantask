// FriendsScreen.kt
package kv.apps.taskmanager.presentation.screens.friendScreens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import kotlinx.coroutines.delay
import kv.apps.taskmanager.presentation.navigation.Screen
import kv.apps.taskmanager.presentation.shared.friendsComposables.FriendCard
import kv.apps.taskmanager.presentation.shared.friendsComposables.PendingFriendRequestCard
import kv.apps.taskmanager.presentation.shared.uiComposables.BottomNavigationBar
import kv.apps.taskmanager.presentation.shared.uiComposables.TopBar
import kv.apps.taskmanager.presentation.viewmodel.AuthViewModel
import kv.apps.taskmanager.presentation.viewmodel.UserFriendsViewModel
import kv.apps.taskmanager.theme.backgroundColor
import kv.apps.taskmanager.theme.mainAppColor

@Composable
fun FriendsScreen(
    navController: NavController,
    userFriendsViewModel: UserFriendsViewModel = hiltViewModel(),
    authViewModel: AuthViewModel = hiltViewModel(),
) {
    val focusManager = LocalFocusManager.current
    val currentUserId by authViewModel.currentUserId.collectAsState()

    val nonNullCurrentUserId = currentUserId

    val friendsState by userFriendsViewModel.friendsState.collectAsState()
    val pendingFriendRequestsState by userFriendsViewModel.pendingFriendRequestsState.collectAsState()
    val isLoading by userFriendsViewModel.isLoadingFriends.collectAsState()
    val acceptFriendRequestState by userFriendsViewModel.acceptFriendRequestState.collectAsState()
    val rejectFriendRequestState by userFriendsViewModel.rejectFriendRequestState.collectAsState()

    val friendsList by remember {
        derivedStateOf {
            friendsState?.getOrNull() ?: emptyList()
        }
    }
    val pendingRequests by remember {
        derivedStateOf {
            pendingFriendRequestsState?.getOrNull() ?: emptyList()
        }
    }

    var showSnackbar by remember { mutableStateOf(false) }
    var snackbarMessage by remember { mutableStateOf("") }

    LaunchedEffect(nonNullCurrentUserId) {
        nonNullCurrentUserId?.let { userId ->
            userFriendsViewModel.getFriends(userId)
            userFriendsViewModel.getPendingFriendRequests(userId)
        }
    }

    LaunchedEffect(acceptFriendRequestState) {
        when {
            acceptFriendRequestState?.isSuccess == true -> {
                snackbarMessage = "Friend request accepted!"
                showSnackbar = true
                userFriendsViewModel.resetState("acceptFriendRequest")
            }
            acceptFriendRequestState?.isFailure == true -> {
                snackbarMessage = "Failed to accept friend request: ${acceptFriendRequestState!!.exceptionOrNull()?.message}"
                showSnackbar = true
                userFriendsViewModel.resetState("acceptFriendRequest")
            }
            else -> Unit
        }
    }

    LaunchedEffect(rejectFriendRequestState) {
        when {
            rejectFriendRequestState?.isSuccess == true -> {
                snackbarMessage = "Friend request rejected!"
                showSnackbar = true
                userFriendsViewModel.resetState("rejectFriendRequest")
            }
            rejectFriendRequestState?.isFailure == true -> {
                snackbarMessage = "Failed to reject friend request: ${rejectFriendRequestState!!.exceptionOrNull()?.message}"
                showSnackbar = true
                userFriendsViewModel.resetState("rejectFriendRequest")
            }
            else -> Unit
        }
    }

    if (showSnackbar) {
        LaunchedEffect(showSnackbar) {
            delay(3000)
            showSnackbar = false
        }
        Snackbar(
            modifier = Modifier.padding(16.dp),
            action = {
                IconButton(
                    onClick = { showSnackbar = false }
                ) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                }
            }
        ) {
            Text(text = snackbarMessage, color = Color.White)
        }
    }

    Scaffold(
        bottomBar = { BottomNavigationBar(navController) },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    navController.navigate(Screen.AddFriend.route)
                },
                containerColor = mainAppColor
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add Friend",
                    tint = Color.White
                )
            }
        },
        containerColor = backgroundColor
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .clickable { focusManager.clearFocus() },
        ) {
            if (isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = mainAppColor)
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                ) {
                    TopBar(
                        navController = navController,
                        onProfileClicked = { /* Handle profile click */ },
                        onLogoutClicked = {
                            navController.navigate("login") {
                                popUpTo("login") { inclusive = true }
                            }
                        },
                        showBackArrow = true,
                        onBackPressed = { navController.popBackStack() },
                        authViewModel = hiltViewModel()
                    )

                    Column(
                        modifier = Modifier
                            .verticalScroll(rememberScrollState())
                            .weight(1f)
                            .fillMaxWidth()
                    ) {
                        Text(
                            text = "Pending Friend Requests",
                            color = Color.White,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(16.dp)
                        )

                        if (pendingFriendRequestsState?.isSuccess == true) {
                            if (pendingRequests.isNotEmpty()) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .heightIn(max = 200.dp)
                                ) {
                                    LazyColumn(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                    ) {
                                        items(pendingRequests) { user ->
                                            PendingFriendRequestCard(
                                                user = user,
                                                currentUserId = nonNullCurrentUserId ?: "",
                                                userFriendsViewModel = userFriendsViewModel,
                                                modifier = Modifier.padding(8.dp)
                                            )
                                        }
                                    }
                                }
                            } else {
                                Text(
                                    text = "No pending friend requests",
                                    color = Color.White,
                                    modifier = Modifier.padding(16.dp)
                                )
                            }
                        } else if (pendingFriendRequestsState?.isFailure == true) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = "Error: ${pendingFriendRequestsState?.exceptionOrNull()?.message}",
                                        color = Color.Red
                                    )
                                    Spacer(modifier = Modifier.height(16.dp))
                                    Button(
                                        onClick = {
                                            nonNullCurrentUserId?.let { userId ->
                                                userFriendsViewModel.getPendingFriendRequests(userId)
                                            }
                                        },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = mainAppColor
                                        )
                                    ) {
                                        Text("Retry")
                                    }
                                }
                            }
                        }

                        Divider(
                            color = Color.Gray.copy(alpha = 0.7f),
                            thickness = 2.dp,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 16.dp)
                        )

                        Text(
                            text = "Friend List",
                            color = Color.White,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(16.dp)
                        )

                        when {
                            friendsState?.isSuccess == true && friendsList.isNotEmpty() -> {
                                LazyColumn(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .weight(1f)
                                ) {
                                    items(friendsList) { friend ->
                                        FriendCard(
                                            friend = friend,
                                            onClick = {
                                                navController.navigate("friendDetail/${friend.friendId}") // Use friendId
                                            },
                                            modifier = Modifier.padding(8.dp),
                                            currentUserId = nonNullCurrentUserId ?: ""
                                        )
                                    }
                                }
                            }
                            friendsState?.isSuccess == true && friendsList.isEmpty() -> {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 32.dp),
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
                                            Text("Add a Friend")
                                        }
                                    }
                                }
                            }
                            friendsState?.isFailure == true -> {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Text(
                                            text = "Error: ${friendsState?.exceptionOrNull()?.message}",
                                            color = Color.Red
                                        )
                                        Spacer(modifier = Modifier.height(16.dp))
                                        Button(
                                            onClick = {
                                                nonNullCurrentUserId?.let { userId ->
                                                    userFriendsViewModel.getFriends(userId)
                                                }
                                            },
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
        }
    }
}