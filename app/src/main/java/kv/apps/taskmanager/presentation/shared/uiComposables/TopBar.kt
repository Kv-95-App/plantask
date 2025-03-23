package kv.apps.taskmanager.presentation.shared.uiComposables

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kv.apps.taskmanager.R
import kv.apps.taskmanager.presentation.viewmodel.AuthViewModel
import kv.apps.taskmanager.theme.backgroundColor

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopBar(
    navController: NavController,
    authViewModel: AuthViewModel,
    onProfileClicked: () -> Unit,
    onLogoutClicked: () -> Unit,
    showBackArrow: Boolean = false,
    onBackPressed: (() -> Unit)? = null
) {
    var expanded by remember { mutableStateOf(false) }
    var isBackButtonEnabled by remember { mutableStateOf(true) }
    val coroutineScope = rememberCoroutineScope()

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(backgroundColor)
            .height(IntrinsicSize.Min), // Ensure the Row takes minimum height
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Left section (Back arrow or empty space)
        Box(
            modifier = Modifier
                .weight(1f)
                .padding(start = if (showBackArrow) 0.dp else 16.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            if (showBackArrow) {
                IconButton(
                    onClick = {
                        if (isBackButtonEnabled) {
                            isBackButtonEnabled = false
                            onBackPressed?.invoke() ?: navController.popBackStack()

                            // Re-enable the button after a short delay
                            coroutineScope.launch {
                                delay(1000) // Prevents rapid double taps
                                isBackButtonEnabled = true
                            }
                        }
                    },
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "",
                        tint = Color.White
                    )
                }
            }
        }

        // Centered logo
        Box(
            modifier = Modifier.weight(2f),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.plantasksmall),
                contentDescription = "Task Management",
                modifier = Modifier
                    .height(90.dp)
                    .fillMaxWidth(), // Ensure the logo takes full width within its Box
                contentScale = ContentScale.Fit
            )
        }

        // Right section (Profile icon)
        Box(
            modifier = Modifier
                .weight(1f)
                .padding(end = 16.dp),
            contentAlignment = Alignment.CenterEnd
        ) {
            IconButton(
                onClick = { expanded = true },
                modifier = Modifier.size(48.dp) // Ensure consistent size
            ) {
                Icon(
                    imageVector = Icons.Default.AccountCircle,
                    contentDescription = "Profile",
                    tint = Color.White,
                    modifier = Modifier.size(42.dp)
                )
            }

            // Dropdown menu
            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = Modifier.background(Color.White),
                offset = DpOffset(8.dp, 0.dp)
            ) {
                DropdownMenuItem(
                    text = { Text("Profile") },
                    onClick = {
                        expanded = false
                        onProfileClicked()
                    }
                )
                DropdownMenuItem(
                    text = { Text("Logout") },
                    onClick = {
                        expanded = false
                        onLogoutClicked()
                    }
                )
            }
        }
    }
}
