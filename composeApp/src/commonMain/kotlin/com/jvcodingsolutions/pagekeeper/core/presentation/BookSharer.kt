package com.jvcodingsolutions.pagekeeper.core.presentation

import androidx.compose.runtime.Composable

data class ShareFile(
    val filePath: String,
    val displayName: String,
)

expect class BookSharerLauncher {
    fun share(file: ShareFile)
    fun share(files: List<ShareFile>)
}

@Composable
expect fun rememberBookSharerLauncher(): BookSharerLauncher
