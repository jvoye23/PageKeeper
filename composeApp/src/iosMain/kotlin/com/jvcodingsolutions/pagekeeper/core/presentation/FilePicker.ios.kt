package com.jvcodingsolutions.pagekeeper.core.presentation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.usePinned
import platform.Foundation.NSData
import platform.Foundation.NSURL
import platform.Foundation.dataWithContentsOfURL
import platform.Foundation.getBytes
import platform.UIKit.UIApplication
import platform.UIKit.UIDocumentPickerViewController
import platform.UIKit.UIDocumentPickerDelegateProtocol
import platform.UniformTypeIdentifiers.UTTypeData
import platform.darwin.NSObject

actual class FilePickerLauncher(
    private val onResult: (fileName: String?, fileBytes: ByteArray?) -> Unit,
) {
    // Hold a strong reference to the delegate to prevent deallocation
    private var currentDelegate: DocumentPickerDelegate? = null

    actual fun launch() {
        val picker = UIDocumentPickerViewController(
            forOpeningContentTypes = listOf(UTTypeData),
        )
        picker.allowsMultipleSelection = false

        val delegate = DocumentPickerDelegate { fileName, bytes ->
            currentDelegate = null
            onResult(fileName, bytes)
        }
        currentDelegate = delegate
        picker.delegate = delegate

        val scene = UIApplication.sharedApplication.connectedScenes.firstOrNull()
        val windowScene = scene as? platform.UIKit.UIWindowScene
        val rootViewController = windowScene?.windows
            ?.firstOrNull { (it as? platform.UIKit.UIWindow)?.isKeyWindow() == true }
            ?.let { (it as? platform.UIKit.UIWindow)?.rootViewController }

        rootViewController?.presentViewController(picker, animated = true, completion = null)
    }
}

@Composable
actual fun rememberFilePickerLauncher(
    onResult: (fileName: String?, fileBytes: ByteArray?) -> Unit,
): FilePickerLauncher {
    return remember(onResult) { FilePickerLauncher(onResult) }
}

@OptIn(ExperimentalForeignApi::class)
private class DocumentPickerDelegate(
    private val onResult: (fileName: String?, fileBytes: ByteArray?) -> Unit,
) : NSObject(), UIDocumentPickerDelegateProtocol {

    override fun documentPicker(
        controller: UIDocumentPickerViewController,
        didPickDocumentsAtURLs: List<*>,
    ) {
        val url = didPickDocumentsAtURLs.firstOrNull() as? NSURL
        if (url == null) {
            onResult(null, null)
            return
        }

        val accessing = url.startAccessingSecurityScopedResource()
        try {
            val fileName = url.lastPathComponent
            val data = NSData.dataWithContentsOfURL(url)
            val bytes = data?.toByteArray()
            onResult(fileName, bytes)
        } finally {
            if (accessing) {
                url.stopAccessingSecurityScopedResource()
            }
        }
    }

    override fun documentPickerWasCancelled(controller: UIDocumentPickerViewController) {
        onResult(null, null)
    }
}

@OptIn(ExperimentalForeignApi::class)
private fun NSData.toByteArray(): ByteArray {
    val size = this.length.toInt()
    val bytes = ByteArray(size)
    if (size > 0) {
        bytes.usePinned { pinned ->
            this.getBytes(pinned.addressOf(0), this.length)
        }
    }
    return bytes
}
