package com.jvcodingsolutions.pagekeeper.core.presentation

import android.app.Activity
import android.content.pm.ActivityInfo
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.platform.LocalContext

@Composable
actual fun OrientationEffect(lockLandscape: Boolean) {
    val context = LocalContext.current
    val activity = context as? Activity ?: return

    DisposableEffect(lockLandscape) {
        activity.requestedOrientation = if (lockLandscape) {
            ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        } else {
            ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
        }

        onDispose {
            activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
        }
    }
}
