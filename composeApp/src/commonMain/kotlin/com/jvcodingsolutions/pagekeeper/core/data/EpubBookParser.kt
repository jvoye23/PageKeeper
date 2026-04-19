package com.jvcodingsolutions.pagekeeper.core.data

import com.jvcodingsolutions.pagekeeper.core.domain.DataError
import com.jvcodingsolutions.pagekeeper.core.domain.Result
import nl.adaptivity.xmlutil.EventType
import nl.adaptivity.xmlutil.xmlStreaming

class EpubBookParser {

    fun parse(epubBytes: ByteArray): Result<BookMetadata, DataError.Local> {
        val zip = ZipReader(epubBytes)
        return try {
            parseInternal(zip)
        } catch (_: Exception) {
            Result.Error(DataError.Local.UNKNOWN)
        } finally {
            zip.close()
        }
    }

    private fun parseInternal(zip: ZipReader): Result<BookMetadata, DataError.Local> {
        // 1. Read container.xml to find the OPF file path
        val containerBytes = zip.readEntry("META-INF/container.xml")
            ?: return Result.Error(DataError.Local.UNKNOWN)
        val opfPath = parseContainerXml(containerBytes.decodeToString())
            ?: return Result.Error(DataError.Local.UNKNOWN)

        // 2. Read and parse the OPF file
        val opfBytes = zip.readEntry(opfPath)
            ?: return Result.Error(DataError.Local.UNKNOWN)
        val opfDir = opfPath.substringBeforeLast("/", "")
        val opfData = parseOpf(opfBytes.decodeToString())

        // 3. Read cover image if referenced
        var coverBytes: ByteArray? = null
        var coverContentType: String? = null
        if (opfData.coverHref != null) {
            val coverPath = if (opfDir.isNotEmpty()) "$opfDir/${opfData.coverHref}" else opfData.coverHref
            coverBytes = zip.readEntry(coverPath)
            coverContentType = opfData.coverMediaType
        }

        return Result.Success(
            BookMetadata(
                title = opfData.title ?: "Untitled",
                author = opfData.author ?: "Unknown Author",
                coverBytes = coverBytes,
                coverContentType = coverContentType,
            )
        )
    }

    private fun parseContainerXml(xml: String): String? {
        val reader = xmlStreaming.newReader(xml)
        try {
            while (reader.hasNext()) {
                val event = reader.next()
                if (event == EventType.START_ELEMENT && reader.localName == "rootfile") {
                    for (i in 0 until reader.attributeCount) {
                        if (reader.getAttributeLocalName(i) == "full-path") {
                            return reader.getAttributeValue(i)
                        }
                    }
                }
            }
        } finally {
            reader.close()
        }
        return null
    }

    private fun parseOpf(xml: String): OpfData {
        var title: String? = null
        var author: String? = null
        var coverMetaContent: String? = null
        val manifestItems = mutableMapOf<String, ManifestItem>()

        var inTitle = false
        var inCreator = false

        val reader = xmlStreaming.newReader(xml)
        try {
            while (reader.hasNext()) {
                val event = reader.next()
                when (event) {
                    EventType.START_ELEMENT -> {
                        when (reader.localName) {
                            "title" -> inTitle = true
                            "creator" -> inCreator = true
                            "meta" -> {
                                // Look for <meta name="cover" content="cover-image-id"/>
                                var name: String? = null
                                var content: String? = null
                                for (i in 0 until reader.attributeCount) {
                                    when (reader.getAttributeLocalName(i)) {
                                        "name" -> name = reader.getAttributeValue(i)
                                        "content" -> content = reader.getAttributeValue(i)
                                    }
                                }
                                if (name == "cover" && content != null) {
                                    coverMetaContent = content
                                }
                            }
                            "item" -> {
                                var id: String? = null
                                var href: String? = null
                                var mediaType: String? = null
                                var properties: String? = null
                                for (i in 0 until reader.attributeCount) {
                                    when (reader.getAttributeLocalName(i)) {
                                        "id" -> id = reader.getAttributeValue(i)
                                        "href" -> href = reader.getAttributeValue(i)
                                        "media-type" -> mediaType = reader.getAttributeValue(i)
                                        "properties" -> properties = reader.getAttributeValue(i)
                                    }
                                }
                                if (id != null && href != null) {
                                    manifestItems[id] = ManifestItem(href, mediaType, properties)
                                }
                            }
                        }
                    }
                    EventType.END_ELEMENT -> {
                        when (reader.localName) {
                            "title" -> inTitle = false
                            "creator" -> inCreator = false
                        }
                    }
                    EventType.TEXT -> {
                        val text = reader.text.trim()
                        if (text.isEmpty()) continue
                        when {
                            inTitle && title == null -> title = text
                            inCreator && author == null -> author = text
                        }
                    }
                    else -> {}
                }
            }
        } finally {
            reader.close()
        }

        // Find cover image: first try EPUB3 properties="cover-image", then EPUB2 meta name="cover"
        var coverHref: String? = null
        var coverMediaType: String? = null

        // EPUB3: look for item with properties="cover-image"
        val epub3Cover = manifestItems.values.find { it.properties?.contains("cover-image") == true }
        if (epub3Cover != null) {
            coverHref = epub3Cover.href
            coverMediaType = epub3Cover.mediaType
        }

        // EPUB2: use <meta name="cover" content="id"/> to find manifest item
        if (coverHref == null && coverMetaContent != null) {
            val item = manifestItems[coverMetaContent]
            if (item != null) {
                coverHref = item.href
                coverMediaType = item.mediaType
            }
        }

        return OpfData(title, author, coverHref, coverMediaType)
    }

    private data class ManifestItem(val href: String, val mediaType: String?, val properties: String?)
    private data class OpfData(val title: String?, val author: String?, val coverHref: String?, val coverMediaType: String?)
}
