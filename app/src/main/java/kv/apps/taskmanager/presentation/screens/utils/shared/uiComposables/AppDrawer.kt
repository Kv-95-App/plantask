package kv.apps.taskmanager.presentation.screens.utils.shared.uiComposables

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DrawerState
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import kotlinx.coroutines.launch
import kv.apps.taskmanager.presentation.navigation.Screen
import kv.apps.taskmanager.theme.backgroundColor

@Composable
fun AppDrawer(
    navController: NavController,
    onProfileClicked: () -> Unit,
    onLogoutClicked: () -> Unit,
    drawerState: DrawerState,
    isLoggingOut: Boolean,
    content: @Composable () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()

    ModalNavigationDrawer(
        drawerState = drawerState,
        gesturesEnabled = true,
        scrimColor = Color.Black.copy(alpha = 0.5f),
        drawerContent = {
            Column(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(280.dp)
                    .background(backgroundColor)
                    .padding(horizontal = 16.dp)
                    .clickable{},
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 24.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.End
                ) {
                    IconButton(
                        onClick = { coroutineScope.launch { drawerState.close() } },
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close menu",
                            tint = Color.White
                        )
                    }
                }

                Column(
                    modifier = Modifier.padding(horizontal = 8.dp)
                ) {
                    DrawerMenuItem(
                        text = "Dashboard",
                        onClick = {
                            navController.navigate(Screen.ProjectList.route)
                            coroutineScope.launch { drawerState.close() }
                        }
                    )

                    DrawerMenuItem(
                        text = "Profile",
                        onClick = {
                            onProfileClicked()
                            coroutineScope.launch { drawerState.close() }
                        }
                    )
                    if (isLoggingOut) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 12.dp, horizontal = 16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = MaterialTheme.colorScheme.primary,
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                "Logging out...",
                                color = Color.White,
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                    } else {
                        DrawerMenuItem(
                            text = "Logout",
                            onClick = {
                                onLogoutClicked()
                                coroutineScope.launch { drawerState.close() }
                            }
                        )
                    }
                }
            }
        }
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clickable(
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() }
                ) {
                    if (drawerState.isOpen) {
                        coroutineScope.launch { drawerState.close() }
                    }
                }
        ) {
            content()
        }
    }
}

@Composable
private fun DrawerMenuItem(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp, horizontal = 16.dp)
    ) {
        Text(
            text = text,
            color = Color.White,
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(start = 8.dp)
        )
    }
    HorizontalDivider(
        color = Color.White.copy(alpha = 0.2f),
        thickness = 1.dp,
        modifier = Modifier.padding(vertical = 4.dp)
    )
}