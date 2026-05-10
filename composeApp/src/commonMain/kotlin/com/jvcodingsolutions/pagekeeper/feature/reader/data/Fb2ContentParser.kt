package com.jvcodingsolutions.pagekeeper.feature.reader.data

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import com.jvcodingsolutions.pagekeeper.core.domain.DataError
import com.jvcodingsolutions.pagekeeper.core.domain.Result
import com.jvcodingsolutions.pagekeeper.feature.reader.domain.BookContentElement
import com.jvcodingsolutions.pagekeeper.feature.reader.domain.BookSection
import com.jvcodingsolutions.pagekeeper.feature.reader.domain.BookStructure
import com.jvcodingsolutions.pagekeeper.feature.reader.domain.ChapterNode
import com.jvcodingsolutions.pagekeeper.feature.reader.domain.StructureSection
import nl.adaptivity.xmlutil.EventType
import nl.adaptivity.xmlutil.XmlReader
import nl.adaptivity.xmlutil.xmlStreaming

class Fb2ContentParser {

    /**
     * Parsed FB2 navigation result: leaf-level XML chunks (one per parseable
     * section) and a [BookStructure] that mirrors them as chapters/sections.
     */
    data class ParsedFb2(
        val chunks: List<String>,
        val structure: BookStructure,
    )

    /**
     * Walks the body once, yielding a leaf-level chunk per parseable section
     * and a two-level [BookStructure] suitable for the Chapters screen.
     *
     * - If a top-level &lt;section&gt; has nested sub-sections, each sub-section
     *   becomes its own chunk and is listed as a chapter under a structure
     *   section named after the top-level &lt;title&gt;.
     * - If a top-level section has no sub-sections it becomes a single chapter
     *   under a synthetic structure section named [defaultSectionTitle].
     */
    fun extractStructure(
        fileBytes: ByteArray,
        defaultSectionTitle: String,
    ): Result<ParsedFb2, DataError.Local> {
        return try {
            val xmlString = fileBytes.decodeToString()
            val (chunks, structureSections) = walkBodyForStructure(xmlString, defaultSectionTitle)
            if (chunks.isEmpty()) {
                Result.Error(DataError.Local.UNKNOWN)
            } else {
                Result.Success(ParsedFb2(chunks, BookStructure(structureSections)))
            }
        } catch (_: Exception) {
            Result.Error(DataError.Local.UNKNOWN)
        }
    }

    private fun walkBodyForStructure(
        xmlString: String,
        defaultSectionTitle: String,
    ): Pair<List<String>, List<StructureSection>> {
        val chunks = mutableListOf<String>()
        val structureSections = mutableListOf<StructureSection>()
        val orphanChapters = mutableListOf<ChapterNode>()

        val reader = xmlStreaming.newReader(xmlString)
        try {
            // Navigate to <body>
            var inBody = false
            while (reader.hasNext()) {
                val event = reader.next()
                if (event == EventType.START_ELEMENT && reader.localName == "body") {
                    inBody = true
                    break
                }
            }
            if (!inBody) return emptyList<String>() to emptyList()

            while (reader.hasNext()) {
                val event = reader.next()
                when (event) {
                    EventType.START_ELEMENT -> {
                        if (reader.localName == "section") {
                            val raw = collectElementContent(reader, "section")
                            processTopLevelSection(
                                raw = raw,
                                chunks = chunks,
                                structureSections = structureSections,
                                orphanChapters = orphanChapters,
                            )
                        }
                    }
                    EventType.END_ELEMENT -> if (reader.localName == "body") break
                    else -> {}
                }
            }
        } finally {
            reader.close()
        }

        if (orphanChapters.isNotEmpty()) {
            structureSections.add(0, StructureSection(defaultSectionTitle, orphanChapters.toList()))
        }

        return chunks to structureSections
    }

