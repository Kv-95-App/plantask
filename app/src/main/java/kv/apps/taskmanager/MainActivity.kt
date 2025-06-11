package kv.apps.taskmanager

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.runtime.DisposableEffect
import androidx.navigation.compose.rememberNavController
import dagger.hilt.android.AndroidEntryPoint
import kv.apps.taskmanager.presentation.navigation.NavGraph
import kv.apps.taskmanager.presentation.navigation.Screen
import kv.apps.taskmanager.presentation.viewmodel.auth.AuthViewModel
import kv.apps.taskmanager.theme.TaskManagerTheme

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private val authViewModel: AuthViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            TaskManagerTheme {
                val navController = rememberNavController()

                DisposableEffect(Unit) {
                    authViewModel.registerLifecycle(lifecycle)
                    onDispose {
                        authViewModel.unregisterLifecycle(lifecycle)
                    }
                }

                NavGraph(
                    navController = navController
                )
            }
        }
    }
}