package kv.apps.taskmanager.presentation.screens.utils.shared.uiComposables

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import kv.apps.taskmanager.theme.backgroundColor
import kv.apps.taskmanager.theme.mainAppColor

@Composable
fun DeleteConfirmationDialog(
    title: String,
    itemName: String,
    onDismissRequest: () -> Unit,
    onConfirm: () -> Unit,
    confirmText: String = "DELETE",
    dismissText: String = "CANCEL"
) {
    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = {
            Text(
                text = title,
                color = mainAppColor
            )
        },
        text = {
            Text(
                text = "Are you sure you want to delete '$itemName'?",
                color = Color.White
            )
        },
        confirmButton = {
            TextButton(
                onClick = onConfirm
            ) {
                Text(confirmText, color = mainAppColor)
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismissRequest
            ) {
                Text(dismissText, color = Color.White)
            }
        },
        containerColor = backgroundColor,
        titleContentColor = mainAppColor,
        textContentColor = Color.White
    )
}