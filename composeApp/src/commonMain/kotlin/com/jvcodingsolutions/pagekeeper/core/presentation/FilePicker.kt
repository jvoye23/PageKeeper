package com.jvcodingsolutions.pagekeeper.core.presentation

import androidx.compose.runtime.Composable

expect class FilePickerLauncher {
    fun launch()
}

@Composable
expect fun rememberFilePickerLauncher(
    onResult: (fileName: String?, fileBytes: ByteArray?) -> Unit,
): FilePickerLauncher
