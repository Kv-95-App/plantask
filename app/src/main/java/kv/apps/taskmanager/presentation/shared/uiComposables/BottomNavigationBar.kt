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
import kv.apps.taskmanager.presentation.navigation.Screen
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
                    tint = if (currentRoute == Screen.ProjectList.route) selectedColor else defaultColor
                )
            },
            label = {
                Text(
                    "Home",
                    color = if (currentRoute == Screen.ProjectList.route) selectedTextColor else defaultColor
                )
            },
            selected = false,
            alwaysShowLabel = true,
            onClick = {
                if (currentRoute != Screen.ProjectList.route) {
                    navController.navigate(Screen.ProjectList.route)
                }
            }
        )

        NavigationBarItem(
            icon = {
                Icon(
                    imageVector = MyIcons.friends(),
                    contentDescription = "Friends",
                    tint = if (currentRoute == Screen.Friends.route) selectedColor else defaultColor
                )
            },
            label = {
                Text(
                    "Friends",
                    color = if (currentRoute == Screen.Friends.route) selectedTextColor else defaultColor
                )
            },
            selected = false,
            alwaysShowLabel = true,
            onClick = {
                if (currentRoute != Screen.Friends.route) {
                    navController.navigate(Screen.Friends.route)
                }
            }
        )

        NavigationBarItem(
            icon = {
                Icon(
                    Icons.Default.Notifications,
                    contentDescription = "Notifications",
                    tint = if (currentRoute == Screen.Notifications.route) selectedColor else defaultColor
                )
            },
            label = {
                Text(
                    "Notifications",
                    color = if (currentRoute == Screen.Notifications.route) selectedTextColor else defaultColor
                )
            },
            selected = false,
            alwaysShowLabel = true,
            onClick = {
                if (currentRoute != Screen.Notifications.route) {
                    navController.navigate(Screen.Notifications.route)
                }
            }
        )
    }
}