    /**
     * If [raw] (the inner XML of a top-level &lt;section&gt;) contains nested
     * &lt;section&gt; children, each child becomes its own chunk + chapter,
     * grouped under a structure section named after the parent's &lt;title&gt;.
     * Otherwise the whole section becomes one orphan chapter chunk.
     */
    private fun processTopLevelSection(
        raw: String,
        chunks: MutableList<String>,
        structureSections: MutableList<StructureSection>,
        orphanChapters: MutableList<ChapterNode>,
    ) {
        val nestedSections = extractDirectChildSections(raw)
        if (nestedSections.isEmpty()) {
            val title = peekFirstTitle(raw) ?: "Chapter ${chunks.size + 1}"
            chunks.add(raw)
            orphanChapters.add(ChapterNode(title = title, sectionIndex = chunks.size - 1))
        } else {
            val parentTitle = peekFirstTitle(raw) ?: "Section ${structureSections.size + 1}"
            val chapterNodes = nestedSections.map { childRaw ->
                val childTitle = peekFirstTitle(childRaw) ?: "Chapter ${chunks.size + 1}"
                chunks.add(childRaw)
                ChapterNode(title = childTitle, sectionIndex = chunks.size - 1)
            }
            structureSections.add(StructureSection(title = parentTitle, chapters = chapterNodes))
        }
    }

    /**
     * Returns the inner XML of each direct-child &lt;section&gt; in [parentInner].
     * Skips deeper descendants — only the next level down is collected.
     */
    private fun extractDirectChildSections(parentInner: String): List<String> {
        val wrapped = "<section>$parentInner</section>"
        val reader = xmlStreaming.newReader(wrapped)
        val results = mutableListOf<String>()
        try {
            // Skip into wrapper element
            while (reader.hasNext()) {
                val ev = reader.next()
                if (ev == EventType.START_ELEMENT && reader.localName == "section") break
            }
            while (reader.hasNext()) {
                val ev = reader.next()
                when (ev) {
                    EventType.START_ELEMENT -> {
                        if (reader.localName == "section") {
                            results.add(collectElementContent(reader, "section"))
                        } else {
                            skipElement(reader)
                        }
                    }
                    EventType.END_ELEMENT -> if (reader.localName == "section") break
                    else -> {}
                }
            }
        } catch (_: Exception) {
            // ignore — best-effort structure detection
        } finally {
            reader.close()
        }
        return results
    }

    /**
     * Reads the first &lt;title&gt; element directly inside a section's inner XML
     * and returns its text content (joined paragraphs). Returns null if absent.
     */
    private fun peekFirstTitle(inner: String): String? {
        val wrapped = "<section>$inner</section>"
        val reader = xmlStreaming.newReader(wrapped)
        try {
            // Step into wrapper
            while (reader.hasNext()) {
                val ev = reader.next()
                if (ev == EventType.START_ELEMENT && reader.localName == "section") break
            }
            while (reader.hasNext()) {
                val ev = reader.next()
                when (ev) {
                    EventType.START_ELEMENT -> {
                        if (reader.localName == "title") {
                            val text = parseTitleElement(reader)
                            if (text.isNotBlank()) return text
                        } else {
                            // Don't descend past first non-title element — title must be first child
                            skipElement(reader)
                        }
                    }
                    EventType.END_ELEMENT -> if (reader.localName == "section") return null
                    else -> {}
                }
            }
        } catch (_: Exception) {
            return null
        } finally {
            reader.close()
        }
        return null
    }

    /**
     * Lightweight initial parse: extracts raw XML strings for each top-level <section>.
     * Returns the section count without parsing any content.
     */
    fun extractSectionChunks(fileBytes: ByteArray): Result<List<String>, DataError.Local> {
        return try {
            val xmlString = fileBytes.decodeToString()
            val chunks = extractBodySectionChunks(xmlString)
            if (chunks.isEmpty()) {
                Result.Error(DataError.Local.UNKNOWN)
            } else {
                Result.Success(chunks)
            }
        } catch (_: Exception) {
            Result.Error(DataError.Local.UNKNOWN)
        }
    }

