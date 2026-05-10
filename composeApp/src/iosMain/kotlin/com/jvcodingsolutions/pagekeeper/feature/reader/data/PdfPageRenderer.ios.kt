package com.jvcodingsolutions.pagekeeper.feature.reader.data

import androidx.compose.ui.graphics.ImageBitmap

actual class PdfPageRenderer actual constructor(filePath: String) {
    actual fun getPageCount(): Int = 0
    actual fun renderPage(index: Int, width: Int): ImageBitmap? = null
    actual fun close() {}
}
