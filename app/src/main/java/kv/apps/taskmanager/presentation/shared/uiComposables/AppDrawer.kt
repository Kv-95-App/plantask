package kv.apps.taskmanager.presentation.shared.uiComposables

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.material3.Divider
import androidx.compose.material3.DrawerState
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
import kotlinx.coroutines.launch
import kv.apps.taskmanager.theme.backgroundColor

@Composable
fun AppDrawer(
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
                    .width(240.dp)
                    .background(backgroundColor)
                    .padding(16.dp)
                    .clickable {  },
                horizontalAlignment = Alignment.End
            ) {
                IconButton(
                    onClick = { coroutineScope.launch { drawerState.close() } }
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close menu",
                        tint = Color.White
                    )
                }

                Column(modifier = Modifier.padding(top = 16.dp)) {
                    Text(
                        "Profile",
                        color = Color.White,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                onProfileClicked()
                                coroutineScope.launch { drawerState.close() }
                            }
                            .padding(12.dp)
                    )

                    Divider(color = Color.White)

                    if (isLoggingOut) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                color = MaterialTheme.colorScheme.primary,
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Logging out...", color = Color.White)
                        }
                    } else {
                        Text(
                            "Logout",
                            color = Color.White,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    onLogoutClicked()
                                    coroutineScope.launch { drawerState.close() }
                                }
                                .padding(12.dp)
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