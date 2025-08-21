package kv.apps.taskmanager.presentation.screens.utils.shared.avatar

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kv.apps.taskmanager.theme.mainAppColor

@Composable
internal fun UserAvatar(
    initials: String,
    isLoading: Boolean,
    size: Dp = 28.dp
) {
    Box(
        modifier = Modifier
            .size(size)
            .clip(CircleShape)
            .background(
                if (isLoading) Color.LightGray.copy(alpha = 0.1f)
                else mainAppColor.copy(alpha = 0.2f)
            )
            .padding(4.dp),
        contentAlignment = Alignment.Center
    ) {
        when {
            isLoading -> {
                var showLoading by remember { mutableStateOf(true) }

                LaunchedEffect(Unit) {
                    delay(250)
                    showLoading = false
                }

                if (showLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(size / 2),
                        strokeWidth = 2.dp,
                        color = mainAppColor
                    )
                }
            }
            initials.isNotEmpty() -> {
                Text(
                    text = initials,
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = mainAppColor
                    )
                )
            }
        }
    }
}