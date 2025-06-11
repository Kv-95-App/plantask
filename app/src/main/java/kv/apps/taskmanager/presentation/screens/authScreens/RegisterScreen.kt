package kv.apps.taskmanager.presentation.screens.authScreens

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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.rememberDatePickerState
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
import kv.apps.taskmanager.presentation.viewmodel.auth.AuthViewModel
import kv.apps.taskmanager.theme.backgroundColor
import kv.apps.taskmanager.theme.mainAppColor
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
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

    var showDatePicker by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState()
    val focusManager = LocalFocusManager.current

    LaunchedEffect(Unit) {
        authViewModel.events.collect { event ->
            when (event) {
                is AuthViewModel.AuthEvent.RegistrationSuccess -> {
                    navController.navigate("login") {
                        popUpTo("register") { inclusive = true }
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

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            snackbarHost = { SnackbarHost(hostState = snackBarHostState) }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(backgroundColor)
                    .padding(paddingValues)
                    .padding(16.dp)
                    .clickable(onClick = { focusManager.clearFocus() }),
                verticalArrangement = Arrangement.Top,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(40.dp))

                Image(
                    painter = painterResource(id = R.drawable.plantask_transparent),
                    contentDescription = "Task Management",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    contentScale = ContentScale.Fit
                )

                Spacer(modifier = Modifier.height(40.dp))

                Text(
                    text = "Create Account",
                    color = Color.White,
                    fontSize = 30.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(20.dp))

                CustomTextField(
                    value = firstName,
                    onValueChange = { firstName = it },
                    label = "First Name"
                )

                Spacer(modifier = Modifier.height(16.dp))

                CustomTextField(
                    value = lastName,
                    onValueChange = { lastName = it },
                    label = "Last Name"
                )

                Spacer(modifier = Modifier.height(16.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showDatePicker = true }
                ) {
                    TextField(
                        value = birthday,
                        onValueChange = {},
                        label = { Text("Birthday") },
                        modifier = Modifier.fillMaxWidth(),
                        readOnly = true,
                        colors = textFieldColors(),
                        enabled = false
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                CustomTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = "Email"
                )

                Spacer(modifier = Modifier.height(16.dp))

                CustomTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = "Password",
                    visualTransformation = PasswordVisualTransformation()
                )

                Spacer(modifier = Modifier.height(16.dp))

                CustomTextField(
                    value = confirmPassword,
                    onValueChange = { confirmPassword = it },
                    label = "Confirm Password",
                    visualTransformation = PasswordVisualTransformation()
                )

                Spacer(modifier = Modifier.height(40.dp))

                Button(
                    onClick = {
                        if (firstName.isBlank() || lastName.isBlank() || birthday.isBlank()) {
                            coroutineScope.launch {
                                snackBarHostState.showSnackbar("Please fill all fields")
                            }
                            return@Button
                        }

                        if (!isValidEmail(email)) {
                            coroutineScope.launch {
                                snackBarHostState.showSnackbar("Invalid email format")
                            }
                            return@Button
                        }

                        if (password.length < 6) {
                            coroutineScope.launch {
                                snackBarHostState.showSnackbar("Password must be at least 6 characters")
                            }
                            return@Button
                        }

                        if (password != confirmPassword) {
                            coroutineScope.launch {
                                snackBarHostState.showSnackbar("Passwords don't match")
                            }
                            return@Button
                        }

                        authViewModel.register(firstName, lastName, birthday, email, password)
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = mainAppColor
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth(0.8f)
                        .height(50.dp),
                    enabled = !isLoading
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            color = Color.Black,
                            modifier = Modifier.size(24.dp))
                    } else {
                        Text(
                            text = "Register",
                            color = Color.Black,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.padding(top = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Already have an account?",
                        color = Color.White,
                        fontSize = 16.sp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    TextButton(
                        onClick = { navController.navigate("login") }
                    ) {
                        Text(
                            text = "Login",
                            color = Color(0xFFFACD3C),
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    }
                }

                if (showDatePicker) {
                    AlertDialog(
                        onDismissRequest = { showDatePicker = false },
                        title = { Text("Select Date") },
                        text = {
                            DatePicker(
                                state = datePickerState,
                                title = { Text("Select your birthday") },
                                headline = { Text("Choose a date") },
                                showModeToggle = true
                            )
                        },
                        confirmButton = {
                            Button(
                                onClick = {
                                    val selectedDateMillis = datePickerState.selectedDateMillis
                                    if (selectedDateMillis != null) {
                                        val calendar = Calendar.getInstance()
                                        calendar.timeInMillis = selectedDateMillis
                                        birthday = formatDate(
                                            calendar.get(Calendar.DAY_OF_MONTH),
                                            calendar.get(Calendar.MONTH) + 1,
                                            calendar.get(Calendar.YEAR)
                                        )
                                    }
                                    showDatePicker = false
                                }
                            ) {
                                Text("OK")
                            }
                        },
                        dismissButton = {
                            Button(
                                onClick = { showDatePicker = false }
                            ) {
                                Text("Cancel")
                            }
                        }
                    )
                }
            }
        }
    }
}

fun isValidEmail(email: String): Boolean {
    return Patterns.EMAIL_ADDRESS.matcher(email).matches()
}

fun formatDate(day: Int, month: Int, year: Int): String {
    return "$day/$month/$year"
}

@Composable
fun CustomTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    visualTransformation: VisualTransformation = VisualTransformation.None
) {
    TextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        modifier = Modifier.fillMaxWidth(),
        colors = textFieldColors(),
        visualTransformation = visualTransformation
    )
}

@Composable
fun textFieldColors() = TextFieldDefaults.colors(
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