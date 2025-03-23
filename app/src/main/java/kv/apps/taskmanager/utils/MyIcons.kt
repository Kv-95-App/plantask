package kv.apps.taskmanager.utils

import androidx.compose.material.icons.filled.Home
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import kv.apps.taskmanager.R


object MyIcons {
    val Home = androidx.compose.material.icons.Icons.Filled.Home

    @Composable
    fun friends(): ImageVector {
        return ImageVector.vectorResource(id = R.drawable.friends) // Correct usage
    }
}