    /**
     * Parse a single section chunk (raw XML) into a BookSection.
     * Called on demand when the user scrolls to a new chapter.
     */
    fun parseSection(sectionXml: String): BookSection {
        val wrappedXml = "<section>$sectionXml</section>"
        val reader = xmlStreaming.newReader(wrappedXml)
        return try {
            // Advance past the wrapper <section> start element
            reader.next() // START_DOCUMENT or similar
            if (reader.eventType != EventType.START_ELEMENT) {
                reader.next()
            }
            parseSectionContent(reader)
        } catch (_: Exception) {
            BookSection(title = null, elements = emptyList())
        } finally {
            reader.close()
        }
    }

    private fun extractBodySectionChunks(xmlString: String): List<String> {
        // Find the <body> content and extract each top-level <section> as raw XML
        val reader = xmlStreaming.newReader(xmlString)
        val chunks = mutableListOf<String>()

        try {
            // Navigate to <body>
            var inBody = false
            while (reader.hasNext()) {
                val event = reader.next()
                if (event == EventType.START_ELEMENT && reader.localName == "body") {
                    inBody = true
                    break
                }
            }
            if (!inBody) return emptyList()

            // For each top-level <section> in body, extract raw XML via string indices
            // We use a simpler approach: parse sections and collect their content as strings
            val bodyStart = xmlString.indexOf("<body")
            if (bodyStart == -1) return emptyList()

            // Find section boundaries using the XML reader
            while (reader.hasNext()) {
                val event = reader.next()
                when (event) {
                    EventType.START_ELEMENT -> {
                        if (reader.localName == "section") {
                            val sectionContent = collectElementContent(reader, "section")
                            chunks.add(sectionContent)
                        }
                    }
                    EventType.END_ELEMENT -> {
                        if (reader.localName == "body") break
                    }
                    else -> {}
                }
            }
        } finally {
            reader.close()
        }

        return chunks
    }

    /**
     * Collects the inner XML content of an element as a string,
     * including all child elements and text.
     */
    private fun collectElementContent(reader: XmlReader, endTag: String): String {
        val sb = StringBuilder()
        var depth = 1

        while (reader.hasNext() && depth > 0) {
            val event = reader.next()
            when (event) {
                EventType.START_ELEMENT -> {
                    sb.append("<${reader.localName}")
                    for (i in 0 until reader.attributeCount) {
                        sb.append(" ${reader.getAttributeLocalName(i)}=\"${reader.getAttributeValue(i)}\"")
                    }
                    sb.append(">")
                    depth++
                }
                EventType.END_ELEMENT -> {
                    depth--
                    if (depth > 0) {
                        sb.append("</${reader.localName}>")
                    }
                }
                EventType.TEXT, EventType.CDSECT -> {
                    sb.append(reader.text)
                }
                else -> {}
            }
        }

        return sb.toString()
    }

    private fun parseSectionContent(reader: XmlReader): BookSection {
        val elements = mutableListOf<BookContentElement>()
        var sectionTitle: String? = null

        while (reader.hasNext()) {
            val event = reader.next()
            when (event) {
                EventType.START_ELEMENT -> {
                    when (reader.localName) {
                        "title" -> {
                            val titleText = parseTitleElement(reader)
                            if (titleText.isNotBlank()) {
                                sectionTitle = titleText
                                elements.add(BookContentElement.ChapterTitle(titleText))
                            }
                        }
                        "p" -> {
                            val text = parseFormattedText(reader, "p")
                            if (text.text.isNotBlank()) {
                                elements.add(BookContentElement.Paragraph(text))
                            }
                        }
                        "cite", "epigraph" -> {
                            val tag = reader.localName
                            val quoteElements = parseCiteOrEpigraph(reader, tag)
                            elements.addAll(quoteElements)
                        }
                        "section" -> {
                            // Nested section — parse inline and flatten
                            val nested = parseSectionContent(reader)
                            elements.addAll(nested.elements)
                        }
                        else -> skipElement(reader)
                    }
                }
                EventType.END_ELEMENT -> {
                    if (reader.localName == "section") break
                }
                else -> {}
            }
        }

        return BookSection(
            title = sectionTitle,
            elements = elements,
        )
    }

