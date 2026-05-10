package com.jvcodingsolutions.pagekeeper.core.presentation

import androidx.compose.runtime.Composable

@Composable
actual fun OrientationEffect(lockLandscape: Boolean) {
    // No-op on iOS — orientation is managed by the system
}
