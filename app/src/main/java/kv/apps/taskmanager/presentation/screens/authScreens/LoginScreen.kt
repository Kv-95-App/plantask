package kv.apps.taskmanager.presentation.screens.authScreens

import android.annotation.SuppressLint
import android.content.res.Configuration
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import kotlinx.coroutines.launch
import kv.apps.taskmanager.R
import kv.apps.taskmanager.presentation.navigation.Screen
import kv.apps.taskmanager.presentation.viewmodel.auth.AuthErrorType
import kv.apps.taskmanager.presentation.viewmodel.auth.AuthViewModel
import kv.apps.taskmanager.presentation.viewmodel.auth.AuthViewModel.AuthEvent
import kv.apps.taskmanager.theme.backgroundColor
import kv.apps.taskmanager.theme.mainAppColor

@SuppressLint("ConfigurationScreenWidthHeight")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    navController: NavHostController,
    authViewModel: AuthViewModel,
    onLoginSuccess: () -> Unit = {
        navController.navigate(Screen.ProjectList.route) {
            popUpTo("login") { inclusive = true }
        }
    },
    onGoogleSignInClicked: () -> Unit
) {
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
    val screenHeight = configuration.screenHeightDp.dp

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var keepLoggedIn by remember { mutableStateOf(false) }
    var passwordVisible by remember { mutableStateOf(false) }
    val focusManager = LocalFocusManager.current
    val scrollState = rememberScrollState()
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    val uiState by authViewModel.uiState.collectAsState()
    val isLoading = uiState.isLoading

    fun showSnackbar(message: String) {
        coroutineScope.launch {
            snackbarHostState.showSnackbar(message)
        }
    }

    LaunchedEffect(Unit) {
        authViewModel.events.collect { event ->
            when (event) {
                is AuthEvent.NavigateToHome -> onLoginSuccess()
                is AuthEvent.Error -> {
                    when (event.type) {
                        AuthErrorType.LoginError -> {
                            showSnackbar("Login failed: ${event.message}")
                        }
                        AuthErrorType.NetworkError -> {
                            showSnackbar("Network error: ${event.message}")
                        }
                        else -> {
                            showSnackbar("Error: ${event.message}")
                        }
                    }
                }
                else -> {}
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .clickable(onClick = { focusManager.clearFocus() })
                .padding(top = 4.dp,bottom = 12.dp, start = 16.dp, end = 16.dp),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (!isLandscape) {
                Spacer(modifier = Modifier.height(2.dp))
                Image(
                    painter = painterResource(id = R.drawable.plantasklogo),
                    contentDescription = "Task Management",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(screenHeight * 0.18f),
                    contentScale = ContentScale.Fit
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(horizontal = if (isLandscape) 16.dp else 0.dp)
            ) {
                Text(
                    text = "Login to",
                    color = Color.White,
                    fontSize = 30.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "PlanTask",
                    color = mainAppColor,
                    fontSize = 38.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }

            Spacer(modifier = Modifier.height(if (isLandscape) 16.dp else 6.dp))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = if (isLandscape) 32.dp else 0.dp)
            ) {
                TextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email", color = Color.White) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = textFieldColorsLogin(),
                    keyboardOptions = KeyboardOptions(
                        imeAction = ImeAction.Next,
                        keyboardType = KeyboardType.Email
                    )
                )

                Spacer(modifier = Modifier.height(12.dp))

                TextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Password", color = Color.White) },
                    modifier = Modifier.fillMaxWidth(),
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Password,
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = { focusManager.clearFocus() }
                    ),
                    trailingIcon = {
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(
                                imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                contentDescription = if (passwordVisible) "Hide password" else "Show password",
                                tint = Color.White
                            )
                        }
                    },
                    colors = textFieldColorsLogin()
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = keepLoggedIn,
                        onCheckedChange = { keepLoggedIn = it },
                        colors = CheckboxDefaults.colors(
                            checkedColor = mainAppColor,
                            checkmarkColor = Color.Black,
                            uncheckedColor = Color.White
                        )
                    )
                    Text(
                        "Keep me logged in",
                        color = Color.White,
                        fontSize = 16.sp
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = { authViewModel.login(email, password, keepLoggedIn) },
                    colors = ButtonDefaults.buttonColors(containerColor = mainAppColor),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    enabled = !isLoading && email.isNotBlank() && password.isNotBlank()
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            color = Color.Black,
                            modifier = Modifier.size(24.dp)
                        )
                    } else {
                        Text(
                            "Login",
                            color = Color.Black,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    HorizontalDivider(
                        modifier = Modifier.weight(1f),
                        thickness = 1.dp,
                        color = Color.Gray
                    )
                    Text(
                        text = "OR",
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )
                    HorizontalDivider(
                        modifier = Modifier.weight(1f),
                        thickness = 1.dp,
                        color = Color.Gray
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {Text(
                    text = "Sign in with Google",
                    color = Color.White,
                    fontSize = 14.sp
                )

                    Spacer(modifier = Modifier.height(8.dp))
                    IconButton(
                        onClick = { onGoogleSignInClicked() },
                        modifier = Modifier
                            .size(72.dp)
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.android_logo4),
                            contentDescription = "Sign in with Google",
                            modifier = Modifier
                                .fillMaxSize(),
                            contentScale = ContentScale.Fit
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            TextButton(
                onClick = { navController.navigate(Screen.ForgotPassword.route) }
            ) {
                Text(
                    "Forgot Password?",
                    color = mainAppColor,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }

            Row(
                modifier = Modifier.padding(top = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Don't have an account?",
                    color = Color.White,
                    fontSize = 16.sp
                )
                Spacer(modifier = Modifier.width(8.dp))
                TextButton(
                    onClick = { navController.navigate(Screen.Register.route) }
                ) {
                    Text(
                        "Register",
                        color = mainAppColor,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }
            }
        }

        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 16.dp)
        ) {
            SnackbarHost(hostState = snackbarHostState)
        }
    }
}

@Composable
private fun textFieldColorsLogin() = TextFieldDefaults.colors(
    focusedContainerColor = Color(0xFF333A47),
    unfocusedContainerColor = Color(0xFF333A47),
    focusedIndicatorColor = Color(0xFFFACD3C),
    unfocusedIndicatorColor = Color.Gray,
    cursorColor = Color.White,
    focusedTextColor = Color.White,
    unfocusedTextColor = Color.White,
    focusedLabelColor = Color.White,
    unfocusedLabelColor = Color.Gray
)