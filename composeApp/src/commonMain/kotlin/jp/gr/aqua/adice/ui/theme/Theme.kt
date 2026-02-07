package jp.gr.aqua.adice.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColorScheme = lightColorScheme(
    primary = Primary,
    onPrimary = OnPrimary,
    primaryContainer = PrimaryDark,
    secondary = Secondary,
    background = Background,
    surface = Surface,
    onSurface = OnSurface,
    onBackground = OnSurface,
    surfaceVariant = LightGray,
    onSurfaceVariant = DarkGray
)

@Composable
fun AdiceTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = LightColorScheme,
        typography = Typography,
        content = content
    )
}
