package com.jvcodingsolutions.pagekeeper.core.data

expect class FileStorage {
    fun getBooksDir(): String
    fun writeFile(relativePath: String, bytes: ByteArray): String
    fun readFile(absolutePath: String): ByteArray
    fun deleteFile(absolutePath: String): Boolean
    fun getFullPath(relativePath: String): String
}
