package kv.apps.taskmanager.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFFBB86FC),  // purple_200
    onPrimary = Color(0xFF000000),
    primaryContainer = Color(0xFF3700B3),  // purple_700
    onPrimaryContainer = Color(0xFFFFFFFF),
    secondary = Color(0xFF03DAC5),  // teal_200
    onSecondary = Color(0xFF000000),
    background = Color(0xFF121212),  // background_color
    onBackground = Color(0xFFFFFFFF),  // white
    surface = Color(0xFF1E1E1E),  // surface_color
    onSurface = Color(0xFFFFFFFF)  // white
)

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF6200EE),  // purple_500
    secondary = Color(0xFF018786), // teal_700
    tertiary = Color(0xFF3700B3),  // purple_700
    background = Color(0xFFFFFFFF), // white
    surface = Color(0xFFFFFFFF),   // white
    onBackground = Color(0xFF000000), // black
    onSurface = Color(0xFF000000)   // black
)

@Composable
fun TaskManagerTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}