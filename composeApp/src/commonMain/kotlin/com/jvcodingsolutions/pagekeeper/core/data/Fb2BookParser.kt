package com.jvcodingsolutions.pagekeeper.core.data

import com.jvcodingsolutions.pagekeeper.core.domain.DataError
import com.jvcodingsolutions.pagekeeper.core.domain.Result
import nl.adaptivity.xmlutil.EventType
import nl.adaptivity.xmlutil.XmlReader
import nl.adaptivity.xmlutil.xmlStreaming
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

class Fb2BookParser {

    @OptIn(ExperimentalEncodingApi::class)
    fun parse(xmlBytes: ByteArray): Result<BookMetadata, DataError.Local> {
        return try {
            val xmlString = xmlBytes.decodeToString()
            val reader = xmlStreaming.newReader(xmlString)
            val raw = parseInternal(reader)
            reader.close()
            val coverBytes = raw.coverBase64
                ?.replace("\\s".toRegex(), "")
                ?.let { Base64.decode(it) }
            Result.Success(
                BookMetadata(
                    title = raw.title,
                    author = raw.author,
                    coverBytes = coverBytes,
                    coverContentType = raw.coverContentType,
                )
            )
        } catch (e: Exception) {
            Result.Error(DataError.Local.UNKNOWN)
        }
    }

    private fun parseInternal(reader: XmlReader): Fb2RawMetadata {
        var title = "Untitled"
        var firstName = ""
        var lastName = ""
        var coverImageId: String? = null
        var coverBase64: String? = null
        var coverContentType: String? = null

        var inTitleInfo = false
        var inAuthor = false
        var inBookTitle = false
        var inFirstName = false
        var inLastName = false
        var inCoverpage = false
        var currentBinaryId: String? = null
        var currentBinaryContentType: String? = null
        var inTargetBinary = false

        while (reader.hasNext()) {
            val event = reader.next()

            when (event) {
                EventType.START_ELEMENT -> {
                    val localName = reader.localName

                    when (localName) {
                        "title-info" -> inTitleInfo = true
                        "author" -> if (inTitleInfo) inAuthor = true
                        "book-title" -> if (inTitleInfo) inBookTitle = true
                        "first-name" -> if (inAuthor) inFirstName = true
                        "last-name" -> if (inAuthor) inLastName = true
                        "coverpage" -> if (inTitleInfo) inCoverpage = true
                        "image" -> {
                            if (inCoverpage) {
                                // The href attribute may be namespaced (l:href or xlink:href)
                                // or just href — try all variants
                                val href = findHrefAttribute(reader)
                                if (href != null) {
                                    // Strip leading '#' to get the binary element ID
                                    coverImageId = href.removePrefix("#")
                                }
                            }
                        }
                        "binary" -> {
                            val id = findAttribute(reader, "id")
                            val contentType = findAttribute(reader, "content-type")
                            if (id != null && id == coverImageId) {
                                inTargetBinary = true
                                currentBinaryContentType = contentType
                            }
                        }
                    }
                }

                EventType.END_ELEMENT -> {
                    val localName = reader.localName

                    when (localName) {
                        "title-info" -> inTitleInfo = false
                        "author" -> if (inTitleInfo) inAuthor = false
                        "book-title" -> inBookTitle = false
                        "first-name" -> inFirstName = false
                        "last-name" -> inLastName = false
                        "coverpage" -> inCoverpage = false
                        "binary" -> {
                            if (inTargetBinary) {
                                coverContentType = currentBinaryContentType
                                inTargetBinary = false
                            }
                        }
                    }
                }

                EventType.TEXT, EventType.CDSECT -> {
                    val text = reader.text.trim()
                    if (text.isEmpty()) continue

                    when {
                        inBookTitle -> title = text
                        inFirstName -> firstName = text
                        inLastName -> lastName = text
                        inTargetBinary -> {
                            // Base64 cover data may span multiple text events
                            coverBase64 = (coverBase64 ?: "") + text
                        }
                    }
                }

                else -> { /* ignore */ }
            }
        }

        val author = buildString {
            if (firstName.isNotBlank()) append(firstName)
            if (lastName.isNotBlank()) {
                if (isNotEmpty()) append(" ")
                append(lastName)
            }
        }.ifBlank { "Unknown Author" }

        return Fb2RawMetadata(
            title = title,
            author = author,
            coverBase64 = coverBase64,
            coverContentType = coverContentType,
        )
    }

    private fun findHrefAttribute(reader: XmlReader): String? {
        for (i in 0 until reader.attributeCount) {
            val localName = reader.getAttributeLocalName(i)
            if (localName == "href") {
                return reader.getAttributeValue(i)
            }
        }
        return null
    }

    private fun findAttribute(reader: XmlReader, name: String): String? {
        for (i in 0 until reader.attributeCount) {
            val localName = reader.getAttributeLocalName(i)
            if (localName == name) {
                return reader.getAttributeValue(i)
            }
        }
        return null
    }
}

private data class Fb2RawMetadata(
    val title: String,
    val author: String,
    val coverBase64: String?,
    val coverContentType: String?,
)