    private fun parseTitleElement(reader: XmlReader): String {
        val parts = mutableListOf<String>()

        while (reader.hasNext()) {
            val event = reader.next()
            when (event) {
                EventType.START_ELEMENT -> {
                    when (reader.localName) {
                        "p" -> {
                            val text = collectText(reader, "p")
                            if (text.isNotBlank()) parts.add(text)
                        }
                        else -> skipElement(reader)
                    }
                }
                EventType.TEXT, EventType.CDSECT -> {
                    val text = reader.text.trim()
                    if (text.isNotBlank()) parts.add(text)
                }
                EventType.END_ELEMENT -> {
                    if (reader.localName == "title") break
                }
                else -> {}
            }
        }

        return parts.joinToString(" – ")
    }

    private fun parseCiteOrEpigraph(reader: XmlReader, tag: String): List<BookContentElement> {
        val elements = mutableListOf<BookContentElement>()

        while (reader.hasNext()) {
            val event = reader.next()
            when (event) {
                EventType.START_ELEMENT -> {
                    when (reader.localName) {
                        "p" -> {
                            val text = parseFormattedText(reader, "p")
                            if (text.text.isNotBlank()) {
                                elements.add(BookContentElement.Quote(text))
                            }
                        }
                        "text-author" -> {
                            val text = parseFormattedText(reader, "text-author")
                            if (text.text.isNotBlank()) {
                                elements.add(BookContentElement.Quote(text))
                            }
                        }
                        else -> skipElement(reader)
                    }
                }
                EventType.END_ELEMENT -> {
                    if (reader.localName == tag) break
                }
                else -> {}
            }
        }

        return elements
    }

    private fun parseFormattedText(reader: XmlReader, endTag: String): AnnotatedString {
        return buildAnnotatedString {
            parseInlineContent(reader, endTag, this)
        }
    }

    private fun parseInlineContent(
        reader: XmlReader,
        endTag: String,
        builder: AnnotatedString.Builder,
    ) {
        while (reader.hasNext()) {
            val event = reader.next()
            when (event) {
                EventType.START_ELEMENT -> {
                    when (reader.localName) {
                        "strong" -> {
                            builder.withStyle(SpanStyle(fontWeight = FontWeight.SemiBold)) {
                                parseInlineContent(reader, "strong", builder)
                            }
                        }
                        "emphasis" -> {
                            builder.withStyle(SpanStyle(fontStyle = FontStyle.Italic)) {
                                parseInlineContent(reader, "emphasis", builder)
                            }
                        }
                        else -> {
                            parseInlineContent(reader, reader.localName, builder)
                        }
                    }
                }
                EventType.TEXT, EventType.CDSECT -> {
                    builder.append(reader.text)
                }
                EventType.END_ELEMENT -> {
                    if (reader.localName == endTag) return
                }
                else -> {}
            }
        }
    }

    private fun collectText(reader: XmlReader, endTag: String): String {
        val sb = StringBuilder()
        while (reader.hasNext()) {
            val event = reader.next()
            when (event) {
                EventType.TEXT, EventType.CDSECT -> sb.append(reader.text)
                EventType.START_ELEMENT -> {
                    sb.append(collectText(reader, reader.localName))
                }
                EventType.END_ELEMENT -> {
                    if (reader.localName == endTag) return sb.toString().trim()
                }
                else -> {}
            }
        }
        return sb.toString().trim()
    }

    private fun skipElement(reader: XmlReader) {
        var depth = 1
        while (reader.hasNext() && depth > 0) {
            when (reader.next()) {
                EventType.START_ELEMENT -> depth++
                EventType.END_ELEMENT -> depth--
                else -> {}
            }
        }
    }
}
