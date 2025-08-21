package kv.apps.taskmanager.presentation.screens.utils.shared.uiComposables

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import kv.apps.taskmanager.presentation.viewmodel.auth.AuthViewModel
import kv.apps.taskmanager.theme.mainAppColor
import kv.apps.taskmanager.theme.onGoingCardColor

@Composable
fun ChangePasswordDialog(
    onDismiss: () -> Unit,
    onChangePassword: (currentPassword: String, newPassword: String) -> Unit,
    state: AuthViewModel.ChangePasswordState
) {
    var currentPassword by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var showError by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = onGoingCardColor,
        title = {
            Text(
                text = "Change Password",
                style = MaterialTheme.typography.titleLarge.copy(
                    color = mainAppColor,
                    fontWeight = FontWeight.Bold
                )
            )
        },
        text = {
            Column {
                if (state is AuthViewModel.ChangePasswordState.Error) {
                    Text(
                        text = state.message,
                        color = Color.Red,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }

                OutlinedTextField(
                    value = currentPassword,
                    onValueChange = { currentPassword = it },
                    label = { Text("Current Password") },
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = mainAppColor,
                        unfocusedBorderColor = Color.Gray,
                        focusedLabelColor = mainAppColor,
                        cursorColor = mainAppColor
                    )
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = newPassword,
                    onValueChange = { newPassword = it },
                    label = { Text("New Password") },
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = mainAppColor,
                        unfocusedBorderColor = Color.Gray,
                        focusedLabelColor = mainAppColor,
                        cursorColor = mainAppColor
                    )
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = confirmPassword,
                    onValueChange = { confirmPassword = it },
                    label = { Text("Confirm New Password") },
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = mainAppColor,
                        unfocusedBorderColor = Color.Gray,
                        focusedLabelColor = mainAppColor,
                        cursorColor = mainAppColor
                    )
                )

                if (showError && newPassword != confirmPassword) {
                    Text(
                        text = "Passwords don't match",
                        color = Color.Red,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            }
        },
        confirmButton = {
            if (state is AuthViewModel.ChangePasswordState.Loading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = mainAppColor
                )
            } else {
                Button(
                    onClick = {
                        if (newPassword == confirmPassword) {
                            onChangePassword(currentPassword, newPassword)
                            showError = false
                        } else {
                            showError = true
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = mainAppColor,
                        contentColor = Color.Black
                    )
                ) {
                    Text("Change")
                }
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                colors = ButtonDefaults.textButtonColors(
                    contentColor = mainAppColor
                )
            ) {
                Text("Cancel")
            }
        }
    )
}