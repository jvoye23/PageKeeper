package com.jvcodingsolutions.pagekeeper.core.data

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.usePinned
import platform.Foundation.NSData
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSSearchPathForDirectoriesInDomains
import platform.Foundation.NSUserDomainMask
import platform.Foundation.create
import platform.Foundation.dataWithContentsOfFile
import platform.Foundation.getBytes
import platform.Foundation.writeToFile

@OptIn(ExperimentalForeignApi::class)
actual class FileStorage {

    private val fileManager = NSFileManager.defaultManager

    actual fun getBooksDir(): String {
        val paths = NSSearchPathForDirectoriesInDomains(
            NSDocumentDirectory,
            NSUserDomainMask,
            true,
        )
        val documentsDir = paths.first() as String
        val booksDir = "$documentsDir/books"
        if (!fileManager.fileExistsAtPath(booksDir)) {
            fileManager.createDirectoryAtPath(
                booksDir,
                withIntermediateDirectories = true,
                attributes = null,
                error = null,
            )
        }
        return booksDir
    }

    actual fun writeFile(relativePath: String, bytes: ByteArray): String {
        val fullPath = getFullPath(relativePath)
        val parentDir = fullPath.substringBeforeLast("/")
        if (!fileManager.fileExistsAtPath(parentDir)) {
            fileManager.createDirectoryAtPath(
                parentDir,
                withIntermediateDirectories = true,
                attributes = null,
                error = null,
            )
        }
        val data = bytes.usePinned { pinned ->
            NSData.create(
                bytes = pinned.addressOf(0),
                length = bytes.size.toULong(),
            )
        }
        data.writeToFile(fullPath, atomically = true)
        return fullPath
    }

    actual fun readFile(absolutePath: String): ByteArray {
        val data = NSData.dataWithContentsOfFile(absolutePath)
            ?: throw IllegalStateException("File not found: $absolutePath")
        return data.toByteArray()
    }

    actual fun deleteFile(absolutePath: String): Boolean {
        return fileManager.removeItemAtPath(absolutePath, error = null)
    }

    actual fun getFullPath(relativePath: String): String {
        return "${getBooksDir()}/$relativePath"
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
