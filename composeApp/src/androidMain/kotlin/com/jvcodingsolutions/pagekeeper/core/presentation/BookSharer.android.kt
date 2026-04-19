package com.jvcodingsolutions.pagekeeper.core.presentation

import android.content.Context
import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.FileProvider
import java.io.File

actual class BookSharerLauncher(
    private val context: Context,
) {
    actual fun share(file: ShareFile) {
        val shareFile = createShareFile(file) ?: return

        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            shareFile,
        )

        val intent = Intent(Intent.ACTION_SEND).apply {
            type = getMimeType(shareFile.extension)
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        context.startActivity(Intent.createChooser(intent, null))
    }

    actual fun share(files: List<ShareFile>) {
        val shareFiles = files.mapNotNull { createShareFile(it) }
        if (shareFiles.isEmpty()) return

        if (shareFiles.size == 1) {
            val single = shareFiles.first()
            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                single,
            )
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = getMimeType(single.extension)
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(Intent.createChooser(intent, null))
            return
        }

        val uris = ArrayList(
            shareFiles.map { file ->
                FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.fileprovider",
                    file,
                )
            }
        )

        val intent = Intent(Intent.ACTION_SEND_MULTIPLE).apply {
            type = "*/*"
            putParcelableArrayListExtra(Intent.EXTRA_STREAM, uris)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        context.startActivity(Intent.createChooser(intent, null))
    }

    private fun createShareFile(shareFile: ShareFile): File? {
        val source = File(shareFile.filePath)
        if (!source.exists()) return null

        val extension = source.extension
        val sanitizedName = shareFile.displayName.replace(Regex("[/\\\\:*?\"<>|]"), "_")
        val shareDir = File(context.cacheDir, "share")
        shareDir.mkdirs()
        val dest = File(shareDir, "$sanitizedName.$extension")
        source.copyTo(dest, overwrite = true)
        return dest
    }

    private fun getMimeType(extension: String): String {
        return when (extension.lowercase()) {
            "epub" -> "application/epub+zip"
            "pdf" -> "application/pdf"
            "fb2" -> "application/x-fictionbook+xml"
            else -> "application/octet-stream"
        }
    }
}

@Composable
actual fun rememberBookSharerLauncher(): BookSharerLauncher {
    val context = LocalContext.current
    return remember(context) { BookSharerLauncher(context) }
}
