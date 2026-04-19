package com.jvcodingsolutions.pagekeeper.core.data

expect class ZipReader(bytes: ByteArray) {
    fun readEntry(entryName: String): ByteArray?
    fun listEntries(): List<String>
    fun close()
}
