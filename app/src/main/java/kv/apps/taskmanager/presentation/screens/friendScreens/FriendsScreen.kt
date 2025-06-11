package kv.apps.taskmanager.presentation.screens.friendScreens

import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import kotlinx.coroutines.delay
import kv.apps.taskmanager.presentation.navigation.Screen
import kv.apps.taskmanager.presentation.shared.friendsComposables.FriendCard
import kv.apps.taskmanager.presentation.shared.friendsComposables.PendingFriendRequestCard
import kv.apps.taskmanager.presentation.shared.uiComposables.BottomNavigationBar
import kv.apps.taskmanager.presentation.shared.uiComposables.TopBar
import kv.apps.taskmanager.presentation.viewmodel.auth.AuthViewModel
import kv.apps.taskmanager.presentation.viewmodel.userFriends.UserFriendsStateType
import kv.apps.taskmanager.presentation.viewmodel.userFriends.UserFriendsViewModel
import kv.apps.taskmanager.theme.backgroundColor
import kv.apps.taskmanager.theme.mainAppColor

@Composable
fun FriendsScreen(
    navController: NavController,
    userFriendsViewModel: UserFriendsViewModel,
    authViewModel: AuthViewModel
) {
    val focusManager = LocalFocusManager.current
    val currentUserId = authViewModel.uiState.collectAsState().value.userId

    val nonNullCurrentUserId = currentUserId

    val uiState = userFriendsViewModel.uiState.collectAsState()
    val friendsState = uiState.value.friends
    val pendingFriendRequestsState = uiState.value.pendingFriendRequests
    val isLoading = uiState.value.isLoading
    val acceptFriendRequestState = uiState.value.acceptFriendRequestState
    val rejectFriendRequestState = uiState.value.rejectFriendRequestState

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

    var showSnackBar by remember { mutableStateOf(false) }
    var snackBarMessage by remember { mutableStateOf("") }

    LaunchedEffect(nonNullCurrentUserId) {
        nonNullCurrentUserId?.let { userId ->
            userFriendsViewModel.getFriends(userId)
            userFriendsViewModel.getPendingFriendRequests(userId)
        }
    }

    LaunchedEffect(acceptFriendRequestState) {
        when {
            acceptFriendRequestState?.isSuccess == true -> {
                snackBarMessage = "Friend request accepted!"
                showSnackBar = true
                userFriendsViewModel.resetState(UserFriendsStateType.ACCEPT_REQUEST)
            }
            acceptFriendRequestState?.isFailure == true -> {
                snackBarMessage = "Failed to accept friend request: ${acceptFriendRequestState.exceptionOrNull()?.message}"
                showSnackBar = true
                userFriendsViewModel.resetState(UserFriendsStateType.ACCEPT_REQUEST)
            }
            else -> Unit
        }
    }

    LaunchedEffect(rejectFriendRequestState) {
        when {
            rejectFriendRequestState?.isSuccess == true -> {
                snackBarMessage = "Friend request rejected!"
                showSnackBar = true
                userFriendsViewModel.resetState(UserFriendsStateType.REJECT_REQUEST)
            }
            rejectFriendRequestState?.isFailure == true -> {
                snackBarMessage = "Failed to reject friend request: ${rejectFriendRequestState.exceptionOrNull()?.message}"
                showSnackBar = true
                userFriendsViewModel.resetState(UserFriendsStateType.REJECT_REQUEST)
            }
            else -> Unit
        }
    }

    if (showSnackBar) {
        LaunchedEffect(showSnackBar) {
            delay(3000)
            showSnackBar = false
        }
        Snackbar(
            modifier = Modifier.padding(16.dp),
            action = {
                IconButton(
                    onClick = { showSnackBar = false }
                ) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                }
            }
        ) {
            Text(text = snackBarMessage, color = Color.White)
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
                    tint = Color.Black
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
                        onBackPressed = { navController.popBackStack() }
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
                                        text = "Error: ${pendingFriendRequestsState.exceptionOrNull()?.message}",
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

                        HorizontalDivider(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 16.dp),
                            thickness = 2.dp,
                            color = Color.Gray.copy(alpha = 0.7f)
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
                                            currentUserId = nonNullCurrentUserId ?: "",
                                            userFriendsViewModel = userFriendsViewModel
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
                                            text = "Error: ${friendsState.exceptionOrNull()?.message}",
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