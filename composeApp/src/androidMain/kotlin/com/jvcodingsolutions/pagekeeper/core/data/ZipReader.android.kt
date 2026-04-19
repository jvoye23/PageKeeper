package com.jvcodingsolutions.pagekeeper.core.data

import java.io.ByteArrayInputStream
import java.util.zip.ZipInputStream

actual class ZipReader actual constructor(private val bytes: ByteArray) {

    actual fun readEntry(entryName: String): ByteArray? {
        val zis = ZipInputStream(ByteArrayInputStream(bytes))
        zis.use { stream ->
            var entry = stream.nextEntry
            while (entry != null) {
                if (entry.name == entryName) {
                    return stream.readBytes()
                }
                entry = stream.nextEntry
            }
        }
        return null
    }

    actual fun listEntries(): List<String> {
        val entries = mutableListOf<String>()
        val zis = ZipInputStream(ByteArrayInputStream(bytes))
        zis.use { stream ->
            var entry = stream.nextEntry
            while (entry != null) {
                entries.add(entry.name)
                entry = stream.nextEntry
            }
        }
        return entries
    }

    actual fun close() {
        // No persistent resources to close; each method creates its own stream
    }
}
