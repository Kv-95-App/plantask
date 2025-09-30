package kv.apps.taskmanager.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance

/**
 * Returns black or white text color depending on the background luminance
 * to ensure good contrast across devices and themes.
 */
fun contrastingTextColor(background: Color): Color {
    // WCAG suggests 0.179 as a threshold when mixing with sRGB gamma; here we use 0.5 simple cutoff.
    // Since mainAppColor is a bright yellow, this will usually return Black.
    return if (background.luminance() > 0.5f) Color.Black else Color.White
}
