package kv.apps.taskmanager.presentation.screens.friendScreens

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import kotlinx.coroutines.delay
import kv.apps.taskmanager.presentation.navigation.Screen
import kv.apps.taskmanager.presentation.shared.uiComposables.BottomNavigationBar
import kv.apps.taskmanager.presentation.shared.uiComposables.TopBar
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

    var friendEmail by remember { mutableStateOf("") }
    var isEmailValid by remember { mutableStateOf(true) }

    val uiState = userFriendsViewModel.uiState.collectAsState()
    val addFriendState = uiState.value.addFriendState
    val isLoading = uiState.value.isLoading

    val validateEmail = { email: String ->
        email.isNotBlank() && android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    val currentUserId = authViewModel.uiState.collectAsState().value.userId

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
            delay(3000)
            userFriendsViewModel.resetState(UserFriendsStateType.ADD_FRIEND)
        }
    }

    Scaffold(
        bottomBar = { BottomNavigationBar(navController) },
        containerColor = backgroundColor
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .clickable { focusManager.clearFocus() },
            contentAlignment = Alignment.TopCenter
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                TopBar(
                    navController = navController,
                    onProfileClicked = { Screen.Profile.route },
                    onLogoutClicked = {
                        navController.navigate(Screen.Login.route) {
                            popUpTo("login") { inclusive = true }
                        }
                    },
                    showBackArrow = true,
                    onBackPressed = { navController.popBackStack() }
                )

                Column(
                    modifier = Modifier
                        .fillMaxSize()
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
                        enabled = !isLoading && currentUserId != null,
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

                    AnimatedVisibility(
                        visible = addFriendState != null
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
