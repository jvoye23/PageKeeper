package com.jvcodingsolutions.pagekeeper.core.presentation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import platform.Foundation.NSFileManager
import platform.Foundation.NSTemporaryDirectory
import platform.Foundation.NSURL
import platform.UIKit.UIActivityViewController
import platform.UIKit.UIApplication
import platform.UIKit.UIWindow
import platform.UIKit.UIWindowScene

actual class BookSharerLauncher {
    actual fun share(file: ShareFile) {
        val url = createShareFile(file) ?: return
        presentShareSheet(listOf(url))
    }

    actual fun share(files: List<ShareFile>) {
        val urls = files.mapNotNull { createShareFile(it) }
        if (urls.isEmpty()) return
        presentShareSheet(urls)
    }

    private fun createShareFile(shareFile: ShareFile): NSURL? {
        val sourceUrl = NSURL.fileURLWithPath(shareFile.filePath)
        val extension = shareFile.filePath.substringAfterLast('.', "")
        val sanitizedName = shareFile.displayName.replace(Regex("[/\\\\:*?\"<>|]"), "_")
        val shareDir = NSTemporaryDirectory() + "share/"
        val fileManager = NSFileManager.defaultManager
        if (!fileManager.fileExistsAtPath(shareDir)) {
            fileManager.createDirectoryAtPath(
                shareDir,
                withIntermediateDirectories = true,
                attributes = null,
                error = null,
            )
        }
        val destPath = "$shareDir$sanitizedName.$extension"
        val destUrl = NSURL.fileURLWithPath(destPath)

        // Remove existing file if present, then copy
        if (fileManager.fileExistsAtPath(destPath)) {
            fileManager.removeItemAtPath(destPath, error = null)
        }
        fileManager.copyItemAtURL(sourceUrl, toURL = destUrl, error = null)
        return destUrl
    }

    private fun presentShareSheet(items: List<Any>) {
        val activityController = UIActivityViewController(
            activityItems = items,
            applicationActivities = null,
        )

        val scene = UIApplication.sharedApplication.connectedScenes.firstOrNull()
        val windowScene = scene as? UIWindowScene
        val rootViewController = windowScene?.windows
            ?.firstOrNull { (it as? UIWindow)?.isKeyWindow() == true }
            ?.let { (it as? UIWindow)?.rootViewController }

        rootViewController?.presentViewController(
            activityController,
            animated = true,
            completion = null,
        )
    }
}

@Composable
actual fun rememberBookSharerLauncher(): BookSharerLauncher {
    return remember { BookSharerLauncher() }
}
