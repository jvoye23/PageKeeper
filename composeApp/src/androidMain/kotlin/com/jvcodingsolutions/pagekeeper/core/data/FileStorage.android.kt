package com.jvcodingsolutions.pagekeeper.core.data

import android.content.Context
import java.io.File

actual class FileStorage(private val context: Context) {

    actual fun getBooksDir(): String {
        val dir = File(context.filesDir, "books")
        if (!dir.exists()) dir.mkdirs()
        return dir.absolutePath
    }

    actual fun writeFile(relativePath: String, bytes: ByteArray): String {
        val file = File(getBooksDir(), relativePath)
        file.parentFile?.mkdirs()
        file.writeBytes(bytes)
        return file.absolutePath
    }

    actual fun readFile(absolutePath: String): ByteArray {
        return File(absolutePath).readBytes()
    }

    actual fun deleteFile(absolutePath: String): Boolean {
        return File(absolutePath).delete()
    }

    actual fun getFullPath(relativePath: String): String {
        return File(getBooksDir(), relativePath).absolutePath
    }
}
