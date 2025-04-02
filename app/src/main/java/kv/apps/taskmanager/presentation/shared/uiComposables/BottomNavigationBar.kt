package kv.apps.taskmanager.presentation.shared.uiComposables

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.navigation.NavController
import kv.apps.taskmanager.theme.mainAppColor
import kv.apps.taskmanager.utils.MyIcons

@Composable
fun BottomNavigationBar(navController: NavController) {
    val defaultColor = Color.White
    val selectedColor = mainAppColor
    val selectedTextColor = Color(0xFFD39E00)

    val currentRoute = navController.currentBackStackEntry?.destination?.route

    NavigationBar(
        containerColor = Color(0xFF2C2F38)
    ) {
        NavigationBarItem(
            icon = {
                Icon(
                    Icons.Default.Home,
                    contentDescription = "Home",
                    tint = if (currentRoute == "project_list") selectedColor else defaultColor
                )
            },
            label = {
                Text(
                    "Home",
                    color = if (currentRoute == "project_list") selectedTextColor else defaultColor
                )
            },
            selected = false,
            alwaysShowLabel = true,
            onClick = {
                if (currentRoute != "project_list") {
                    navController.navigate("project_list")
                }
            }
        )

        NavigationBarItem(
            icon = {
                Icon(
                    imageVector = MyIcons.friends(),
                    contentDescription = "Friends",
                    tint = if (currentRoute == "friends") selectedColor else defaultColor
                )
            },
            label = {
                Text(
                    "Friends",
                    color = if (currentRoute == "friends") selectedTextColor else defaultColor
                )
            },
            selected = false,
            alwaysShowLabel = true,
            onClick = {
                if (currentRoute != "friends") {
                    navController.navigate("friends")
                }
            }
        )

        NavigationBarItem(
            icon = {
                Icon(
                    Icons.Default.Notifications,
                    contentDescription = "Notifications",
                    tint = if (currentRoute == "notifications") selectedColor else defaultColor
                )
            },
            label = {
                Text(
                    "Notifications",
                    color = if (currentRoute == "notifications") selectedTextColor else defaultColor
                )
            },
            selected = false,
            alwaysShowLabel = true,
            onClick = {
                if (currentRoute != "notifications") {
                    navController.navigate("notifications")
                }
            }
        )
    }
}


