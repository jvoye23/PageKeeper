package com.jvcodingsolutions.pagekeeper.core.presentation

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext

actual class FilePickerLauncher(
    private val launcher: androidx.activity.result.ActivityResultLauncher<Array<String>>,
) {
    actual fun launch() {
        launcher.launch(arrayOf("*/*"))
    }
}

@Composable
actual fun rememberFilePickerLauncher(
    onResult: (fileName: String?, fileBytes: ByteArray?) -> Unit,
): FilePickerLauncher {
    val context = LocalContext.current

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument(),
    ) { uri: Uri? ->
        if (uri == null) {
            onResult(null, null)
            return@rememberLauncherForActivityResult
        }

        val fileName = getFileName(context, uri)
        val bytes = context.contentResolver.openInputStream(uri)?.use { it.readBytes() }

        if (bytes != null) {
            onResult(fileName, bytes)
        } else {
            onResult(null, null)
        }
    }

    return remember(launcher) { FilePickerLauncher(launcher) }
}

private fun getFileName(context: android.content.Context, uri: Uri): String? {
    if (uri.scheme == "content") {
        context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            val nameIndex = cursor.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
            if (nameIndex >= 0 && cursor.moveToFirst()) {
                return cursor.getString(nameIndex)
            }
        }
    }
    return uri.lastPathSegment
}
