package kv.apps.taskmanager.presentation.screens.authScreens

import android.annotation.SuppressLint
import android.util.Patterns
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
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
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import kotlinx.coroutines.launch
import kv.apps.taskmanager.R
import kv.apps.taskmanager.presentation.navigation.Screen
import kv.apps.taskmanager.presentation.screens.utils.shared.uiComposables.rememberCustomDatePicker
import kv.apps.taskmanager.presentation.viewmodel.auth.AuthErrorType
import kv.apps.taskmanager.presentation.viewmodel.auth.AuthViewModel
import kv.apps.taskmanager.theme.backgroundColor
import kv.apps.taskmanager.theme.mainAppColor
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun RegisterScreen(
    navController: NavHostController,
    authViewModel: AuthViewModel
) {
    var firstName by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }
    var birthday by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }

    val uiState by authViewModel.uiState.collectAsState()
    val isLoading = uiState.isLoading

    val snackBarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    val focusManager = LocalFocusManager.current

    val showDatePicker = rememberCustomDatePicker(
        onDateSelected = { date ->
            birthday = date.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
        }
    )

    LaunchedEffect(Unit) {
        authViewModel.events.collect { event ->
            when (event) {
                is AuthViewModel.AuthEvent.RegistrationSuccess -> {
                    navController.navigate(Screen.ProjectList.route) {
                        popUpTo(Screen.Register.route) { inclusive = true }
                    }
                }

                is AuthViewModel.AuthEvent.Error -> {
                    coroutineScope.launch {
                        snackBarHostState.showSnackbar(event.message)
                    }
                }

                else -> {}
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackBarHostState) { data ->
            Snackbar(
                snackbarData = data,
                containerColor = Color(0xFF323232),
                contentColor = Color.White,
                actionColor = mainAppColor,
                dismissActionContentColor = Color.White
            )
        } }
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(backgroundColor)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .clickable(onClick = { focusManager.clearFocus() })
                    .padding(16.dp),
                verticalArrangement = Arrangement.Top,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                Image(
                    painter = painterResource(id = R.drawable.plantasklogo),
                    contentDescription = "Task Management",
                    modifier = Modifier
                        .fillMaxSize()
                        .height(140.dp),
                    contentScale = ContentScale.Fit
                )

                Text(
                    text = "Create Account",
                    color = Color.White,
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(16.dp))

                CustomTextField(
                    value = firstName,
                    onValueChange = { firstName = it },
                    label = "First Name"
                )

                Spacer(modifier = Modifier.height(12.dp))

                CustomTextField(
                    value = lastName,
                    onValueChange = { lastName = it },
                    label = "Last Name"
                )

                Spacer(modifier = Modifier.height(12.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showDatePicker() }
                ) {
                    TextField(
                        value = birthday,
                        onValueChange = {},
                        label = { Text("Birthday") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = textFieldColors2(),
                        visualTransformation = VisualTransformation.None,
                        readOnly = true,
                        enabled = false
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                CustomTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = "Email"
                )

                Spacer(modifier = Modifier.height(12.dp))

                CustomTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = "Password",
                    visualTransformation = PasswordVisualTransformation()
                )

                Spacer(modifier = Modifier.height(12.dp))

                CustomTextField(
                    value = confirmPassword,
                    onValueChange = { confirmPassword = it },
                    label = "Confirm Password",
                    visualTransformation = PasswordVisualTransformation()
                )

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = {
                        when {
                            firstName.isBlank() || lastName.isBlank() || birthday.isBlank() -> {
                                coroutineScope.launch {
                                    snackBarHostState.showSnackbar("Please fill all fields")
                                }
                            }
                            !isValidEmail(email) -> {
                                coroutineScope.launch {
                                    snackBarHostState.showSnackbar(AuthErrorType.InvalidEmailFormat.message)
                                }
                            }
                            password.length < 6 -> {
                                coroutineScope.launch {
                                    snackBarHostState.showSnackbar(AuthErrorType.WeakPassword.message)
                                }
                            }
                            password != confirmPassword -> {
                                coroutineScope.launch {
                                    snackBarHostState.showSnackbar(AuthErrorType.PasswordMismatch.message)
                                }
                            }
                            else -> {
                                authViewModel.register(firstName, lastName, birthday, email, password)
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = mainAppColor,
                        disabledContainerColor = Color.Gray
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth(0.9f)
                        .height(48.dp),
                    enabled = !isLoading && firstName.isNotBlank() && lastName.isNotBlank() &&
                            birthday.isNotBlank() && email.isNotBlank() &&
                            password.isNotBlank() && confirmPassword.isNotBlank() &&
                            isValidEmail(email) && password.length >= 6 &&
                            password == confirmPassword
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
                        Text(
                            text = "Register",
                            color = if (firstName.isNotBlank() && lastName.isNotBlank() && birthday.isNotBlank() && email.isNotBlank() && password.isNotBlank() && confirmPassword.isNotBlank() && isValidEmail(email) && password.length >= 6 && password == confirmPassword) {
                                Color.Black
                            } else {
                                Color.Black
                            },
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.padding(top = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Already have an account?",
                        color = Color.White,
                        fontSize = 16.sp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    TextButton(
                        onClick = { navController.navigate(Screen.Login.route) }
                    ) {
                        Text(
                            text = "Login",
                            color = Color(0xFFFACD3C),
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    }
                }
            }
        }
    }
}
fun isValidEmail(email: String): Boolean {
    return Patterns.EMAIL_ADDRESS.matcher(email).matches()
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    readOnly: Boolean = false,
    enabled: Boolean = true,
    modifier: Modifier = Modifier
) {
    TextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        modifier = modifier.fillMaxWidth(),
        colors = textFieldColors2(),
        visualTransformation = visualTransformation,
        readOnly = readOnly,
        enabled = enabled
    )
}

@Composable
fun textFieldColors2() = TextFieldDefaults.colors(
    focusedContainerColor = Color(0xFF333A47),
    unfocusedContainerColor = Color(0xFF333A47),
    disabledContainerColor = Color(0xFF333A47),
    focusedIndicatorColor = Color(0xFFFACD3C),
    unfocusedIndicatorColor = Color.Gray,
    disabledIndicatorColor = Color.Gray,
    cursorColor = Color.White,
    focusedTextColor = Color.White,
    unfocusedTextColor = Color.White,
    disabledTextColor = Color.White,
    focusedLabelColor = Color.White,
    unfocusedLabelColor = Color.Gray,
    disabledLabelColor = Color.Gray
)