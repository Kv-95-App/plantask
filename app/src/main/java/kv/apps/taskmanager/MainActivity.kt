package kv.apps.taskmanager

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.navigation.compose.rememberNavController
import dagger.hilt.android.AndroidEntryPoint
import kv.apps.taskmanager.presentation.navigation.NavGraph
import kv.apps.taskmanager.presentation.navigation.Screen
import kv.apps.taskmanager.theme.TaskManagerTheme

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            TaskManagerTheme {
                val navController = rememberNavController()
                NavGraph(
                    navController = navController,
                    startDestination = Screen.SplashScreen.route
                )
            }
        }
    }
}