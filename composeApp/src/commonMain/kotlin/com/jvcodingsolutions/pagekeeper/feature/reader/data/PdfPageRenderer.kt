package com.jvcodingsolutions.pagekeeper.feature.reader.data

import androidx.compose.ui.graphics.ImageBitmap

expect class PdfPageRenderer(filePath: String) {
    fun getPageCount(): Int
    fun renderPage(index: Int, width: Int): ImageBitmap?
    fun close()
}
