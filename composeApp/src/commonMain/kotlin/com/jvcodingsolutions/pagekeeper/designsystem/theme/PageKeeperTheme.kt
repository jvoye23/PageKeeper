package com.jvcodingsolutions.pagekeeper.designsystem.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val PageKeeperColorScheme = lightColorScheme(
    primary = Primary,
    onPrimary = OnPrimary,
    background = BgMain,
    onBackground = TextPrimary,
    surface = BgMain,
    onSurface = TextPrimary,
    onSurfaceVariant = TextSecondary,
    secondaryContainer = BgActive,
    surfaceContainerHigh = BgCard,
    surfaceContainerLow = TabletBlockBg,
    outlineVariant = Divider,
    error = StateAlert,
    onError = OnPrimary,
)

@Composable
fun PageKeeperTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = PageKeeperColorScheme,
        typography = PageKeeperTypography(),
        shapes = PageKeeperShapes,
        content = content,
    )
}
