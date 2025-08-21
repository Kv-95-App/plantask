package kv.apps.taskmanager.presentation.screens.friendSection.screens

import android.util.Patterns
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kv.apps.taskmanager.presentation.navigation.Screen
import kv.apps.taskmanager.presentation.screens.utils.shared.uiComposables.AppDrawer
import kv.apps.taskmanager.presentation.screens.utils.shared.menuBars.BottomNavigationBar
import kv.apps.taskmanager.presentation.screens.utils.shared.menuBars.TopBar
import kv.apps.taskmanager.presentation.viewmodel.auth.AuthViewModel
import kv.apps.taskmanager.presentation.viewmodel.userFriends.UserFriendsStateType
import kv.apps.taskmanager.presentation.viewmodel.userFriends.UserFriendsViewModel
import kv.apps.taskmanager.theme.backgroundColor
import kv.apps.taskmanager.theme.mainAppColor

@Composable
fun AddFriendScreen(
    navController: NavController,
    userFriendsViewModel: UserFriendsViewModel,
    authViewModel: AuthViewModel,
) {
    val focusManager = LocalFocusManager.current
    val authUiState by authViewModel.uiState.collectAsState()
    val currentUserId = authUiState.userId
    val isLoggingOut = authUiState.isLoggingOut

    var friendEmail by remember { mutableStateOf("") }
    var isEmailValid by remember { mutableStateOf(true) }

    val uiState = userFriendsViewModel.uiState.collectAsState()
    val addFriendState = uiState.value.addFriendState
    val isLoading = uiState.value.isLoading

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val coroutineScope = rememberCoroutineScope()

    var showSuccessMessage by remember { mutableStateOf(false) }
    var successMessage by remember { mutableStateOf("") }

    val validateEmail = { email: String ->
        email.isNotBlank() && Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    val handleAddFriend = {
        if (validateEmail(friendEmail)) {
            isEmailValid = true
            if (currentUserId != null) {
                userFriendsViewModel.addFriend(currentUserId, friendEmail)
                focusManager.clearFocus()
            }
        } else {
            isEmailValid = false
        }
    }

    LaunchedEffect(addFriendState) {
        if (addFriendState != null) {
            if (addFriendState.isSuccess) {
                successMessage = "Friend request sent successfully!"
                showSuccessMessage = true
                friendEmail = ""

                delay(3000)
                showSuccessMessage = false
            } else {
                successMessage = addFriendState.exceptionOrNull()?.message ?: "Failed to send friend request"
                showSuccessMessage = true

                delay(3000)
                showSuccessMessage = false
            }
            userFriendsViewModel.resetState(UserFriendsStateType.ADD_FRIEND)
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
            modifier = Modifier.fillMaxSize(),
            topBar = {
                TopBar(
                    navController = navController,
                    onMenuClicked = { coroutineScope.launch { drawerState.open() } },
                    showBackArrow = true,
                    onBackPressed = { navController.popBackStack() }
                )
            },
            bottomBar = { BottomNavigationBar(navController) },
            containerColor = backgroundColor
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(backgroundColor)
                    .clickable { focusManager.clearFocus() }
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .padding(16.dp)
                ) {
                    Text(
                        text = "Add Friend",
                        fontWeight = FontWeight.Bold,
                        fontSize = 24.sp,
                        color = Color(0xFFFFC107)
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    Text(
                        text = "Enter your friend's email address to send them a friend request",
                        color = Color.White,
                        fontSize = 16.sp
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    OutlinedTextField(
                        value = friendEmail,
                        onValueChange = {
                            friendEmail = it
                            if (!isEmailValid) {
                                isEmailValid = true
                            }
                            if (showSuccessMessage) {
                                showSuccessMessage = false
                            }
                        },
                        label = { Text("Friend's Email") },
                        isError = !isEmailValid,
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Email,
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(
                            onDone = { handleAddFriend() }
                        ),
                        trailingIcon = {
                            if (friendEmail.isNotEmpty()) {
                                IconButton(onClick = { friendEmail = "" }) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Clear",
                                        tint = mainAppColor
                                    )
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = mainAppColor,
                            unfocusedBorderColor = Color.Gray,
                            errorBorderColor = Color.Red,
                            cursorColor = mainAppColor,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        )
                    )

                    if (!isEmailValid) {
                        Text(
                            text = "Please enter a valid email address",
                            color = Color.Red,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(start = 4.dp, top = 4.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = { handleAddFriend() },
                        enabled = !isLoading && currentUserId != null && friendEmail.isNotEmpty(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = mainAppColor,
                            disabledContainerColor = Color.Gray,
                            contentColor = Color.Black
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text("Send Friend Request", fontSize = 16.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    AnimatedVisibility(visible = showSuccessMessage) {
                        val isSuccess = addFriendState?.isSuccess ?: false
                        val messageColor = if (isSuccess) Color.Green else Color.Red

                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = messageColor.copy(alpha = 0.1f)
                            ),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = successMessage,
                                    color = messageColor,
                                    fontSize = 16.sp
                                )
                            }
                        }
                    }

                    AnimatedVisibility(
                        visible = addFriendState != null && !showSuccessMessage
                    ) {
                        val result = addFriendState
                        val (message, color) = if (result?.isSuccess == true) {
                            "Friend request sent successfully!" to Color.Green
                        } else {
                            val errorMsg = result?.exceptionOrNull()?.message ?: "Failed to send friend request"
                            errorMsg to Color.Red
                        }

                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = color.copy(alpha = 0.1f)
                            ),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = message,
                                    color = color,
                                    fontSize = 16.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}