package com.jvcodingsolutions.pagekeeper.core.data

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.alloc
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
import kotlinx.cinterop.reinterpret
import kotlinx.cinterop.usePinned
import kotlinx.cinterop.value
import platform.zlib.Z_FINISH
import platform.zlib.Z_OK
import platform.zlib.Z_STREAM_END
import platform.zlib.inflate
import platform.zlib.inflateEnd
import platform.zlib.inflateInit2
import platform.zlib.z_stream

@OptIn(ExperimentalForeignApi::class)
actual class ZipReader actual constructor(private val bytes: ByteArray) {

    private data class ZipEntry(
        val name: String,
        val compressedSize: Int,
        val uncompressedSize: Int,
        val compressionMethod: Int,
        val dataOffset: Int,
    )

    private val entries: List<ZipEntry> by lazy { parseCentralDirectory() }

    actual fun readEntry(entryName: String): ByteArray? {
        val entry = entries.find { it.name == entryName } ?: return null
        val compressedData = bytes.copyOfRange(entry.dataOffset, entry.dataOffset + entry.compressedSize)

        return when (entry.compressionMethod) {
            0 -> compressedData // STORE — no compression
            8 -> inflateData(compressedData, entry.uncompressedSize) // DEFLATE
            else -> null
        }
    }

    actual fun listEntries(): List<String> = entries.map { it.name }

    actual fun close() { /* No resources to release */ }

    private fun parseCentralDirectory(): List<ZipEntry> {
        val result = mutableListOf<ZipEntry>()

        // Find End of Central Directory record (signature 0x06054b50)
        var eocdOffset = -1
        for (i in bytes.size - 22 downTo 0) {
            if (readInt(i) == 0x06054b50) {
                eocdOffset = i
                break
            }
        }
        if (eocdOffset < 0) return result

        val centralDirOffset = readInt(eocdOffset + 16)
        val entryCount = readShort(eocdOffset + 10)

        var offset = centralDirOffset
        for (i in 0 until entryCount) {
            if (readInt(offset) != 0x02014b50) break // Central directory file header signature

            val compressionMethod = readShort(offset + 10)
            val compressedSize = readInt(offset + 20)
            val uncompressedSize = readInt(offset + 24)
            val nameLength = readShort(offset + 28)
            val extraLength = readShort(offset + 30)
            val commentLength = readShort(offset + 32)
            val localHeaderOffset = readInt(offset + 42)

            val name = bytes.decodeToString(offset + 46, offset + 46 + nameLength)

            // Calculate actual data offset from local file header
            val localNameLength = readShort(localHeaderOffset + 26)
            val localExtraLength = readShort(localHeaderOffset + 28)
            val dataOffset = localHeaderOffset + 30 + localNameLength + localExtraLength

            result.add(
                ZipEntry(
                    name = name,
                    compressedSize = compressedSize,
                    uncompressedSize = uncompressedSize,
                    compressionMethod = compressionMethod,
                    dataOffset = dataOffset,
                )
            )

            offset += 46 + nameLength + extraLength + commentLength
        }

        return result
    }

    private fun readShort(offset: Int): Int {
        return (bytes[offset].toInt() and 0xFF) or
                ((bytes[offset + 1].toInt() and 0xFF) shl 8)
    }

    private fun readInt(offset: Int): Int {
        return (bytes[offset].toInt() and 0xFF) or
                ((bytes[offset + 1].toInt() and 0xFF) shl 8) or
                ((bytes[offset + 2].toInt() and 0xFF) shl 16) or
                ((bytes[offset + 3].toInt() and 0xFF) shl 24)
    }

    private fun inflateData(compressed: ByteArray, expectedSize: Int): ByteArray? = memScoped {
        val output = ByteArray(expectedSize)
        val stream = alloc<z_stream>()

        compressed.usePinned { compressedPinned ->
            output.usePinned { outputPinned ->
                stream.next_in = compressedPinned.addressOf(0).reinterpret()
                stream.avail_in = compressed.size.toUInt()
                stream.next_out = outputPinned.addressOf(0).reinterpret()
                stream.avail_out = output.size.toUInt()

                // -15 for raw DEFLATE (no zlib/gzip header)
                if (inflateInit2(stream.ptr, -15) != Z_OK) return null

                val result = inflate(stream.ptr, Z_FINISH)
                inflateEnd(stream.ptr)

                if (result != Z_STREAM_END) return null
            }
        }
        output
    }
}
