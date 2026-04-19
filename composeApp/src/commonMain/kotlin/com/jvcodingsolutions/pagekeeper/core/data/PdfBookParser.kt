package com.jvcodingsolutions.pagekeeper.core.data

import com.jvcodingsolutions.pagekeeper.core.domain.DataError
import com.jvcodingsolutions.pagekeeper.core.domain.Result

class PdfBookParser {

    fun parse(pdfBytes: ByteArray): Result<BookMetadata, DataError.Local> {
        return try {
            val text = pdfBytes.decodeToString(throwOnInvalidSequence = false)
            val title = extractPdfField(text, "Title") ?: "Untitled"
            val author = extractPdfField(text, "Author") ?: "Unknown Author"

            Result.Success(
                BookMetadata(
                    title = title,
                    author = author,
                    coverBytes = null,
                    coverContentType = null,
                )
            )
        } catch (_: Exception) {
            Result.Error(DataError.Local.UNKNOWN)
        }
    }

    private fun extractPdfField(text: String, fieldName: String): String? {
        // Try parenthesized string: /Title (My Book Title)
        val parenRegex = Regex("""/$fieldName\s*\(([^)]*)\)""")
        parenRegex.find(text)?.let { match ->
            val value = match.groupValues[1]
                .replace("\\(", "(")
                .replace("\\)", ")")
                .replace("\\\\", "\\")
            if (value.isNotBlank()) return value
        }

        // Try hex string: /Title <FEFF0048006500...>
        val hexRegex = Regex("""/$fieldName\s*<([0-9A-Fa-f]+)>""")
        hexRegex.find(text)?.let { match ->
            val hex = match.groupValues[1]
            val decoded = decodeHexString(hex)
            if (decoded.isNotBlank()) return decoded
        }

        return null
    }

    private fun decodeHexString(hex: String): String {
        if (hex.length < 4) return ""

        // Check for UTF-16 BOM (FEFF)
        val isUtf16 = hex.length >= 4 &&
                hex.substring(0, 4).uppercase() == "FEFF"

        return if (isUtf16) {
            // Decode UTF-16BE (skip BOM)
            val chars = StringBuilder()
            var i = 4
            while (i + 3 < hex.length) {
                val codePoint = hex.substring(i, i + 4).toInt(16)
                chars.append(codePoint.toChar())
                i += 4
            }
            chars.toString()
        } else {
            // Decode as ASCII hex pairs
            val chars = StringBuilder()
            var i = 0
            while (i + 1 < hex.length) {
                val byte = hex.substring(i, i + 2).toInt(16)
                chars.append(byte.toChar())
                i += 2
            }
            chars.toString()
        }
    }
}
