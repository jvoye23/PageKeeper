package com.jvcodingsolutions.pagekeeper.feature.reader.data

import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer as AndroidPdfRenderer
import android.os.ParcelFileDescriptor
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import java.io.File

actual class PdfPageRenderer actual constructor(filePath: String) {

    private val fileDescriptor: ParcelFileDescriptor =
        ParcelFileDescriptor.open(File(filePath), ParcelFileDescriptor.MODE_READ_ONLY)

    private val renderer: AndroidPdfRenderer = AndroidPdfRenderer(fileDescriptor)

    actual fun getPageCount(): Int = renderer.pageCount

    actual fun renderPage(index: Int, width: Int): ImageBitmap? {
        if (index < 0 || index >= renderer.pageCount) return null
        val page = renderer.openPage(index)
        val scale = width.toFloat() / page.width
        val bitmapWidth = width
        val bitmapHeight = (page.height * scale).toInt()
        val bitmap = Bitmap.createBitmap(bitmapWidth, bitmapHeight, Bitmap.Config.ARGB_8888)
        bitmap.eraseColor(android.graphics.Color.WHITE)
        page.render(bitmap, null, null, AndroidPdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
        page.close()
        return bitmap.asImageBitmap()
    }

    actual fun close() {
        renderer.close()
        fileDescriptor.close()
    }
}
