package kv.apps.taskmanager.presentation.screens.utils.shared.uiComposables

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import kv.apps.taskmanager.theme.backgroundColor
import kv.apps.taskmanager.theme.mainAppColor

@Composable
fun LoadingOverlay(
    modifier: Modifier = Modifier,
    overlayBackground: Color = backgroundColor,
    indicatorColor: Color = mainAppColor,
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(overlayBackground),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(color = indicatorColor)
    }
}
